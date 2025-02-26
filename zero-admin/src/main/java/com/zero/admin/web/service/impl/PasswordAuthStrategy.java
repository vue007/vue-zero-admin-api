package com.zero.admin.web.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.zero.admin.base.core.constant.Constants;
import com.zero.admin.base.core.constant.GlobalConstants;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.domain.model.LoginUser;
import com.zero.admin.base.core.domain.model.PasswordLoginBody;
import com.zero.admin.base.core.enums.LoginType;
import com.zero.admin.base.core.exception.user.CaptchaException;
import com.zero.admin.base.core.exception.user.CaptchaExpireException;
import com.zero.admin.base.core.exception.user.UserException;
import com.zero.admin.base.core.utils.MessageUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.core.utils.ValidatorUtils;
import com.zero.admin.base.json.utils.JsonUtils;
import com.zero.admin.base.redis.utils.RedisUtils;
import com.zero.admin.base.satoken.utils.LoginHelper;
import com.zero.admin.base.tenant.helper.TenantHelper;
import com.zero.admin.base.web.config.properties.CaptchaProperties;
import com.zero.admin.system.domain.SysUser;
import com.zero.admin.system.domain.vo.SysClientVo;
import com.zero.admin.system.domain.vo.SysUserVo;
import com.zero.admin.system.mapper.SysUserMapper;
import com.zero.admin.web.domain.vo.LoginVo;
import com.zero.admin.web.service.IAuthStrategy;
import com.zero.admin.web.service.SysLoginService;
import org.springframework.stereotype.Service;

/**
 * 密码认证策略
 *
 * @author Akai
 */
@Slf4j
@Service("password" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class PasswordAuthStrategy implements IAuthStrategy {

    private final CaptchaProperties captchaProperties;
    private final SysLoginService loginService;
    private final SysUserMapper userMapper;

    @Override
    public LoginVo login(String body, SysClientVo client) {
        PasswordLoginBody loginBody = JsonUtils.parseObject(body, PasswordLoginBody.class);
        ValidatorUtils.validate(loginBody);
        String tenantId = loginBody.getTenantId();
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String code = loginBody.getCode();
        String uuid = loginBody.getUuid();

        boolean captchaEnabled = captchaProperties.getEnable();
        // 验证码开关
        if (captchaEnabled) {
            validateCaptcha(tenantId, username, code, uuid);
        }
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            SysUserVo user = loadUserByUsername(username);
            loginService.checkLogin(LoginType.PASSWORD, tenantId, username, () -> !BCrypt.checkpw(password, user.getPassword()));
            // 此处可根据登录用户的数据不同 自行创建 loginUser
            return loginService.buildLoginUser(user);
        });
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        SaLoginModel model = new SaLoginModel();
        model.setDevice(client.getDeviceType());
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        // 生成token
        LoginHelper.login(loginUser, model);

        LoginVo loginVo = new LoginVo();
        String tk = StpUtil.getTokenValue();
        loginVo.setAccessToken(tk);
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        return loginVo;
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    private void validateCaptcha(String tenantId, String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    private SysUserVo loadUserByUsername(String username) {
        SysUserVo user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, username));
        if (ObjectUtil.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new UserException("user.not.exists", username);
        } else if (SystemConstants.DISABLE.equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new UserException("user.blocked", username);
        }
        return user;
    }

}
