package com.zero.admin.base.shiro.session;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import com.zero.admin.base.core.utils.StringUtils;

import java.io.Serializable;

/**
 * 从 {@code Authorization: Bearer <token>} 请求头读取会话 ID 的会话管理器。
 * <p>
 * 前后端分离场景下前端不携带 Cookie，会话 ID 即登录时返回的 access_token，
 * 直接作为 Shiro 会话的 sessionId 存储在 Redis 中。
 *
 * @author Akai
 */
public class TokenWebSessionManager extends DefaultWebSessionManager {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    public TokenWebSessionManager() {
        setSessionIdUrlRewritingEnabled(false);
        setDeleteInvalidSessions(true);
    }

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        String authorization = WebUtils.toHttp(request).getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isNotBlank(authorization) && authorization.startsWith(TOKEN_PREFIX)) {
            String token = authorization.substring(TOKEN_PREFIX.length()).trim();
            if (StringUtils.isNotBlank(token)) {
                return token;
            }
        }
        return super.getSessionId(request, response);
    }

}
