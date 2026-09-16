package com.zero.admin.tenantapp.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zero.admin.base.tenant.core.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * App 与认证客户端的终端渠道绑定。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_application_client")
public class TenantApplicationClient extends TenantEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId("id")
    private Long id;

    private Long applicationId;

    /** sys_client.id。 */
    private Long authClientId;

    /** App 对外公开的终端渠道码，例如 app、h5、miniapp。 */
    private String channel;

    /** 0 正常，1 停用。 */
    private String status;

    @TableLogic
    private String delFlag;
}
