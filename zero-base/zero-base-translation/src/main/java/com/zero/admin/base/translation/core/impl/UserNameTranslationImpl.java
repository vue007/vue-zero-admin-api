package com.zero.admin.base.translation.core.impl;

import lombok.AllArgsConstructor;
import com.zero.admin.base.core.service.UserService;
import com.zero.admin.base.translation.annotation.TranslationType;
import com.zero.admin.base.translation.constant.TransConstant;
import com.zero.admin.base.translation.core.TranslationInterface;

/**
 * 用户名翻译实现
 *
 * @author Akai
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.USER_ID_TO_NAME)
public class UserNameTranslationImpl implements TranslationInterface<String> {

    private final UserService userService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof Long id) {
            return userService.selectUserNameById(id);
        }
        return null;
    }
}
