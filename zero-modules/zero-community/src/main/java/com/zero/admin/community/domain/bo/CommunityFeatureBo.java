package com.zero.admin.community.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommunityFeatureBo {
    @NotNull(message = "精选状态不能为空")
    private Boolean featured;
}
