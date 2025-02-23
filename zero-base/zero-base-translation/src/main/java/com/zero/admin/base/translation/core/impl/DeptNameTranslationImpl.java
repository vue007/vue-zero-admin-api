package com.zero.admin.base.translation.core.impl;

import lombok.AllArgsConstructor;
import com.zero.admin.base.core.service.DeptService;
import com.zero.admin.base.translation.annotation.TranslationType;
import com.zero.admin.base.translation.constant.TransConstant;
import com.zero.admin.base.translation.core.TranslationInterface;

/**
 * 部门翻译实现
 *
 * @author Akai
 */
@AllArgsConstructor
@TranslationType(type = TransConstant.DEPT_ID_TO_NAME)
public class DeptNameTranslationImpl implements TranslationInterface<String> {

    private final DeptService deptService;

    @Override
    public String translation(Object key, String other) {
        if (key instanceof String ids) {
            return deptService.selectDeptNameByIds(ids);
        } else if (key instanceof Long id) {
            return deptService.selectDeptNameByIds(id.toString());
        }
        return null;
    }
}
