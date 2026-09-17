package com.zero.admin.community.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 发布版本的设备兼容范围。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_asset_compatibility")
public class CommunityAssetCompatibility extends TenantEntity {
    @TableId("compatibility_id")
    private Long compatibilityId;
    private Long applicationId;
    private Long releaseId;
    private String platformCode;
    private String productCode;
    private String deviceType;
    private String capabilityVersion;
    @TableLogic
    private String delFlag;
}
