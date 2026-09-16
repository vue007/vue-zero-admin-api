package com.zero.admin.tenantapp.service.impl;

import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.tenantapp.domain.TenantApplication;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationClientAuthVo;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationClientVo;
import com.zero.admin.tenantapp.mapper.TenantApplicationClientMapper;
import com.zero.admin.tenantapp.mapper.TenantApplicationMapper;
import com.zero.admin.tenantapp.service.TenantApplicationSecretManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantApplicationServiceImplTest {

    @Mock
    private TenantApplicationMapper applicationMapper;

    @Mock
    private TenantApplicationClientMapper applicationClientMapper;

    @Mock
    private TenantApplicationSecretManager secretManager;

    private TenantApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TenantApplicationServiceImpl(
            applicationMapper, applicationClientMapper, secretManager);
    }

    @Test
    void resolvesEnabledClientAndNormalizesPublicChannel() {
        TenantApplication application = enabledApplication();
        TenantApplicationClientVo terminal = enabledTerminal();
        when(applicationMapper.selectOne(any())).thenReturn(application);
        when(applicationMapper.tenantAvailable("000001")).thenReturn(true);
        when(applicationClientMapper.selectByApplicationAndChannel(
            100L, "000001", "miniapp")).thenReturn(terminal);

        TenantApplicationClientAuthVo result =
            service.resolveEnabledClient(" app_public ", " MiniApp ");

        assertEquals(100L, result.getApplicationId());
        assertEquals(2L, result.getAuthClientId());
        assertEquals("internal-client-id", result.getClientId());
        assertEquals("miniapp", result.getChannel());
        assertEquals(java.util.List.of("app:member", "app:partner"), result.getScopes());
        verify(applicationClientMapper).selectByApplicationAndChannel(
            100L, "000001", "miniapp");
    }

    @Test
    void rejectsDisabledBindingOrAuthenticationClient() {
        TenantApplication application = enabledApplication();
        TenantApplicationClientVo terminal = enabledTerminal();
        terminal.setClientStatus(SystemConstants.DISABLE);
        when(applicationMapper.selectOne(any())).thenReturn(application);
        when(applicationMapper.tenantAvailable("000001")).thenReturn(true);
        when(applicationClientMapper.selectByApplicationAndChannel(
            100L, "000001", "app")).thenReturn(terminal);

        assertThrows(ServiceException.class,
            () -> service.resolveEnabledClient("app_public", "app"));
    }

    private TenantApplication enabledApplication() {
        TenantApplication application = new TenantApplication();
        application.setId(100L);
        application.setTenantId("000001");
        application.setAppId("app_public");
        application.setScopeCodes("app:member,app:partner");
        application.setStatus(SystemConstants.NORMAL);
        return application;
    }

    private TenantApplicationClientVo enabledTerminal() {
        TenantApplicationClientVo terminal = new TenantApplicationClientVo();
        terminal.setApplicationId(100L);
        terminal.setAuthClientId(2L);
        terminal.setChannel("miniapp");
        terminal.setStatus(SystemConstants.NORMAL);
        terminal.setClientId("internal-client-id");
        terminal.setClientKey("app");
        terminal.setDeviceType("miniapp");
        terminal.setGrantType("password,social");
        terminal.setTimeout(604800L);
        terminal.setActiveTimeout(1800L);
        terminal.setClientStatus(SystemConstants.NORMAL);
        return terminal;
    }
}
