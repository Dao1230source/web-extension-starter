package org.source.web.trace;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.source.spring.trace.TraceContext;

public class FeignRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        TraceWebUtil.computeWebContext();
        requestTemplate.header(TraceContext.TRACE_ID, TraceContext.getTraceId());
        requestTemplate.header(TraceContext.USER_ID, TraceContext.getUserId());
        TraceContext.extensionData().forEach(requestTemplate::header);
    }
}
