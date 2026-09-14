package com.zero.admin.tenantapp.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

/**
 * 租户 App 接入应用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_application")
public class TenantApplication extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    private String appName;

    /** 全局唯一、可公开的应用标识。 */
    private String appId;

    /** BCrypt 摘要，永不通过管理接口返回。 */
    @JsonIgnore
    private String secretHash;

    private String appType;

    /** 逗号分隔的 scope 标识。 */
    private String scopeCodes;

    /** 0 正常，1 停用。 */
    private String status;

    private Date lastAccessTime;

    private Date secretRotatedTime;

    @TableLogic
    private String delFlag;

    private String remark;
}
