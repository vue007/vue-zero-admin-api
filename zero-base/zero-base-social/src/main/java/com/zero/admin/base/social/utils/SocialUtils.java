package com.zero.admin.base.social.utils;

import cn.hutool.core.util.ObjectUtil;
import com.xkcoding.http.config.HttpConfig;
import me.zhyd.oauth.config.AuthConfig;
import me.zhyd.oauth.exception.AuthException;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.*;
import com.zero.admin.base.core.utils.SpringUtils;
import com.zero.admin.base.social.config.properties.SocialLoginConfigProperties;
import com.zero.admin.base.social.config.properties.SocialProperties;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;

/**
 * 认证授权工具类
 *
 * @author Akai
 */
public class SocialUtils {

    private static final AuthRedisStateCache STATE_CACHE = SpringUtils.getBean(AuthRedisStateCache.class);

    public static AuthResponse<AuthUser> loginAuth(String source, String code, String state, SocialProperties socialProperties) throws AuthException {
        AuthRequest authRequest = getAuthRequest(source, socialProperties);
        AuthCallback callback = new AuthCallback();
        callback.setCode(code);
        callback.setState(state);
        return authRequest.login(callback);
    }

    public static AuthRequest getAuthRequest(String source, SocialProperties socialProperties) throws AuthException {
        SocialLoginConfigProperties obj = socialProperties.getType().get(source);
        if (ObjectUtil.isNull(obj)) {
            throw new AuthException("不支持的第三方登录类型");
        }
        AuthConfig.AuthConfigBuilder builder = AuthConfig.builder()
            .clientId(obj.getClientId())
            .clientSecret(obj.getClientSecret())
            .redirectUri(obj.getRedirectUri())
            .serverUrl(obj.getServerUrl())
            .scopes(obj.getScopes())
            .httpConfig(createHttpConfig(source, socialProperties));
        return switch (source.toLowerCase()) {
            case "dingtalk" -> new AuthDingTalkV2Request(builder.build(), STATE_CACHE);
            case "baidu" -> new AuthBaiduRequest(builder.build(), STATE_CACHE);
            case "github" -> new AuthGithubRequest(builder.build(), STATE_CACHE);
            case "gitee" -> new AuthGiteeRequest(builder.build(), STATE_CACHE);
            case "weibo" -> new AuthWeiboRequest(builder.build(), STATE_CACHE);
            case "coding" -> new AuthCodingRequest(builder.build(), STATE_CACHE);
            case "oschina" -> new AuthOschinaRequest(builder.build(), STATE_CACHE);
            // 支付宝在创建回调地址时，不允许使用localhost或者127.0.0.1，所以这儿的回调地址使用的局域网内的ip
            case "alipay_wallet" -> new AuthAlipayRequest(builder.build(), socialProperties.getType().get("alipay_wallet").getAlipayPublicKey(), STATE_CACHE);
            case "qq" -> new AuthQqRequest(builder.build(), STATE_CACHE);
            case "wechat_open" -> new AuthWeChatOpenRequest(builder.build(), STATE_CACHE);
            case "taobao" -> new AuthTaobaoRequest(builder.build(), STATE_CACHE);
            case "douyin" -> new AuthDouyinRequest(builder.build(), STATE_CACHE);
            case "linkedin" -> new AuthLinkedinRequest(builder.build(), STATE_CACHE);
            case "microsoft" -> new AuthMicrosoftRequest(builder.tenantId(obj.getTenantId()).build(), STATE_CACHE);
            case "renren" -> new AuthRenrenRequest(builder.build(), STATE_CACHE);
            case "stack_overflow" -> new AuthStackOverflowRequest(builder.stackOverflowKey(obj.getStackOverflowKey()).build(), STATE_CACHE);
            case "huawei" -> new AuthHuaweiV3Request(builder.build(), STATE_CACHE);
            case "wechat_enterprise" -> new AuthWeChatEnterpriseQrcodeV2Request(builder.agentId(obj.getAgentId()).build(), STATE_CACHE);
            case "gitlab" -> new AuthGitlabRequest(builder.build(), STATE_CACHE);
            case "wechat_mp" -> new AuthWeChatMpRequest(builder.build(), STATE_CACHE);
            case "aliyun" -> new AuthAliyunRequest(builder.build(), STATE_CACHE);
            case "maxkey" -> new AuthMaxKeyRequest(builder.build(), STATE_CACHE);
            case "topiam" -> new AuthTopIamRequest(builder.build(), STATE_CACHE);
            case "gitea" -> new AuthGiteaRequest(builder.build(), STATE_CACHE);
            default -> throw new AuthException("未获取到有效的Auth配置");
        };
    }

    private static HttpConfig createHttpConfig(String source, SocialProperties socialProperties) {
        SocialProperties.HttpConfig config = socialProperties.getHttpConfig();
        HttpConfig.HttpConfigBuilder builder = HttpConfig.builder();
        if (config == null) {
            return builder.build();
        }

        builder.timeout(config.getTimeout());
        SocialProperties.ProxyConfig proxyConfig = findProxyConfig(source, config.getProxy());
        if (proxyConfig == null || proxyConfig.getType() == Proxy.Type.DIRECT
            || proxyConfig.getHostname() == null || proxyConfig.getHostname().isBlank()
            || proxyConfig.getPort() == null || proxyConfig.getPort() < 1 || proxyConfig.getPort() > 65535) {
            return builder.build();
        }

        builder.proxy(new Proxy(proxyConfig.getType(),
            new InetSocketAddress(proxyConfig.getHostname(), proxyConfig.getPort())));
        return builder.build();
    }

    private static SocialProperties.ProxyConfig findProxyConfig(
        String source, Map<String, SocialProperties.ProxyConfig> proxyConfigs) {
        if (proxyConfigs == null || proxyConfigs.isEmpty()) {
            return null;
        }
        return proxyConfigs.entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(source))
            .map(Map.Entry::getValue)
            .findFirst()
            .orElse(null);
    }
}

