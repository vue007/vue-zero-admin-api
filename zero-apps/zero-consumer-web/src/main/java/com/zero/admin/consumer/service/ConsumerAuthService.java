package com.zero.admin.consumer.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zero.admin.base.core.constant.SystemConstants;
import com.zero.admin.base.core.constant.TenantConstants;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.ServletUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.redis.utils.RedisUtils;
import com.zero.admin.base.shiro.utils.LoginHelper;
import com.zero.admin.base.social.config.properties.SocialLoginConfigProperties;
import com.zero.admin.base.social.config.properties.SocialProperties;
import com.zero.admin.base.social.utils.SocialUtils;
import com.zero.admin.consumer.config.ConsumerAuthProperties;
import com.zero.admin.consumer.config.ConsumerWechatProperties;
import com.zero.admin.consumer.domain.request.ConsumerPasswordLoginRequest;
import com.zero.admin.consumer.domain.request.ConsumerRegisterRequest;
import com.zero.admin.consumer.domain.request.ConsumerSocialLoginRequest;
import com.zero.admin.consumer.domain.request.ConsumerWechatLoginRequest;
import com.zero.admin.consumer.domain.vo.ConsumerLoginVo;
import com.zero.admin.consumer.domain.vo.ConsumerMemberProfileVo;
import com.zero.admin.member.domain.model.MemberLoginUser;
import com.zero.admin.member.domain.model.MemberSocialIdentity;
import com.zero.admin.member.domain.vo.MemberVo;
import com.zero.admin.member.service.IMemberService;
import com.zero.admin.system.domain.SysClient;
import com.zero.admin.system.domain.SysTenant;
import com.zero.admin.system.mapper.SysClientMapper;
import com.zero.admin.system.mapper.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthToken;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWechatMiniProgramRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.Duration;

/** C 端认证协议编排，不复用后台 sys_user 登录策略。 */
@Service
@RequiredArgsConstructor
public class ConsumerAuthService {

    private static final String PASSWORD_GRANT = "password";
    private static final String SOCIAL_GRANT = "social";

    private final ConsumerAuthProperties authProperties;
    private final ConsumerWechatProperties wechatProperties;
    private final SocialProperties socialProperties;
    private final IMemberService memberService;
    private final SysClientMapper clientMapper;
    private final SysTenantMapper tenantMapper;

    public ConsumerLoginVo register(ConsumerRegisterRequest request) {
        SysClient client = validateBoundary(request.getClientId(), PASSWORD_GRANT);
        MemberVo member = memberService.register(authProperties.getTenantId(), request);
        return issueSession(member, client, PASSWORD_GRANT);
    }

    public ConsumerLoginVo passwordLogin(ConsumerPasswordLoginRequest request) {
        SysClient client = validateBoundary(request.getClientId(), PASSWORD_GRANT);
        MemberVo member = authenticatePasswordWithRetryLimit(request);
        return issueSession(member, client, PASSWORD_GRANT);
    }

    public ConsumerLoginVo socialLogin(ConsumerSocialLoginRequest request) {
        SysClient client = validateBoundary(request.getClientId(), SOCIAL_GRANT);
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
            request.getSource(), request.getCode(), request.getState(), socialProperties);
        if (!response.ok() || response.getData() == null) {
            throw new ServiceException(StringUtils.blankToDefault(response.getMsg(), "第三方登录失败"));
        }
        AuthUser authUser = response.getData();
        AuthToken token = authUser.getToken();
        MemberSocialIdentity identity = MemberSocialIdentity.builder()
            .source(authUser.getSource())
            .openId(authUser.getUuid())
            .unionId(token == null ? null : token.getUnionId())
            .username(authUser.getUsername())
            .nickname(authUser.getNickname())
            .avatar(authUser.getAvatar())
            .build();
        MemberVo member = memberService.loginOrRegisterSocial(authProperties.getTenantId(), identity);
        return issueSession(member, client, request.getSource());
    }

    public ConsumerLoginVo wechatMiniProgramLogin(ConsumerWechatLoginRequest request) {
        SysClient client = validateBoundary(request.getClientId(), SOCIAL_GRANT);
        ConsumerWechatProperties.MiniProgram miniProgram =
            wechatProperties.getMiniPrograms().get(request.getAppId());
        if (miniProgram == null || StringUtils.isBlank(miniProgram.getSecret())) {
            throw new ServiceException("微信小程序未配置或未启用");
        }

        AuthRequest authRequest = new AuthWechatMiniProgramRequest(AuthConfig.builder()
            .clientId(request.getAppId())
            .clientSecret(miniProgram.getSecret())
            .ignoreCheckRedirectUri(true)
            .ignoreCheckState(true)
            .build());
        AuthCallback callback = new AuthCallback();
        callback.setCode(request.getCode());
        AuthResponse<AuthUser> response = authRequest.login(callback);
        if (!response.ok() || response.getData() == null || response.getData().getToken() == null) {
            throw new ServiceException(StringUtils.blankToDefault(response.getMsg(), "微信登录失败"));
        }
        AuthUser authUser = response.getData();
        AuthToken token = authUser.getToken();
        String openId = StringUtils.blankToDefault(token.getOpenId(), authUser.getUuid());
        MemberSocialIdentity identity = MemberSocialIdentity.builder()
            .source("wechat_mini_program@" + request.getAppId())
            .openId(openId)
            .unionId(token.getUnionId())
            .username(authUser.getUsername())
            .nickname(authUser.getNickname())
            .avatar(authUser.getAvatar())
            .build();
        MemberVo member = memberService.loginOrRegisterSocial(authProperties.getTenantId(), identity);
        return issueSession(member, client, "wechat_mini_program");
    }

    public String socialAuthorizeUrl(String clientId, String source) {
        validateBoundary(clientId, SOCIAL_GRANT);
        ensureSocialProviderConfigured(source);
        return SocialUtils.getAuthRequest(source, socialProperties).authorize(AuthStateUtils.createState());
    }

    public List<String> socialProviders() {
        if (CollUtil.isEmpty(socialProperties.getType())) {
            return List.of();
        }
        return socialProperties.getType().entrySet().stream()
            .filter(entry -> providerConfigured(entry.getValue()))
            .map(Map.Entry::getKey)
            .sorted()
            .toList();
    }

    public void logout() {
        LoginHelper.logout();
    }

    private ConsumerLoginVo issueSession(MemberVo member, SysClient client, String loginSource) {
        memberService.recordLogin(member.getTenantId(), member.getMemberId(), ServletUtils.getClientIP());

        MemberLoginUser loginUser = new MemberLoginUser();
        loginUser.setTenantId(member.getTenantId());
        loginUser.setUserId(member.getMemberId());
        loginUser.setUsername(member.getUsername());
        loginUser.setNickname(member.getNickname());
        loginUser.setUserType(UserType.APP_USER.getUserType());
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        loginUser.setLoginSource(loginSource);
        LoginHelper.login(loginUser, ObjectUtil.defaultIfNull(client.getTimeout(), 0L));

        return ConsumerLoginVo.builder()
            .accessToken(LoginHelper.getToken())
            .expireIn(LoginHelper.getTokenTimeout())
            .clientId(client.getClientId())
            .member(ConsumerMemberProfileVo.from(member))
            .build();
    }

    private MemberVo authenticatePasswordWithRetryLimit(ConsumerPasswordLoginRequest request) {
        String username = request.getUsername().strip().toLowerCase(Locale.ROOT);
        String retryKey = "consumer:password:error:"
            + authProperties.getTenantId() + ":" + username;
        int retryCount = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(retryKey), 0);
        if (retryCount >= authProperties.getMaxRetryCount()) {
            throw new ServiceException("密码错误次数过多，请稍后再试");
        }
        try {
            MemberVo member = memberService.authenticatePassword(
                authProperties.getTenantId(), username, request.getPassword());
            RedisUtils.deleteObject(retryKey);
            return member;
        } catch (ServiceException exception) {
            int nextRetryCount = retryCount + 1;
            RedisUtils.setCacheObject(retryKey, nextRetryCount,
                Duration.ofMinutes(authProperties.getLockMinutes()));
            if (nextRetryCount >= authProperties.getMaxRetryCount()) {
                throw new ServiceException("密码错误次数过多，请稍后再试");
            }
            throw exception;
        }
    }

    private SysClient validateBoundary(String clientId, String grantType) {
        validateTenant();
        SysClient client = clientMapper.selectOne(new LambdaQueryWrapper<SysClient>()
            .eq(SysClient::getClientId, clientId));
        if (client == null || !supportsGrant(client.getGrantType(), grantType)) {
            throw new ServiceException("客户端或授权类型无效");
        }
        if (!SystemConstants.NORMAL.equals(client.getStatus())) {
            throw new ServiceException("客户端已停用");
        }
        boolean allowed = authProperties.getAllowedClientKeys().stream()
            .anyMatch(key -> key.equalsIgnoreCase(client.getClientKey()));
        if (!allowed) {
            throw new ServiceException("该客户端不能访问C端会员服务");
        }
        return client;
    }

    private void validateTenant() {
        String tenantId = authProperties.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            throw new ServiceException("C端应用未绑定租户");
        }
        if (TenantConstants.DEFAULT_TENANT_ID.equals(tenantId)) {
            return;
        }
        SysTenant tenant = tenantMapper.selectOne(new LambdaQueryWrapper<SysTenant>()
            .eq(SysTenant::getTenantId, tenantId));
        if (tenant == null || !SystemConstants.NORMAL.equals(tenant.getStatus())) {
            throw new ServiceException("C端应用所属租户不存在或已停用");
        }
        if (tenant.getExpireTime() != null && tenant.getExpireTime().before(new Date())) {
            throw new ServiceException("C端应用所属租户已过期");
        }
    }

    private boolean supportsGrant(String configured, String expected) {
        if (StringUtils.isBlank(configured)) {
            return false;
        }
        return List.of(configured.split(",")).stream()
            .map(String::strip)
            .anyMatch(expected::equalsIgnoreCase);
    }

    private void ensureSocialProviderConfigured(String source) {
        if (CollUtil.isEmpty(socialProperties.getType())
            || !providerConfigured(socialProperties.getType().get(source))) {
            throw new ServiceException("不支持或未配置该第三方登录平台");
        }
    }

    private boolean providerConfigured(SocialLoginConfigProperties properties) {
        return properties != null
            && StringUtils.isNotBlank(properties.getClientId())
            && StringUtils.isNotBlank(properties.getClientSecret())
            && StringUtils.isNotBlank(properties.getRedirectUri());
    }
}
