package com.zero.admin.tenantapp.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zero.admin.tenantapp.domain.TenantApplicationClient;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/** App 终端认证策略绑定视图。 */
@Data
@AutoMapper(target = TenantApplicationClient.class)
public class TenantApplicationClientVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String tenantId;
    private Long applicationId;
    private Long authClientId;
    private String channel;
    private String status;

    @JsonIgnore
    private String clientId;
    private String clientKey;
    private String deviceType;
    private String grantType;
    private List<String> grantTypeList;
    private Long timeout;
    private Long activeTimeout;
    private String clientStatus;

    private Date createTime;
    private Date updateTime;
}
