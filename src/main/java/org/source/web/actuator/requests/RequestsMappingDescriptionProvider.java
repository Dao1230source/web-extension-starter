package org.source.web.actuator.requests;

import jakarta.servlet.Servlet;
import org.jetbrains.annotations.Nullable;
import org.source.spring.utility.SpringUtil;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

public class RequestsMappingDescriptionProvider implements MappingDescriptionProvider {

    @Override
    public String getMappingName() {
        return "requests";
    }

    @Override
    public Map<String, List<RequestDescription>> describeMappings(ApplicationContext context) {
        if (context instanceof WebApplicationContext webApplicationContext) {
            return describeMappings(webApplicationContext);
        }
        return Collections.emptyMap();
    }

    private Map<String, List<RequestDescription>> describeMappings(WebApplicationContext context) {
        Map<String, List<RequestDescription>> mappings = new HashMap<>();
        determineDispatcherServlets(context).forEach((name, dispatcherServlet) -> mappings.put(name,
                describeMappings(new RequestsHandlerMappings(name, dispatcherServlet, context))));
        return mappings;
    }


    private Map<String, DispatcherServlet> determineDispatcherServlets(WebApplicationContext context) {
        Map<String, DispatcherServlet> dispatcherServlets = new LinkedHashMap<>();
        context.getBeansOfType(ServletRegistrationBean.class).values().forEach(registration -> {
            Servlet servlet = registration.getServlet();
            if (servlet instanceof DispatcherServlet dispatcherServlet && !dispatcherServlets.containsValue(servlet)) {
                dispatcherServlets.put(registration.getServletName(), dispatcherServlet);
            }
        });
        context.getBeansOfType(DispatcherServlet.class).forEach((name, dispatcherServlet) -> {
            if (!dispatcherServlets.containsValue(dispatcherServlet)) {
                dispatcherServlets.put(name, dispatcherServlet);
            }
        });
        return dispatcherServlets;
    }


    private List<RequestDescription> describeMappings(RequestsHandlerMappings mappings) {
        return mappings.getHandlerMappings().stream().map(this::describe).flatMap(Collection::stream).toList();
    }

    public List<RequestDescription> describe(RequestMappingHandlerMapping handlerMapping) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        return handlerMethods.entrySet().stream().map(this::describe).filter(Objects::nonNull).toList();
    }

    @Nullable
    private RequestDescription describe(Map.Entry<RequestMappingInfo, HandlerMethod> mapping) {
        HandlerMethod handlerMethod = mapping.getValue();
        if (BasicErrorController.class.isAssignableFrom(handlerMethod.getBeanType())) {
            return null;
        }
        RequestMappingInfo requestMappingInfo = mapping.getKey();
        RequestDescription description = new RequestDescription();
        description.setContextPath(SpringUtil.getContextPath());
        description.setName(handlerMethod.getMethod().getName());
        description.setClassName(handlerMethod.getMethod().getDeclaringClass().getCanonicalName());
        description.setPath(paths(requestMappingInfo).stream().findFirst().orElse(null));
        description.setMethods(requestMappingInfo.getMethodsCondition().getMethods());
        return description;
    }

    public Set<String> paths(RequestMappingInfo requestMappingInfo) {
        PatternsRequestCondition patternsCondition = requestMappingInfo.getPatternsCondition();
        if (Objects.nonNull(patternsCondition)) {
            return patternsCondition.getPatterns();
        }
        PathPatternsRequestCondition pathPatternsCondition = requestMappingInfo.getPathPatternsCondition();
        if (Objects.nonNull(pathPatternsCondition)) {
            return pathPatternsCondition.getPatternValues();
        }
        return Collections.emptySet();
    }

}
