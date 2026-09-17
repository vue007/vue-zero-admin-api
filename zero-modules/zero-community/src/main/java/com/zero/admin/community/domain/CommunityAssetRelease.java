package com.zero.admin.community.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import com.zero.admin.community.mybatis.JsonbStringTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/** 社区配置不可变版本。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "community_asset_release", autoResultMap = true)
public class CommunityAssetRelease extends TenantEntity {

    @TableId("release_id")
    private Long releaseId;
    private Long applicationId;
    private Long assetId;
    private Integer versionNo;
    private Integer schemaVersion;
    private String sourcePlatformCode;
    private String sourceProductCode;
    private String sourceCapabilityVersion;
    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String payload;
    private String payloadHash;
    private Integer payloadSize;
    private String changelog;
    private Date publishedAt;
    @TableLogic
    private String delFlag;
}
