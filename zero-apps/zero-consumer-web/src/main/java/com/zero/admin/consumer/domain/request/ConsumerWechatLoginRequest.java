package com.zero.admin.consumer.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 微信小程序 wx.login 登录请求。 */
@Data
public class ConsumerWechatLoginRequest {

    @NotBlank(message = "应用ID不能为空")
    @Schema(
        description = "平台生成的公开应用ID；微信小程序AppID由该应用的服务端配置绑定",
        example = "app_aabbccddeeff00112233445566778899"
    )
    private String appId;

    @NotBlank(message = "微信登录code不能为空")
    private String code;
}
