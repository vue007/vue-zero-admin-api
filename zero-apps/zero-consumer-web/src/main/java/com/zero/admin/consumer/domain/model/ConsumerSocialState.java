package com.zero.admin.consumer.domain.model;

/** OAuth state 中保存的 C 端应用上下文。回调时会重新校验应用与租户状态。 */
public record ConsumerSocialState(
    String appId,
    String channel,
    String source,
    String nonce
) {
}
