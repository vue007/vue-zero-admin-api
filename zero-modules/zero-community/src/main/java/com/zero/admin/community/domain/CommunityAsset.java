package com.zero.admin.community.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/** 社区设备配置聚合根。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_asset")
public class CommunityAsset extends TenantEntity {

    @TableId("asset_id")
    private Long assetId;
    private Long applicationId;
    private Long ownerMemberId;
    private String title;
    private String summary;
    private String coverUrl;
    private String kind;
    private String deviceType;
    private String visibility;
    private String status;
    private Long currentReleaseId;
    private String featured;
    private Long favoriteCount;
    private Long downloadCount;
    private Date publishedAt;
    private String rejectReason;
    @TableLogic
    private String delFlag;
}
