package com.zero.admin.consumer.domain.model;

/** OAuth state 中保存的可信 C 端租户上下文。state 由 JustAuth 缓存并在回调时校验。 */
public record ConsumerSocialState(
    String tenantId,
    String clientId,
    String source,
    String nonce
) {
}
