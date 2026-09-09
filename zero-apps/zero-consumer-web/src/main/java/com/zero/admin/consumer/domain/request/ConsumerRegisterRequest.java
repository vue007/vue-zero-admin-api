package com.zero.admin.consumer.domain.request;

import com.zero.admin.member.domain.bo.MemberRegisterBo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** C 端密码注册请求。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConsumerRegisterRequest extends MemberRegisterBo {

    @NotBlank(message = "租户编号不能为空")
    private String tenantId;

    @NotBlank(message = "客户端ID不能为空")
    private String clientId;
}
