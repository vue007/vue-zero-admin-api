package com.zero.admin.consumer.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** C 端 OAuth/开放平台登录请求。 */
@Data
public class ConsumerSocialLoginRequest {

    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    @NotBlank(message = "第三方登录来源不能为空")
    private String source;

    @NotBlank(message = "第三方授权码不能为空")
    private String code;

    @NotBlank(message = "第三方授权state不能为空")
    private String state;
}
