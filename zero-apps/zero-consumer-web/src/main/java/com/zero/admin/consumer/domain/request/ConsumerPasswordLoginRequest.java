package com.zero.admin.consumer.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** C 端密码登录请求。 */
@Data
public class ConsumerPasswordLoginRequest {

    @NotBlank(message = "租户编号不能为空")
    private String tenantId;

    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    @NotBlank(message = "会员账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
