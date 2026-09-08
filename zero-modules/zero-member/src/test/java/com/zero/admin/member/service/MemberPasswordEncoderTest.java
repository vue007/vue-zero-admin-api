package com.zero.admin.member.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MemberPasswordEncoderTest {

    private final MemberPasswordEncoder encoder = new MemberPasswordEncoder();

    @Test
    void shouldEncodeAndVerifyPassword() {
        String encoded = encoder.encode("consumer-password");

        assertNotEquals("consumer-password", encoded);
        assertTrue(encoder.matches("consumer-password", encoded));
        assertFalse(encoder.matches("wrong-password", encoded));
    }

    @Test
    void shouldRejectMissingOrMalformedHash() {
        assertFalse(encoder.matches("consumer-password", null));
        assertFalse(encoder.matches("consumer-password", "plain-text"));
        assertFalse(encoder.matches(null, "$2a$10$invalid"));
    }
}
