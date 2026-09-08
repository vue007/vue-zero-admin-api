package com.zero.admin.consumer.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/** C 端登录结果。 */
@Value
@Builder
public class ConsumerLoginVo {

    @JsonProperty("access_token")
    String accessToken;

    @JsonProperty("expire_in")
    Long expireIn;

    @JsonProperty("client_id")
    String clientId;

    ConsumerMemberProfileVo member;
}
