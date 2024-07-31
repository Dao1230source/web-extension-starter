package org.source.web.actuator;

import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;

/**
 * 通过 Actuator 获取所有接口的详情
 */
@ConditionalOnProperty(prefix = "org.source.web.enabled", name = "actuator", matchIfMissing = true)
@AutoConfiguration
public class ActuatorConfig {

    @Bean
    public RequestsEndpoint requestsEndpoint(Collection<MappingDescriptionProvider> descriptionProviders,
                                             WebApplicationContext context) {
        return new RequestsEndpoint(descriptionProviders, context);
    }
}
