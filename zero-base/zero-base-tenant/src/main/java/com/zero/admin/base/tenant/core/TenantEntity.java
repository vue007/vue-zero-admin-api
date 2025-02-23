package com.zero.admin.base.tenant.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.zero.admin.base.mybatis.core.domain.BaseEntity;

/**
 * 租户基类
 *
 * @author Akai
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    /**
     * 租户编号
     */
    private String tenantId;

}
