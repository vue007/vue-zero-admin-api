package com.zero.admin.consumer.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** C 端密码登录请求。 */
@Data
public class ConsumerPasswordLoginRequest {

    @NotBlank(message = "应用ID不能为空")
    @Schema(
        description = "平台生成的公开应用ID，对应 app_application.app_id；后端据此解析租户",
        example = "app_aabbccddeeff00112233445566778899"
    )
    private String appId;

    @NotBlank(message = "会员账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
