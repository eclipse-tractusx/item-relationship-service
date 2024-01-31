/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.catalina.connector.Connector;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.TomcatServletWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the trusted port
 */
@Configuration
public class TrustedPortConfiguration {
    private final String serverPort;

    private final String managementPort;

    private final String trustedPort;

    public TrustedPortConfiguration(@Value("${server.port:8080}") final String serverPort,
            @Value("${management.server.port:${server.port:8080}}") final String managementPort,
            @Value("${server.trustedPort}") final String trustedPort) {

        this.serverPort = serverPort;
        this.managementPort = managementPort;
        this.trustedPort = trustedPort;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {

        final Connector[] additionalConnectors = this.additionalConnector();

        final ServerProperties serverProperties = new ServerProperties();
        return new TomcatMultiConnectorServletWebServerFactoryCustomizer(serverProperties, additionalConnectors);
    }

    private Connector[] additionalConnector() {

        if (StringUtils.isEmpty(this.trustedPort)) {
            return new Connector[0];
        }

        final Set<String> defaultPorts = new HashSet<>();
        defaultPorts.add(serverPort);
        defaultPorts.add(managementPort);

        if (defaultPorts.contains(trustedPort)) {
            return new Connector[0];
        } else {
            final Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
            connector.setScheme("http");
            connector.setPort(Integer.parseInt(trustedPort));
            return new Connector[] { connector };
        }
    }

    /**
     * Customizer for additional connectors
     */
    private static class TomcatMultiConnectorServletWebServerFactoryCustomizer
            extends TomcatServletWebServerFactoryCustomizer {
        private final Connector[] additionalConnectors;

        /* package */ TomcatMultiConnectorServletWebServerFactoryCustomizer(final ServerProperties serverProperties,
                final Connector... additionalConnectors) {
            super(serverProperties);
            this.additionalConnectors = Arrays.copyOf(additionalConnectors, additionalConnectors.length);
        }

        @Override
        public void customize(final TomcatServletWebServerFactory factory) {
            super.customize(factory);

            if (additionalConnectors != null && additionalConnectors.length > 0) {
                factory.addAdditionalTomcatConnectors(additionalConnectors);
            }
        }
    }

    @Bean
    public FilterRegistrationBean<TrustedEndpointsFilter> trustedEndpointsFilter() {
        return new FilterRegistrationBean<>(new TrustedEndpointsFilter(trustedPort));
    }
}
