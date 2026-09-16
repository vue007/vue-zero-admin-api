package com.zero.admin.consumer.domain.request;

import com.zero.admin.member.domain.bo.MemberRegisterBo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端密码注册请求。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumerRegisterRequest extends MemberRegisterBo {

    @NotBlank(message = "应用ID不能为空")
    @Schema(
        description = "平台生成的公开应用ID，对应 app_application.app_id；后端据此解析租户",
        example = "app_aabbccddeeff00112233445566778899"
    )
    private String appId;

    @NotBlank(message = "终端渠道不能为空")
    @Pattern(regexp = "[A-Za-z][A-Za-z0-9_-]{0,31}", message = "终端渠道格式不正确")
    @Schema(description = "App 中配置的公开终端渠道码", example = "app")
    private String channel;
}
