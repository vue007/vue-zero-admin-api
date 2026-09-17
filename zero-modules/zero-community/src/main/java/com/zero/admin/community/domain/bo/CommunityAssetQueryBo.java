package com.zero.admin.community.domain.bo;

import lombok.Data;

/** 社区内容查询条件。 */
@Data
public class CommunityAssetQueryBo {
    private String tenantId;
    private Long applicationId;
    private Long ownerMemberId;
    private String keyword;
    private String kind;
    private String deviceType;
    private String status;
    private String featured;
}
