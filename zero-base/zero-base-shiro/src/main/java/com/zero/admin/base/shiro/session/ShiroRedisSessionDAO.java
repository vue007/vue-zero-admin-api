package com.zero.admin.base.shiro.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * 基于 Redis 的 Shiro 会话 DAO。
 *
 * @author Akai
 */
public class ShiroRedisSessionDAO extends AbstractSessionDAO {

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        ShiroSessionStore.save(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        return ShiroSessionStore.read(sessionId);
    }

    @Override
    public void update(Session session) {
        ShiroSessionStore.save(session);
    }

    @Override
    public void delete(Session session) {
        ShiroSessionStore.delete(session.getId());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        return Collections.emptyList();
    }

}
