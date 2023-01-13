/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.semanticshub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class SemanticsHubClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final SemanticsHubClientImpl semanticsHubClient = new SemanticsHubClientImpl(restTemplate, "url/{urn}", "");

    @Test
    void shouldCallExternalServiceOnceAndGetJsonSchema() throws SchemaNotFoundException {
        final String jsonSchemaMock = "{\"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"integer\"}";
        doReturn(jsonSchemaMock).when(restTemplate).getForObject(any(), eq(String.class));

        final String resultJsonSchema = semanticsHubClient.getModelJsonSchema("urn");

        assertThat(resultJsonSchema).isNotBlank().contains("http://json-schema.org/draft-07/schema#");
        verify(this.restTemplate, times(1)).getForObject(any(), eq(String.class));
    }

}
