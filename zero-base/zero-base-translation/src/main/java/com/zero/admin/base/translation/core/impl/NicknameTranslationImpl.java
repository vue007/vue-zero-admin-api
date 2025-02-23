package com.zero.admin.base.translation.core.impl;

import lombok.AllArgsConstructor;
import com.zero.admin.base.core.service.UserService;
import com.zero.admin.base.translation.annotation.TranslationType;
import com.zero.admin.base.translation.constant.TransConstant;
import com.zero.admin.base.translation.core.TranslationInterface;

/**
 * 用户名称翻译实现
 *
 * @author may
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NICKNAME)
public class NicknameTranslationImpl implements TranslationInterface<String> {

    private final UserService userService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof Long id) {
            return userService.selectNicknameByIds(id.toString());
        } else if (key instanceof String ids) {
            return userService.selectNicknameByIds(ids);
        }
        return null;
    }
}
