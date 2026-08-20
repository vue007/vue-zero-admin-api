package com.zero.admin.base.translation.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import com.zero.admin.base.translation.annotation.TranslationType;
import com.zero.admin.base.translation.core.TranslationInterface;
import com.zero.admin.base.translation.core.handler.TranslationBeanSerializerModifier;
import com.zero.admin.base.translation.core.handler.TranslationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.module.SimpleModule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 翻译模块配置类
 *
 * @author Akai
 */
@Slf4j
@AutoConfiguration
public class TranslationConfig {

    @Autowired
    private List<TranslationInterface<?>> list;

    @PostConstruct
    public void init() {
        Map<String, TranslationInterface<?>> map = new HashMap<>(list.size());
        for (TranslationInterface<?> trans : list) {
            if (trans.getClass().isAnnotationPresent(TranslationType.class)) {
                TranslationType annotation = trans.getClass().getAnnotation(TranslationType.class);
                map.put(annotation.type(), trans);
            } else {
                log.warn(trans.getClass().getName() + " 翻译实现类未标注 TranslationType 注解!");
            }
        }
        TranslationHandler.TRANSLATION_MAPPER.putAll(map);
    }

    /**
     * 在 Jackson 3 的不可变 ObjectMapper 构建阶段注册翻译序列化修改器。
     */
    @Bean
    public JsonMapperBuilderCustomizer translationJsonCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule("zero-admin-translation");
            module.setSerializerModifier(new TranslationBeanSerializerModifier());
            builder.addModule(module);
        };
    }

}
