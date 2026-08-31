package com.zero.admin.base.shiro.web;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.web.filter.authc.AuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

import java.nio.charset.StandardCharsets;

/**
 * REST 风格认证过滤器。
 * <p>
 * 未登录请求直接返回 JSON 401，而不是像默认 form 过滤器那样重定向到登录页。
 * 过滤器在 Shiro 过滤链中以 {@code rest} 名称引用。
 *
 * @author Akai
 */
public class RestAuthFilter extends AuthenticationFilter {

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_OK);
        httpResponse.setContentType("application/json;charset=UTF-8");
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpResponse.getWriter().write("{\"code\":401,\"msg\":\"认证失败，无法访问系统资源\"}");
        return false;
    }

}
