package com.zero.admin.base.translation.core.impl;

import lombok.AllArgsConstructor;
import com.zero.admin.base.core.service.OssService;
import com.zero.admin.base.translation.annotation.TranslationType;
import com.zero.admin.base.translation.constant.TransConstant;
import com.zero.admin.base.translation.core.TranslationInterface;

/**
 * OSS翻译实现
 *
 * @author Akai
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.OSS_ID_TO_URL)
public class OssUrlTranslationImpl implements TranslationInterface<String> {

    private final OssService ossService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof String ids) {
            return ossService.selectUrlByIds(ids);
        } else if (key instanceof Long id) {
            return ossService.selectUrlByIds(id.toString());
        }
        return null;
    }
}
