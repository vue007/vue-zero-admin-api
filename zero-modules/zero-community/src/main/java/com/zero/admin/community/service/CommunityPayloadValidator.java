package com.zero.admin.community.service;

import cn.hutool.core.lang.Dict;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.json.utils.JsonUtils;
import com.zero.admin.community.constant.CommunityConstants;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 社区配置的服务端白名单校验。 */
@Component
public class CommunityPayloadValidator {

    private static final Set<String> MACRO_ACTIONS = Set.of(
        "up", "down", "mouse_up", "mouse_down", "tap", "delay", "wheel_up", "wheel_down"
    );

    public String validateAndSerialize(String kind, String deviceType, Object payload) {
        if (!CommunityConstants.KINDS.contains(kind)) {
            throw new ServiceException("不支持的配置类型");
        }
        if (!CommunityConstants.DEVICE_TYPES.contains(deviceType)) {
            throw new ServiceException("不支持的设备类型");
        }
        String json = JsonUtils.toJsonString(payload);
        if (json == null || json.getBytes(StandardCharsets.UTF_8).length > CommunityConstants.MAX_PAYLOAD_BYTES) {
            throw new ServiceException("配置内容不能为空且不能超过1 MiB");
        }
        Dict root = JsonUtils.parseMap(json);
        if (root == null) {
            throw new ServiceException("配置内容必须是 JSON 对象");
        }
        switch (kind) {
            case CommunityConstants.KIND_DPI -> validateDpi(root);
            case CommunityConstants.KIND_MACRO -> validateMacro(root);
            case CommunityConstants.KIND_LIGHTING -> validateLighting(root);
            case CommunityConstants.KIND_PROFILE -> validateProfile(root);
            default -> throw new ServiceException("不支持的配置类型");
        }
        return json;
    }

    private void validateDpi(Map<String, Object> root) {
        Object rawValues = root.get("values");
        if (!(rawValues instanceof List<?> values) || values.isEmpty() || values.size() > 20) {
            throw new ServiceException("DPI values 必须包含1到20个档位");
        }
        for (Object tier : values) {
            if (tier instanceof Number number) {
                requireRange(number, 50, 100000, "DPI数值");
            } else if (tier instanceof List<?> axes && axes.size() == 2) {
                requireRange(axes.get(0), 50, 100000, "DPI X轴");
                requireRange(axes.get(1), 50, 100000, "DPI Y轴");
            } else {
                throw new ServiceException("DPI档位必须是数值或[X,Y]");
            }
        }
        requireOptionalRange(root.get("index"), 0, values.size() - 1, "DPI当前档位");
        requireOptionalRange(root.get("size"), 1, values.size(), "DPI档位数量");
    }

    private void validateMacro(Map<String, Object> root) {
        Object rawMacros = root.get("macros");
        if (!(rawMacros instanceof List<?> macros) || macros.isEmpty() || macros.size() > 5000) {
            throw new ServiceException("宏动作必须包含1到5000项");
        }
        for (Object rawItem : macros) {
            if (!(rawItem instanceof Map<?, ?> item) || !MACRO_ACTIONS.contains(String.valueOf(item.get("action")))) {
                throw new ServiceException("宏中包含不支持的动作");
            }
            requireOptionalRange(item.get("duration"), 0, 600000, "宏动作时长");
        }
        requireOptionalRange(root.get("repeatCount"), 0, 65533, "宏重复次数");
    }

    private void validateLighting(Map<String, Object> root) {
        if (!(root.get("mode") instanceof Number) && !(root.get("type") instanceof Number)) {
            throw new ServiceException("灯光配置必须包含 mode 或 type");
        }
        requireOptionalRange(root.get("brightness"), 0, 100, "灯光亮度");
        requireOptionalRange(root.get("speed"), 0, 100, "灯光速度");
        Object color = root.get("color");
        if (color != null && !String.valueOf(color).matches("^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$")) {
            throw new ServiceException("灯光颜色必须是十六进制颜色");
        }
    }

    private void validateProfile(Map<String, Object> root) {
        Object items = root.get("items");
        Object refs = root.get("refs");
        if (!(items instanceof List<?>) && !(refs instanceof List<?>)) {
            throw new ServiceException("Profile 必须包含 items 或 refs 数组");
        }
    }

    private void requireOptionalRange(Object value, long min, long max, String label) {
        if (value != null) requireRange(value, min, max, label);
    }

    private void requireRange(Object value, long min, long max, String label) {
        if (!(value instanceof Number number) || number.longValue() < min || number.longValue() > max) {
            throw new ServiceException(label + "超出允许范围");
        }
    }
}
