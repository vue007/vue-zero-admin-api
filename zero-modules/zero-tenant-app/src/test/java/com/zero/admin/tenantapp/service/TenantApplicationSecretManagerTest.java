package com.zero.admin.tenantapp.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TenantApplicationSecretManagerTest {

    private final TenantApplicationSecretManager secretManager =
        new TenantApplicationSecretManager();

    @Test
    void generatedApplicationIdsAreGloballyDistinctOpaqueIdentifiers() {
        Set<String> ids = new HashSet<>();
        for (int index = 0; index < 100; index++) {
            String appId = secretManager.generateAppId();
            assertTrue(appId.matches("app_[0-9a-f]{32}"));
            assertTrue(ids.add(appId));
        }
    }

    @Test
    void generatedSecretIsStoredAsSaltedBcryptHash() {
        String secret = secretManager.generateSecret();
        String firstHash = secretManager.hashSecret(secret);
        String secondHash = secretManager.hashSecret(secret);

        assertTrue(secret.matches("sk_[A-Za-z0-9_-]{43}"));
        assertNotEquals(secret, firstHash);
        assertNotEquals(firstHash, secondHash);
        assertTrue(secretManager.matches(secret, firstHash));
        assertFalse(secretManager.matches("wrong-secret", firstHash));
    }

    @Test
    void malformedOrMissingCredentialMaterialIsRejected() {
        assertFalse(secretManager.matches(null, "$2a$12$invalid"));
        assertFalse(secretManager.matches("secret", null));
        assertFalse(secretManager.matches("secret", "plain-text"));
    }
}
