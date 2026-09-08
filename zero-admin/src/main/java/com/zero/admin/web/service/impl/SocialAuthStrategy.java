package com.zero.admin.web.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.domain.model.SocialLoginBody;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.exception.user.UserException;
import com.zero.admin.base.core.utils.ValidatorUtils;
import com.zero.admin.base.json.utils.JsonUtils;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.base.social.config.properties.SocialProperties;
import com.zero.admin.base.social.utils.SocialUtils;
import com.zero.admin.base.tenant.helper.TenantHelper;
import com.zero.admin.system.domain.vo.SysClientVo;
import com.zero.admin.system.domain.vo.SysSocialVo;
import com.zero.admin.system.domain.vo.SysUserVo;
import com.zero.admin.system.mapper.SysUserMapper;
import com.zero.admin.system.service.ISysSocialService;
import com.zero.admin.web.domain.vo.LoginVo;
import com.zero.admin.web.service.IAuthStrategy;
import com.zero.admin.web.service.SysLoginService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 第三方授权策略
 *
 * @author Akai
 */
@Slf4j
@Service("social" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class SocialAuthStrategy implements IAuthStrategy {

    private final SocialProperties socialProperties;
    private final ISysSocialService sysSocialService;
    private final SysUserMapper userMapper;
    private final SysLoginService loginService;

    /**
     * 登录-第三方授权登录
     *
     * @param body     登录信息
     * @param client   客户端信息
     */
    @Override
    public LoginVo login(String body, SysClientVo client) {
        SocialLoginBody loginBody = JsonUtils.parseObject(body, SocialLoginBody.class);
        ValidatorUtils.validate(loginBody);
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
                loginBody.getSource(), loginBody.getSocialCode(),
                loginBody.getSocialState(), socialProperties);
        if (!response.ok()) {
            throw new ServiceException(response.getMsg());
        }
        AuthUser authUserData = response.getData();
        String authId = authUserData.getSource() + authUserData.getUuid();
        // 登录阶段尚未建立 Shiro 会话，显式使用已校验的登录租户建立临时上下文，
        // 确保授权关系查询从 SQL 层就限定在目标租户内。
        List<SysSocialVo> list = TenantHelper.dynamic(loginBody.getTenantId(),
            () -> sysSocialService.selectByAuthId(authId));
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("你还没有绑定第三方账号，绑定后才可以登录！");
        }
        SysSocialVo social = list.get(0);
        LoginUser loginUser = TenantHelper.dynamic(social.getTenantId(), () -> {
            SysUserVo user = loadUser(social.getUserId());
            // 此处可根据登录用户的数据不同 自行创建 loginUser 属性不够用继承扩展就行了
            return loginService.buildLoginUser(user);
        });
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        // 生成token
        LoginHelper.login(loginUser, client.getTimeout());
        String token = LoginHelper.getToken();
        loginService.recordLoginSuccess(loginUser, token);

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(token);
        loginVo.setExpireIn(LoginHelper.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        return loginVo;
    }

    private SysUserVo loadUser(Long userId) {
        SysUserVo user = userMapper.selectVoById(userId);
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", "");
            throw new UserException("user.not.exists", "");
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", "");
            throw new UserException("user.blocked", "");
        }
        return user;
    }

}
