package org.source.web.unified;

import lombok.extern.slf4j.Slf4j;
import org.source.spring.exception.BizException;
import org.source.spring.exception.BizExceptionEnum;
import org.source.spring.io.Response;
import org.source.utility.exceptions.BaseException;
import org.source.utility.utils.Strings;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zengfugen
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "org.source.web.enabled", value = "unified", matchIfMissing = true)
@RestControllerAdvice
public class UnifiedExceptionHandler {

    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<Void> exception(BaseException exception) {
        if (exception instanceof BizException) {
            return Response.fail(exception);
        }
        return Response.fail(new BizException(exception));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Response<Void> exception(MethodArgumentNotValidException exception) {
        List<ObjectError> allErrors = exception.getAllErrors();
        String res = allErrors.stream().map(k -> {
            String code = k.getCode();
            if (k instanceof FieldError fieldError) {
                code = fieldError.getField();
            }
            return Strings.format("{}:{}", code, k.getDefaultMessage());
        }).collect(Collectors.joining(","));
        return Response.fail(BizExceptionEnum.PARAM_EXCEPTION, res);
    }


    @ExceptionHandler(Exception.class)
    public Response<Void> exception(Exception exception) {
        log.error("UnifiedExceptionHandler", exception);
        Exception e = BizExceptionEnum.RUNTIME_EXCEPTION.except(exception);
        return Response.fail(BizExceptionEnum.RUNTIME_EXCEPTION, e.toString());
    }

}
