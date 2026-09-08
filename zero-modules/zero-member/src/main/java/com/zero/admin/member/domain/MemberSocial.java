package com.zero.admin.member.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 会员第三方身份绑定。只保存识别身份所需字段，不持久化第三方 access token。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_member_social")
public class MemberSocial extends TenantEntity {

    @TableId("social_id")
    private Long socialId;

    private Long memberId;

    private String authId;

    private String source;

    private String openId;

    private String unionId;

    private String username;

    private String nickname;

    private String avatar;

    @TableLogic
    private String delFlag;
}
