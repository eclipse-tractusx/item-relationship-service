/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs;

import org.assertj.core.api.Assertions;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central" })
@ActiveProfiles(profiles = "test")
@Import(TestConfig.class)
@ExtendWith({ MockitoExtension.class,
              SpringExtension.class
})
class ConnectorEndpointServiceTest {

    @Autowired
    ConnectorEndpointsService connectorEndpointsService;

    @Autowired
    CacheManager cacheManager;

    @Test
    void shouldGenerateCacheRecordWhenFetchConnectorEndpointsCalled() {
        // given
        final String bpnRecord = "";

        // when
        connectorEndpointsService.fetchConnectorEndpoints(bpnRecord);

        // then
        final Cache connectorEndpointServiceCache = cacheManager.getCache("connector_endpoint_service_cache");
        Assertions.assertThat(connectorEndpointServiceCache.get(bpnRecord)).isNotNull();
    }
}
