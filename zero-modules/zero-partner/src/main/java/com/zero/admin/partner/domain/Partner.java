package com.zero.admin.partner.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 租户维护的合作客户（商户）。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_partner")
public class Partner extends TenantEntity {

    @TableId("partner_id")
    private Long partnerId;

    private String partnerCode;

    private String partnerName;

    private String creditCode;

    private String contactName;

    private String contactPhone;

    private String contactEmail;

    private String address;

    /** 0 正常，1 停用。 */
    private String status;

    @TableLogic
    private String delFlag;

    private String remark;
}
