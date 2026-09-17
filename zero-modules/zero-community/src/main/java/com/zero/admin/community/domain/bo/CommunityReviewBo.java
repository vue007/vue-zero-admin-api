package com.zero.admin.community.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 后台审核请求。action 仅允许 approve/reject。 */
@Data
public class CommunityReviewBo {
    @NotBlank(message = "审核动作不能为空")
    private String action;
    @Size(max = 500, message = "审核意见不能超过500个字符")
    private String reason;
}
