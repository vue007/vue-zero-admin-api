package com.zero.admin.base.web.interceptor;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.zero.admin.base.core.utils.StringUtils;
import com.zero.admin.base.json.utils.JsonUtils;
import com.zero.admin.base.web.filter.RepeatedlyRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * web的调用时间统计拦截器
 *
 * @author Akai
 */
@Slf4j
public class PlusWebInvokeTimeInterceptor implements HandlerInterceptor {

    private final static ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();
    private static final String MASK_VALUE = "******";
    private static final Set<String> SENSITIVE_PARAM_NAMES = Set.of(
        "password", "oldpassword", "newpassword", "confirmpassword",
        "accesskey", "accesskeyid", "secretkey", "secretaccesskey", "secretid",
        "apikey", "apikeysecret", "clientsecret", "token", "tokenid", "tokenids",
        "sessiontoken", "accesstoken", "refreshtoken", "authorization"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getMethod() + " " + request.getRequestURI();

        // 打印请求参数
        if (isJsonRequest(request)) {
            String jsonParam = "";
            if (request instanceof RepeatedlyRequestWrapper) {
                BufferedReader reader = request.getReader();
                jsonParam = IoUtil.read(reader);
            }
            log.info("[PLUS]开始请求 => URL[{}],参数类型[json],参数:[{}]", url, maskJsonParams(jsonParam));
        } else {
            Map<String, String[]> parameterMap = request.getParameterMap();
            if (MapUtil.isNotEmpty(parameterMap)) {
                String parameters = JsonUtils.toJsonString(maskRequestParams(parameterMap));
                log.info("[PLUS]开始请求 => URL[{}],参数类型[param],参数:[{}]", url, parameters);
            } else {
                log.info("[PLUS]开始请求 => URL[{}],无参数", url);
            }
        }

        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        StopWatch stopWatch = KEY_CACHE.get();
        if (ObjectUtil.isNotNull(stopWatch)) {
            stopWatch.stop();
            log.info("[PLUS]结束请求 => URL[{}],耗时:[{}]毫秒", request.getMethod() + " " + request.getRequestURI(), stopWatch.getDuration().toMillis());
            KEY_CACHE.remove();
        }
    }

    /**
     * 判断本次请求的数据类型是否为json
     *
     * @param request request
     * @return boolean
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType != null) {
            return StringUtils.startsWithIgnoreCase(contentType, MediaType.APPLICATION_JSON_VALUE);
        }
        return false;
    }

    private String maskJsonParams(String jsonParam) {
        if (StringUtils.isBlank(jsonParam)) {
            return jsonParam;
        }
        try {
            Object value = JsonUtils.parseObject(jsonParam, Object.class);
            maskSensitiveValues(value);
            return JsonUtils.toJsonString(value);
        } catch (RuntimeException e) {
            // 请求日志不是业务处理的一部分。解析失败时隐藏请求体，避免异常格式绕过脱敏。
            return "[请求体解析失败，已隐藏]";
        }
    }

    private Map<String, Object> maskRequestParams(Map<String, String[]> parameterMap) {
        Map<String, Object> result = new LinkedHashMap<>();
        parameterMap.forEach((name, value) ->
            result.put(name, isSensitiveParam(name) ? MASK_VALUE : value));
        return result;
    }

    @SuppressWarnings("unchecked")
    private void maskSensitiveValues(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<Object, Object> map = (Map<Object, Object>) source;
            map.replaceAll((name, item) -> {
                if (name instanceof String key && isSensitiveParam(key)) {
                    return MASK_VALUE;
                }
                maskSensitiveValues(item);
                return item;
            });
        } else if (value instanceof Collection<?> collection) {
            collection.forEach(this::maskSensitiveValues);
        }
    }

    private boolean isSensitiveParam(String name) {
        String normalized = name.replace("_", "")
            .replace("-", "")
            .toLowerCase(Locale.ROOT);
        return SENSITIVE_PARAM_NAMES.contains(normalized);
    }

}
