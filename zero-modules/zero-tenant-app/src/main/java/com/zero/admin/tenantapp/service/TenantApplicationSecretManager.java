package com.zero.admin.tenantapp.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/** 生成并校验高熵 App 凭证。 */
@Component
public class TenantApplicationSecretManager {

    private static final int SECRET_BYTES = 32;
    private static final int BCRYPT_LOG_ROUNDS = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateAppId() {
        return "app_" + UUID.randomUUID().toString().replace("-", "");
    }

    public String generateSecret() {
        byte[] bytes = new byte[SECRET_BYTES];
        secureRandom.nextBytes(bytes);
        return "sk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashSecret(String rawSecret) {
        return BCrypt.hashpw(rawSecret, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }

    public boolean matches(String rawSecret, String secretHash) {
        if (rawSecret == null || secretHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawSecret, secretHash);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }
}
