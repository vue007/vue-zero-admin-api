package com.zero.admin.web.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import com.zero.admin.system.domain.vo.SysTenantVo;

/**
 * 租户列表
 *
 * @author Akai
 */
@Data
@AutoMapper(target = SysTenantVo.class)
public class TenantListVo {

    /**
     * 租户编号
     */
    private String tenantId;

    /**
     * 企业名称
     */
    private String companyName;

    /**
     * 域名
     */
    private String domain;

}
