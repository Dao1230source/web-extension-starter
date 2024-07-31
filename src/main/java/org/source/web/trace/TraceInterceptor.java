package org.source.web.trace;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.source.spring.properties.SecurityProperties;
import org.source.spring.trace.TraceContext;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

@AllArgsConstructor
public class TraceInterceptor implements HandlerInterceptor {
    private final SecurityProperties securityProperties;

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        TraceWebUtil.validRequest(request, securityProperties.getSecretKey());
        TraceWebUtil.computeRequest(request);
        return true;
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                @Nonnull Object handler, @Nullable Exception ex) {
        TraceContext.clear();
    }
}