package org.source.web.unified;

import org.jetbrains.annotations.NotNull;
import org.source.spring.io.Response;
import org.source.spring.io.ResponseIgnore;
import org.source.utility.exceptions.BaseException;
import org.source.utility.utils.Jsons;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author zengfugen
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "org.source.web.enabled", value = "unified", matchIfMissing = true)
@RestControllerAdvice
public class UnifiedResponseAdvice implements ResponseBodyAdvice<Object> {
    private static final Class<? extends Annotation> IGNORE_TYPE = ResponseIgnore.class;

    @Override
    public boolean supports(MethodParameter methodParameter, @NotNull Class clazz) {
        return !(AnnotatedElementUtils.hasAnnotation(methodParameter.getClass(), IGNORE_TYPE)
                || methodParameter.hasMethodAnnotation(IGNORE_TYPE));
    }

    /**
     * 返回类型为String时，调用  {@link StringHttpMessageConverter} 处理，返回值也应该是String，
     * 否则在 super.addDefaultHeaders() 时会转换报错
     * <p>
     * 返回值为null时，不会调用本类处理，因此应返回不为空的默认值
     * AbstractMessageConverterMethodProcessor#writeWithMessageConverters()
     *
     * @param body               返回内容
     * @param methodParameter    方法参数
     * @param mediaType          mediaType
     * @param aClass             aClass
     * @param serverHttpRequest  serverHttpRequest
     * @param serverHttpResponse serverHttpResponse
     * @return 包装的返回参数
     */
    @Override
    public Object beforeBodyWrite(Object body, @NotNull MethodParameter methodParameter,
                                  @NotNull MediaType mediaType,
                                  @NotNull Class aClass,
                                  @NotNull ServerHttpRequest serverHttpRequest,
                                  @NotNull ServerHttpResponse serverHttpResponse) {
        if (body instanceof String) {
            return Jsons.str(Response.success(body));
        }
        if (serverHttpResponse instanceof ServletServerHttpResponse httpResponse) {
            int status = httpResponse.getServletResponse().getStatus();
            if (status != 200 && body instanceof Map<?, ?> map) {
                Response<?> response = Response.fail(new BaseException(String.valueOf(status), (String) map.get("error"), null, null));
                return handleFailed(response);
            }
        }
        if (body instanceof Response<?> response) {
            return handleFailed(response);
        }
        return Response.success(body);
    }

    private Response<?> handleFailed(Response<?> response) {
        if (!response.isSuccess()) {
            response.setTraceId(response.getTraceId());
        }
        return response;
    }
}
