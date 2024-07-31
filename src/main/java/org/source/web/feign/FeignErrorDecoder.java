package org.source.web.feign;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.source.spring.exception.BizException;
import org.source.spring.exception.BizExceptionEnum;
import org.source.utility.utils.Jsons;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("methodKey:{}", methodKey);
        try (Reader reader = response.body().asReader(StandardCharsets.UTF_8)) {
            return Jsons.getInstance().readValue(reader, BizException.class);
        } catch (IOException e) {
            throw BizExceptionEnum.RUNTIME_EXCEPTION.except(e);
        }
    }
}

