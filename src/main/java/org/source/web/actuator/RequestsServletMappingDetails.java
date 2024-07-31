package org.source.web.actuator;

import lombok.Data;
import org.springframework.boot.actuate.web.mappings.HandlerMethodDescription;
import org.springframework.boot.actuate.web.mappings.servlet.RequestMappingConditionsDescription;

@Data
public class RequestsServletMappingDetails {

    private HandlerMethodDescription handlerMethod;

    private RequestMappingConditionsDescription requestMappingConditions;
}
