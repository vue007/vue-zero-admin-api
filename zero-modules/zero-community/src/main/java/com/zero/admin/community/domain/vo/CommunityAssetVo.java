package com.zero.admin.community.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/** 社区内容视图。详情接口会包含 payload，列表接口可不返回。 */
@Data
public class CommunityAssetVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long assetId;
    private String tenantId;
    private Long applicationId;
    private Long ownerMemberId;
    private String ownerName;
    private String ownerAvatar;
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
    private Boolean favorited;
    private Date publishedAt;
    private String rejectReason;
    private Date createTime;
    private Date updateTime;

    private Integer versionNo;
    private Integer schemaVersion;
    private String sourcePlatformCode;
    private String sourceProductCode;
    private String sourceCapabilityVersion;
    private Object payload;
    private String payloadHash;
    private Integer payloadSize;
    private String changelog;
}
