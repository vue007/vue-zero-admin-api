package com.zero.admin.base.shiro.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.zero.admin.base.core.constant.HttpStatus;
import com.zero.admin.base.core.domain.R;

/**
 * Shiro 异常处理器。
 *
 * @author Akai
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ShiroExceptionHandler {

    /**
     * 未登录（注解校验触发）。
     */
    @ExceptionHandler(UnauthenticatedException.class)
    public R<Void> handleUnauthenticatedException(UnauthenticatedException e, HttpServletRequest request) {
        log.debug("请求地址'{}',认证失败'{}',无法访问系统资源", request.getRequestURI(), e.getMessage());
        return R.fail(HttpStatus.UNAUTHORIZED, "认证失败，无法访问系统资源");
    }

    /**
     * 权限不足（含角色/权限注解校验失败）。
     */
    @ExceptionHandler(AuthorizationException.class)
    public R<Void> handleAuthorizationException(AuthorizationException e, HttpServletRequest request) {
        log.error("请求地址'{}',权限校验失败'{}'", request.getRequestURI(), e.getMessage());
        return R.fail(HttpStatus.FORBIDDEN, "没有访问权限，请联系管理员授权");
    }

    /**
     * 认证异常（Realm 认证流程抛出）。
     */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        log.error("请求地址'{}',认证失败'{}',无法访问系统资源", request.getRequestURI(), e.getMessage());
        return R.fail(HttpStatus.UNAUTHORIZED, "认证失败，无法访问系统资源");
    }

}
