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
package org.eclipse.tractusx.irs.testing.containers;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;
import org.eclipse.tractusx.irs.data.LocalTestDataConfiguration;
import org.mockito.Mockito;

/**
 * Helper class for local test files.
 */
public class LocalTestDataConfigurationAware {

    protected LocalTestDataConfiguration localTestDataConfiguration = Mockito.mock(LocalTestDataConfiguration.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected LocalTestDataConfigurationAware() throws IOException {
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try (var stream = LocalTestDataConfigurationAware.class.getResourceAsStream("/test_data/CX_Testdata.json")) {
            final CxTestDataContainer cxTestDataContainer = objectMapper.readValue(stream, CxTestDataContainer.class);
            Mockito.when(localTestDataConfiguration.cxTestDataContainer()).thenReturn(cxTestDataContainer);
        }
    }

}
