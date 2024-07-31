package org.source.web.actuator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.web.mappings.MappingDescriptionProvider;
import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletsMappingDescriptionProvider;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Endpoint(id = "requests")
public class RequestsEndpoint {
    private final Collection<MappingDescriptionProvider> descriptionProviders;

    private final WebApplicationContext context;

    @ReadOperation
    public List<RequestDescription> requests() {
        return descriptionProviders.stream()
                .filter(DispatcherServletsMappingDescriptionProvider.class::isInstance)
                .map(DispatcherServletsMappingDescriptionProvider.class::cast)
                .map(k -> new RequestsMappingDescriptionProvider().describeMappings(context))
                .map(Map::values)
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .toList();
    }

}
