package com.zero.admin.domain;

import com.zero.admin.domain.utils.StringUtils;

public class BaseController {

    /**
     * 响应返回结果
     *
     * @param rows 影响行数
     * @return 操作结果
     */
    protected Rt<Void> toAjax(int rows) {
        return rows > 0 ? Rt.ok() : Rt.fail();
    }

    /**
     * 响应返回结果
     *
     * @param result 结果
     * @return 操作结果
     */
    protected Rt<Void> toAjax(boolean result) {
        return result ? Rt.ok() : Rt.fail();
    }

    /**
     * 页面跳转
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }

}
