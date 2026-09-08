package com.zero.admin.member.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/** 后台修改会员状态。 */
@Data
public class MemberStatusBo {

    @NotNull(message = "会员ID不能为空")
    private Long memberId;

    @NotBlank(message = "会员状态不能为空")
    @Pattern(regexp = "[01]", message = "会员状态只能为0或1")
    private String status;
}
