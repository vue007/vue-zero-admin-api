package com.zero.admin.base.satoken.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import com.zero.admin.base.core.factory.YmlPropertySourceFactory;
import com.zero.admin.base.satoken.core.dao.PlusSaTokenDao;
import com.zero.admin.base.satoken.core.service.SaPermissionImpl;
import com.zero.admin.base.satoken.handler.SaTokenExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * sa-token 配置
 *
 * @author Akai
 */
@AutoConfiguration
@PropertySource(value = "classpath:base-satoken.yml", factory = YmlPropertySourceFactory.class)
public class SaTokenConfig {

    @Bean
    public StpLogic getStpLogicJwt() {
        System.out.println("Sa-Token整合JWT模式启用成功");
        // Sa-Token 整合 jwt (简单模式)
        return new StpLogicJwtForSimple();
    }

    /**
     * 权限接口实现(使用bean注入方便用户替换)
     */
    @Bean
    public StpInterface stpInterface() {
        return new SaPermissionImpl();
    }

    /**
     * 自定义dao层存储
     */
    @Bean
    public SaTokenDao saTokenDao() {
        return new PlusSaTokenDao();
    }

    /**
     * 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }

}
