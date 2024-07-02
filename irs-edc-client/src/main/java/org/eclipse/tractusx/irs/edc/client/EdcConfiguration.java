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
package org.eclipse.tractusx.irs.edc.client;

import java.time.Duration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * EDC configuration settings. Automatically populated by Spring from application.yml
 * and other configuration sources.
 */
@Configuration("irsEdcClientEdcConfiguration")
@ConfigurationProperties(prefix = "irs-edc-client")
@Data
public class EdcConfiguration {

    private ControlplaneConfig controlplane = new ControlplaneConfig();
    private SubmodelConfig submodel = new SubmodelConfig();
    private String callbackUrl;

    /**
     * Container for controlplane config
     */
    @Data
    public static class ControlplaneConfig {
        private EndpointConfig endpoint = new EndpointConfig();

        private String providerSuffix;

        private int catalogLimit;

        private int catalogPageSize;

        private Duration requestTtl;

        private ApiKeyConfig apiKey = new ApiKeyConfig();

        /**
         * Container for controlplane endpoint config
         */
        @Data
        public static class EndpointConfig {
            private String data;
            private String catalog;
            private String contractNegotiation;
            private String transferProcess;
            private String stateSuffix;

        }

        /**
         * Container for controlplane  apikey config
         */
        @Data
        public static class ApiKeyConfig {
            private String header;
            private String secret;

        }
    }

    /**
     * Container for submodel config
     */
    @Data
    public static class SubmodelConfig {

        private Duration requestTtl;

        private String urnPrefix;

        private String submodelSuffix;
    }


}
