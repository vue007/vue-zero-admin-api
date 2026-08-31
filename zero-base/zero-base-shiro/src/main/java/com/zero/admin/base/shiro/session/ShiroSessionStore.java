package com.zero.admin.base.shiro.session;

import org.apache.shiro.session.Session;
import org.springframework.util.SerializationUtils;
import com.zero.admin.base.core.constant.GlobalConstants;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.redis.utils.RedisUtils;

import java.io.Serializable;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Shiro 会话的 Redis 持久化存储。
 * <p>
 * 会话对象使用 JDK 原生序列化（{@link SimpleSession} 本身即 Serializable），
 * 以 Base64 字符串形式复用项目统一的 RedisUtils 存储，避免与 Redisson 的
 * JSON 编解码器在反序列化复杂会话结构时产生类型丢失问题。
 *
 * @author Akai
 */
public final class ShiroSessionStore {

    // 使用全局 key 前缀，避免多租户前缀处理器在会话读写时递归解析租户。
    private static final String SESSION_KEY_PREFIX = GlobalConstants.GLOBAL_REDIS_KEY + "shiro:session:";

    private ShiroSessionStore() {
    }

    public static void save(Session session) {
        String value = Base64.getEncoder()
            .encodeToString(SerializationUtils.serialize(session));
        long timeout = session.getTimeout();
        if (timeout > 0) {
            RedisUtils.setCacheObject(key(session.getId()), value, Duration.ofMillis(timeout));
        } else {
            RedisUtils.setCacheObject(key(session.getId()), value);
        }
    }

    public static Session read(Serializable sessionId) {
        String value = RedisUtils.getCacheObject(key(sessionId));
        if (StringUtils.isBlank(value)) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(value);
        return (Session) SerializationUtils.deserialize(data);
    }

    public static void delete(Serializable sessionId) {
        RedisUtils.deleteObject(key(sessionId));
    }

    public static String key(Serializable sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    /**
     * 获取当前所有活跃会话的 ID 列表（用于踢下线等管理操作）。
     */
    public static Collection<Serializable> activeSessionIds() {
        Collection<String> keys = RedisUtils.keys(SESSION_KEY_PREFIX + "*");
        return keys.stream()
            .map(key -> (Serializable) key.substring(SESSION_KEY_PREFIX.length()))
            .collect(Collectors.toList());
    }

}
