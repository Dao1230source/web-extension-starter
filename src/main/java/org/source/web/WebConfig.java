package org.source.web;

import lombok.AllArgsConstructor;
import org.source.spring.properties.SecurityProperties;
import org.source.web.trace.TraceInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AllArgsConstructor
@ConditionalOnProperty(prefix = "org.source.web.enabled", name = "config", matchIfMissing = true)
@ConditionalOnBean(SecurityProperties.class)
@AutoConfiguration
public class WebConfig implements WebMvcConfigurer {
    private final SecurityProperties securityProperties;

    @Bean
    public TraceInterceptor logInterceptor() {
        return new TraceInterceptor(securityProperties);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor());
    }
}