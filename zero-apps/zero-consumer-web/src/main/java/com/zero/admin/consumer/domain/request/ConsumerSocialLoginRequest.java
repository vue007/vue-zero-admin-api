package com.zero.admin.consumer.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** C 端 OAuth/开放平台登录请求。 */
@Data
public class ConsumerSocialLoginRequest {

    @NotBlank(message = "应用ID不能为空")
    @Schema(
        description = "平台生成的公开应用ID，对应 app_application.app_id；必须与授权state一致",
        example = "app_aabbccddeeff00112233445566778899"
    )
    private String appId;

    @NotBlank(message = "第三方登录来源不能为空")
    private String source;

    @NotBlank(message = "第三方授权码不能为空")
    private String code;

    @NotBlank(message = "第三方授权state不能为空")
    private String state;
}
