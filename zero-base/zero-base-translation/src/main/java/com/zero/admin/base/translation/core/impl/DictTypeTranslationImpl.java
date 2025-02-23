package com.zero.admin.base.translation.core.impl;

import lombok.AllArgsConstructor;
import com.zero.admin.base.core.service.DictService;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.translation.annotation.TranslationType;
import com.zero.admin.base.translation.constant.TransConstant;
import com.zero.admin.base.translation.core.TranslationInterface;

/**
 * 字典翻译实现
 *
 * @author Akai
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.DICT_TYPE_TO_LABEL)
public class DictTypeTranslationImpl implements TranslationInterface<String> {

    private final DictService dictService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof String dictValue && StringUtils.isNotBlank(other)) {
            return dictService.getDictLabel(other, dictValue);
        }
        return null;
    }
}
