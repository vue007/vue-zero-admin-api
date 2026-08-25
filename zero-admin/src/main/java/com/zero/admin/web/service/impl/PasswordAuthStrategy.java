package com.zero.admin.web.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
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
import com.zero.admin.base.shiro.utils.LoginHelper;
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
            loginService.checkLogin(LoginType.PASSWORD, tenantId, username,
                () -> !matchesPassword(password, user.getPassword(), username));
            // 此处可根据登录用户的数据不同 自行创建 loginUser
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

    /**
     * 校验 BCrypt 密码。历史脏数据或被误写入的明文密码按密码错误处理，
     * 避免 jBCrypt 抛出 IllegalArgumentException 导致登录接口返回 500。
     */
    private boolean matchesPassword(String rawPassword, String encodedPassword, String username) {
        if (StringUtils.isBlank(encodedPassword)
            || encodedPassword.length() != 60
            || !encodedPassword.startsWith("$2a$")) {
            log.warn("登录用户：{} 的密码摘要格式无效，请管理员重置密码.", username);
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException exception) {
            log.warn("登录用户：{} 的密码摘要无法校验，请管理员重置密码.", username);
            return false;
        }
    }

}
