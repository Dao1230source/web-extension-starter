/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.source.web.actuator;

import jakarta.servlet.ServletException;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardWrapper;
import org.source.utility.utils.Streams;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * {@code DispatcherServletHandlerMappings} provides access to a {@link DispatcherServlet
 * DispatcherServlet's} handler mappings, triggering initialization of the dispatcher
 * servlet if necessary.
 *
 * @author Andy Wilkinson
 */
public class RequestsHandlerMappings {

    private final String name;

    private final DispatcherServlet dispatcherServlet;

    private final WebApplicationContext applicationContext;

    RequestsHandlerMappings(String name, DispatcherServlet dispatcherServlet,
                            WebApplicationContext applicationContext) {
        this.name = name;
        this.dispatcherServlet = dispatcherServlet;
        this.applicationContext = applicationContext;
    }

    protected List<RequestMappingHandlerMapping> getHandlerMappings() {
        List<HandlerMapping> handlerMappings = this.dispatcherServlet.getHandlerMappings();
        if (handlerMappings == null) {
            initializeDispatcherServletIfPossible();
            handlerMappings = this.dispatcherServlet.getHandlerMappings();
        }
        // 只保留 controller
        return Streams.of(handlerMappings)
                .filter(RequestMappingHandlerMapping.class::isInstance)
                .map(RequestMappingHandlerMapping.class::cast)
                .toList();
    }

    private void initializeDispatcherServletIfPossible() {
        if (!(this.applicationContext instanceof ServletWebServerApplicationContext webServerApplicationContext)) {
            return;
        }
        WebServer webServer = webServerApplicationContext.getWebServer();
        if (webServer instanceof TomcatWebServer tomcatWebServer) {
            new TomcatServletInitializer(tomcatWebServer).initializeServlet(this.name);
        }
    }

    private record TomcatServletInitializer(TomcatWebServer webServer) {

        void initializeServlet(String name) {
            findContext().ifPresent(context -> initializeServlet(context, name));
        }

        private Optional<Context> findContext() {
            return Stream.of(this.webServer.getTomcat().getHost().findChildren())
                    .filter(Context.class::isInstance)
                    .map(Context.class::cast)
                    .findFirst();
        }

        private void initializeServlet(Context context, String name) {
            Container child = context.findChild(name);
            if (child instanceof StandardWrapper wrapper) {
                try {
                    wrapper.deallocate(wrapper.allocate());
                } catch (ServletException ex) {
                    // Continue
                }
            }
        }

    }


}
