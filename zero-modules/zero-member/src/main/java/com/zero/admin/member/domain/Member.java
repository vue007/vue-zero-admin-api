package com.zero.admin.member.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * C 端会员。会员身份与后台 {@code sys_user} 完全分离。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_member")
public class Member extends TenantEntity {

    @TableId("member_id")
    private Long memberId;

    private String username;

    private String nickname;

    private String mobile;

    private String email;

    private String avatar;

    @TableField(
        insertStrategy = FieldStrategy.NOT_EMPTY,
        updateStrategy = FieldStrategy.NOT_EMPTY,
        whereStrategy = FieldStrategy.NOT_EMPTY
    )
    private String password;

    /** 0 正常，1 停用。 */
    private String status;

    @TableLogic
    private String delFlag;

    private String registerSource;

    private String loginIp;

    private Date loginDate;

    private String remark;
}
