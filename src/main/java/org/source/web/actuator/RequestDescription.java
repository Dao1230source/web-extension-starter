package org.source.web.actuator;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;

@Data
public class RequestDescription {
    private String contextPath;
    private String name;
    private String className;
    private String path;
    private Set<RequestMethod> methods;
}
