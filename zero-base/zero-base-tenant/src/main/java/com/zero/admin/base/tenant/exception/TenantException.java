package com.zero.admin.base.tenant.exception;

import com.zero.admin.base.core.exception.base.BaseException;

import java.io.Serial;

/**
 * 租户异常类
 *
 * @author Akai
 */
public class TenantException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TenantException(String code, Object... args) {
        super("tenant", code, args, null);
    }
}
