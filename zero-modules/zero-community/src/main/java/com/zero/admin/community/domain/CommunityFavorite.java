package com.zero.admin.community.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 会员收藏关系。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_favorite")
public class CommunityFavorite extends TenantEntity {
    @TableId("favorite_id")
    private Long favoriteId;
    private Long applicationId;
    private Long assetId;
    private Long memberId;
    @TableLogic
    private String delFlag;
}
