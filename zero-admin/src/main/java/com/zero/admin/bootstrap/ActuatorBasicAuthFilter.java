package com.zero.admin.bootstrap;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 使用独立 Basic Auth 凭据保护 Actuator，避免暴露运行环境、日志和线程信息。
 *
 * @author Akai
 */
public class ActuatorBasicAuthFilter implements Filter {

    private final byte[] username;
    private final byte[] password;

    public ActuatorBasicAuthFilter(String username, String password) {
        this.username = username.getBytes(StandardCharsets.UTF_8);
        this.password = password.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String authorization = httpRequest.getHeader("Authorization");

        if (!isAuthorized(authorization)) {
            httpResponse.setHeader("WWW-Authenticate", "Basic realm=\"actuator\"");
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isAuthorized(String authorization) {
        if (authorization == null || !authorization.startsWith("Basic ")) {
            return false;
        }
        try {
            String credentials = new String(
                Base64.getDecoder().decode(authorization.substring(6)), StandardCharsets.UTF_8);
            String[] pair = credentials.split(":", 2);
            return pair.length == 2
                && MessageDigest.isEqual(username, pair[0].getBytes(StandardCharsets.UTF_8))
                && MessageDigest.isEqual(password, pair[1].getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
