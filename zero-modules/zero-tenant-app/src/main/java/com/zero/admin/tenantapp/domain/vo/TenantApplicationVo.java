package com.zero.admin.tenantapp.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zero.admin.tenantapp.domain.TenantApplication;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/** 租户 App 接入视图，不包含任何密钥材料。 */
@Data
@AutoMapper(target = TenantApplication.class)
public class TenantApplicationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantId;
    private String tenantName;
    private String appName;
    private String appId;
    @JsonIgnore
    private String scopeCodes;
    private List<String> scopes;
    private List<TenantApplicationClientVo> terminals;
    private String status;
    private String remark;
    private Date lastAccessTime;
    private Date secretRotatedTime;
    private Date createTime;
    private Date updateTime;
}
