package com.zero.admin.consumer.service;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.zero.admin.base.core.enums.UserType;
import com.zero.admin.base.core.exception.ServiceException;
import com.zero.admin.base.core.utils.ServletUtils;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.redis.utils.RedisUtils;
import com.zero.admin.base.json.utils.JsonUtils;
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
import com.zero.admin.consumer.domain.model.ConsumerSocialState;
import com.zero.admin.consumer.domain.vo.ConsumerLoginVo;
import com.zero.admin.consumer.domain.vo.ConsumerMemberProfileVo;
import com.zero.admin.member.domain.model.MemberLoginUser;
import com.zero.admin.member.domain.model.MemberSocialIdentity;
import com.zero.admin.member.domain.vo.MemberVo;
import com.zero.admin.member.service.IMemberService;
import com.zero.admin.tenantapp.domain.vo.TenantApplicationClientAuthVo;
import com.zero.admin.tenantapp.service.ITenantApplicationService;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** C 端认证协议编排，不复用后台 sys_user 登录策略。 */
@Service
@RequiredArgsConstructor
public class ConsumerAuthService {

    private static final String PASSWORD_GRANT = "password";
    private static final String SOCIAL_GRANT = "social";
    private static final String MEMBER_SCOPE = "app:member";

    private final ConsumerAuthProperties authProperties;
    private final ConsumerWechatProperties wechatProperties;
    private final SocialProperties socialProperties;
    private final IMemberService memberService;
    private final ITenantApplicationService applicationService;

    public ConsumerLoginVo register(ConsumerRegisterRequest request) {
        AuthBoundary boundary = validateBoundary(request.getAppId(), request.getChannel(), PASSWORD_GRANT);
        MemberVo member = memberService.register(boundary.tenantId(), request);
        return issueSession(member, boundary, PASSWORD_GRANT);
    }

    public ConsumerLoginVo passwordLogin(ConsumerPasswordLoginRequest request) {
        AuthBoundary boundary = validateBoundary(request.getAppId(), request.getChannel(), PASSWORD_GRANT);
        MemberVo member = authenticatePasswordWithRetryLimit(request, boundary.tenantId());
        return issueSession(member, boundary, PASSWORD_GRANT);
    }

    public ConsumerLoginVo socialLogin(ConsumerSocialLoginRequest request) {
        String source = request.getSource().strip().toLowerCase(Locale.ROOT);
        ConsumerSocialState socialState = parseSocialState(request.getState());
        if (!request.getAppId().strip().equals(socialState.appId())
            || !request.getChannel().strip().equalsIgnoreCase(socialState.channel())
            || !source.equals(socialState.source())) {
            throw new ServiceException("第三方登录上下文无效");
        }
        AuthBoundary boundary = validateBoundary(socialState.appId(), socialState.channel(), SOCIAL_GRANT);
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
            source, request.getCode(), request.getState(), socialProperties);
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
        MemberVo member = memberService.loginOrRegisterSocial(boundary.tenantId(), identity);
        return issueSession(member, boundary, source);
    }

    public ConsumerLoginVo wechatMiniProgramLogin(ConsumerWechatLoginRequest request) {
        AuthBoundary boundary = validateBoundary(request.getAppId(), request.getChannel(), SOCIAL_GRANT);
        ConsumerWechatProperties.MiniProgram miniProgram =
            wechatProperties.getMiniPrograms().get(boundary.appId());
        if (miniProgram == null
            || StringUtils.isBlank(miniProgram.getWechatAppId())
            || StringUtils.isBlank(miniProgram.getSecret())) {
            throw new ServiceException("微信小程序未配置或未启用");
        }

        AuthRequest authRequest = new AuthWechatMiniProgramRequest(AuthConfig.builder()
            .clientId(miniProgram.getWechatAppId())
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
            .source("wechat_mini_program@" + miniProgram.getWechatAppId())
            .openId(openId)
            .unionId(token.getUnionId())
            .username(authUser.getUsername())
            .nickname(authUser.getNickname())
            .avatar(authUser.getAvatar())
            .build();
        MemberVo member = memberService.loginOrRegisterSocial(boundary.tenantId(), identity);
        return issueSession(member, boundary, "wechat_mini_program");
    }

    public String socialAuthorizeUrl(String appId, String channel, String source) {
        String normalizedSource = source.strip().toLowerCase(Locale.ROOT);
        AuthBoundary boundary = validateBoundary(appId, channel, SOCIAL_GRANT);
        ensureSocialProviderConfigured(normalizedSource);
        ConsumerSocialState state = new ConsumerSocialState(
            boundary.appId(), boundary.channel(), normalizedSource, AuthStateUtils.createState());
        String encodedState = Base64.encode(JsonUtils.toJsonString(state), StandardCharsets.UTF_8);
        return SocialUtils.getAuthRequest(normalizedSource, socialProperties).authorize(encodedState);
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

    private ConsumerLoginVo issueSession(
        MemberVo member,
        AuthBoundary boundary,
        String loginSource
    ) {
        if (!Objects.equals(member.getTenantId(), boundary.tenantId())) {
            throw new ServiceException("会员租户上下文异常");
        }
        memberService.recordLogin(member.getTenantId(), member.getMemberId(), ServletUtils.getClientIP());

        TenantApplicationClientAuthVo client = boundary.applicationClient();
        MemberLoginUser loginUser = new MemberLoginUser();
        loginUser.setTenantId(member.getTenantId());
        loginUser.setUserId(member.getMemberId());
        loginUser.setUsername(member.getUsername());
        loginUser.setNickname(member.getNickname());
        loginUser.setUserType(UserType.APP_USER.getUserType());
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        loginUser.setApplicationId(client.getApplicationId());
        loginUser.setAuthClientId(client.getAuthClientId());
        loginUser.setAppId(boundary.appId());
        loginUser.setChannel(boundary.channel());
        loginUser.setAppScopes(List.copyOf(client.getScopes()));
        loginUser.setLoginSource(loginSource);
        LoginHelper.login(loginUser, ObjectUtil.defaultIfNull(client.getTimeout(), 0L));

        return ConsumerLoginVo.builder()
            .accessToken(LoginHelper.getToken())
            .expireIn(LoginHelper.getTokenTimeout())
            .appId(boundary.appId())
            .channel(boundary.channel())
            .member(ConsumerMemberProfileVo.from(member))
            .build();
    }

    private MemberVo authenticatePasswordWithRetryLimit(
        ConsumerPasswordLoginRequest request,
        String tenantId
    ) {
        String username = request.getUsername().strip().toLowerCase(Locale.ROOT);
        String retryKey = "consumer:password:error:" + tenantId + ":" + username;
        int retryCount = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(retryKey), 0);
        if (retryCount >= authProperties.getMaxRetryCount()) {
            throw new ServiceException("密码错误次数过多，请稍后再试");
        }
        try {
            MemberVo member = memberService.authenticatePassword(
                tenantId, username, request.getPassword());
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

    private AuthBoundary validateBoundary(String appId, String channel, String grantType) {
        TenantApplicationClientAuthVo applicationClient =
            applicationService.resolveEnabledClient(appId, channel);
        if (applicationClient.getScopes() == null
            || applicationClient.getScopes().stream().noneMatch(MEMBER_SCOPE::equalsIgnoreCase)) {
            throw new ServiceException("应用未获会员服务授权");
        }
        if (!supportsGrant(applicationClient.getGrantType(), grantType)) {
            throw new ServiceException("当前终端不支持该登录方式");
        }
        return new AuthBoundary(applicationClient);
    }

    private ConsumerSocialState parseSocialState(String encodedState) {
        try {
            ConsumerSocialState state = JsonUtils.parseObject(
                Base64.decodeStr(encodedState, StandardCharsets.UTF_8), ConsumerSocialState.class);
            if (state == null
                || StringUtils.isBlank(state.appId())
                || StringUtils.isBlank(state.channel())
                || StringUtils.isBlank(state.source())
                || StringUtils.isBlank(state.nonce())) {
                throw new ServiceException("第三方登录上下文无效");
            }
            return new ConsumerSocialState(
                state.appId().strip(),
                state.channel().strip().toLowerCase(Locale.ROOT),
                state.source().toLowerCase(Locale.ROOT),
                state.nonce());
        } catch (ServiceException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new ServiceException("第三方登录上下文无效");
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

    private record AuthBoundary(TenantApplicationClientAuthVo applicationClient) {

        private String tenantId() {
            return applicationClient.getTenantId();
        }

        private String appId() {
            return applicationClient.getAppId();
        }

        private String channel() {
            return applicationClient.getChannel();
        }
    }
}
