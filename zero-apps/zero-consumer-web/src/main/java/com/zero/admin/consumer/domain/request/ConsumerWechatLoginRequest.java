package com.zero.admin.consumer.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 微信小程序 wx.login 登录请求。 */
@Data
public class ConsumerWechatLoginRequest {

    @NotBlank(message = "客户端ID不能为空")
    private String clientId;

    @NotBlank(message = "微信小程序appId不能为空")
    private String appId;

    @NotBlank(message = "微信登录code不能为空")
    private String code;
}
