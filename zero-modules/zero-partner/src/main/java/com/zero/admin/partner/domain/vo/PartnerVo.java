package com.zero.admin.partner.domain.vo;

import com.zero.admin.partner.domain.Partner;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/** 合作客户视图。 */
@Data
@AutoMapper(target = Partner.class)
public class PartnerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long partnerId;
    private String tenantId;
    private String partnerCode;
    private String partnerName;
    private String creditCode;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String status;
    private String remark;
    private Date createTime;
    private Date updateTime;
}
