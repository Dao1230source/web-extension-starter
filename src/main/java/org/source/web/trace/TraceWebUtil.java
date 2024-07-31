package org.source.web.trace;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.source.spring.trace.TraceContext;
import org.source.utility.enums.BaseExceptionEnum;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.Objects;

@UtilityClass
public class TraceWebUtil {

    public static void computeRequest(@Nullable HttpServletRequest request) {
        TraceContext.putIfAbsent(TraceContext.TRACE_ID,
                () -> Objects.isNull(request) ? null : request.getHeader(TraceContext.TRACE_ID),
                TraceContext::getDefaultTraceId);
        TraceContext.putIfAbsent(TraceContext.USER_ID,
                () -> Objects.isNull(request) ? null : request.getHeader(TraceContext.USER_ID),
                TraceContext::getDefaultUserId);
        if (Objects.isNull(request)) {
            return;
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (name.startsWith(TraceContext.EXTENSION_DATA_PREFIX)) {
                TraceContext.put(name, request.getHeader(name));
            }
        }
    }

    private static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    public static void computeWebContext() {
        computeRequest(getRequest());
    }

    public static void validRequest(HttpServletRequest request, String webSecretKey) {
        String secretKey = request.getHeader(TraceContext.SECRET_KEY);
        BaseExceptionEnum.INVALID_RPC_REQUEST.notEmpty(secretKey);
        BaseExceptionEnum.INVALID_SECRET_KEY.isTrue(secretKey.equals(webSecretKey));
    }

}
