package org.source.web.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(value = "org.source.web.enabled")
@AutoConfiguration
public class EnableProperties {
    /**
     * 启用 spring actuator
     */
    private boolean actuator = true;
    private boolean feign = true;
    private boolean unified = true;
    private boolean config = true;
}
