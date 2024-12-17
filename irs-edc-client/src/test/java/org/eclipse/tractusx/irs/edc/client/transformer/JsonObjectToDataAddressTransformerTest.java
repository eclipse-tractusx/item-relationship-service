/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.edc.client.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.testutil.TestMother.objectMapper;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.tractusx.irs.edc.client.model.edr.DataAddress;
import org.eclipse.tractusx.irs.edc.client.model.edr.Properties;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JsonObjectToDataAddressTransformerTest {
    private EdcTransformer edcTransformer;

    @BeforeEach
    void setUp() {
        final TitaniumJsonLd jsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        jsonLd.registerNamespace("edc", "https://w3id.org/edc/v0.0.1/ns/");
        jsonLd.registerNamespace("tx", "https://w3id.org/tractusx/v0.0.1/ns/");
        jsonLd.registerNamespace("tx-auth", "https://w3id.org/tractusx/auth/");
        jsonLd.registerNamespace("cx-policy", "https://w3id.org/catenax/policy/");
        jsonLd.registerNamespace("odrl", "http://www.w3.org/ns/odrl/2/");

        ObjectMapper objectMapper = objectMapper();
        edcTransformer = new EdcTransformer(objectMapper, jsonLd, new TypeTransformerRegistryImpl());
    }

    @Test
    void shouldTransformJsonObjectToDataAddressCorrectly() {
        // Arrange
        final String dataAddressJson = getDataAddressJson();

        // Act
        final DataAddress dataAddress = edcTransformer.transformDataAddress(dataAddressJson, StandardCharsets.UTF_8);

        // DataAddress
        assertThat(dataAddress).isNotNull();
        Properties properties = dataAddress.properties();
        assertThat(properties).isNotNull();
        assertThat(properties.endpointType()).isEqualTo("https://w3id.org/idsa/v4.1/HTTP");
        assertThat(properties.refreshEndpoint()).isEqualTo("http://umbrella-dataprovider-edc-dataplane:8081/api/public/token");
        assertThat(properties.audience()).isEqualTo("did:web:mock-util-service/BPNL00000003AYRE");
        assertThat(properties.endpoint()).isEqualTo("http://umbrella-dataprovider-edc-dataplane:8081/api/public");
        assertThat(properties.refreshToken()).isEqualTo("eyJraWQiOiJ0b2tlblNpZ25lclB1YmxpY0tleSIsImFsZyI6IlJTMjU2In0.eyJleHAiOjE3MjAwODQ4NDgsImlhdCI6MTcyMDA4NDU0OCwianRpIjoiNDY0OWM3ZTItY2IwZC00M2E2LWFiODEtZGEzMGY3MDc4ZmE2In0.kZUk7j_3BATsQusIOn-Ex8e4GAtI3eXZqrKJDefh6d8HtVbcR1AM-arOZZ2kLWcuGA115J2lVaSGSgcyKya3FFmsYZ4oUJkeiy0RtYtXxtZS5-ZUhzQqbjxz5oZVte3AVDfRmJGxr6j9Go0RguMC3ebtTJFwHHMvr85Ytqp2EaSb6kSDiprO2U9IKl3dfrLXmfNSxs279_hTpnWLltVSj3e1RfgCfy5ji_EfICs0JaWhSUjOb4y2v96VexQPBXRewEQ6ob5S9AIE9DqjcudHEB2HM1MXIcvFGmDXKQ6CJwtvwzR6iCNj_KEbKOeVDmzo7lTGCtViGycsAW6B9hQayQ");
        assertThat(properties.expiresIn()).isEqualTo("300");
        assertThat(properties.authorization()).isEqualTo("eyJraWQiOiJ0b2tlblNpZ25lclB1YmxpY0tleSIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJCUE5MMDAwMDAwMDNBWVJFIiwiYXVkIjoiQlBOTDAwMDAwMDAzQVlSRSIsInN1YiI6IkJQTkwwMDAwMDAwM0FZUkUiLCJleHAiOjE3MjAwODQ4NDgsImlhdCI6MTcyMDA4NDU0OCwianRpIjoiZmEzZjRiMGQtNjYxNy00ZTJhLWE5OGQtMDEzNDA5YmUyNGE4In0.EpClrA8M13UQzjSU17962XdfxWOHMXVcTaOUCsOSoaxUVbpztVT81SqBaSUnTw-mCv7AUH3u60SEYBPYcNa7S1_VEOJnQ9UuK1egeOGVPWHPWwakpTvSxYlRA_ELiAvOjVHv397roPBZnpej7AEr2tZTbJcS2j7D5XeFOKXtcnsG3yyqKzJFlGsuylYqewDuhZbDWN9yx3QQPFV8vhblJw2vlfOdnn_t7JdfpygKHHgwfJqcQ3kqrArgg0hA4JfJV6bJU2eI5Z9wPbOtJA98Uq7sqOh_OTRohGdRtbglPBMPDfwKqO2REC7TlOBCgeZbdTAa3gRVvKjUHu1L5gNPCA");
        assertThat(properties.refreshAudience()).isEqualTo("did:web:mock-util-service/BPNL00000003AYRE");
    }

    private static @NotNull String getDataAddressJson() {
        return """
                {
                 	"@type": "DataAddress",
                 	"endpointType": "https://w3id.org/idsa/v4.1/HTTP",
                 	"tx-auth:refreshEndpoint": "http://umbrella-dataprovider-edc-dataplane:8081/api/public/token",
                 	"tx-auth:audience": "did:web:mock-util-service/BPNL00000003AYRE",
                 	"type": "https://w3id.org/idsa/v4.1/HTTP",
                 	"endpoint": "http://umbrella-dataprovider-edc-dataplane:8081/api/public",
                 	"tx-auth:refreshToken": "eyJraWQiOiJ0b2tlblNpZ25lclB1YmxpY0tleSIsImFsZyI6IlJTMjU2In0.eyJleHAiOjE3MjAwODQ4NDgsImlhdCI6MTcyMDA4NDU0OCwianRpIjoiNDY0OWM3ZTItY2IwZC00M2E2LWFiODEtZGEzMGY3MDc4ZmE2In0.kZUk7j_3BATsQusIOn-Ex8e4GAtI3eXZqrKJDefh6d8HtVbcR1AM-arOZZ2kLWcuGA115J2lVaSGSgcyKya3FFmsYZ4oUJkeiy0RtYtXxtZS5-ZUhzQqbjxz5oZVte3AVDfRmJGxr6j9Go0RguMC3ebtTJFwHHMvr85Ytqp2EaSb6kSDiprO2U9IKl3dfrLXmfNSxs279_hTpnWLltVSj3e1RfgCfy5ji_EfICs0JaWhSUjOb4y2v96VexQPBXRewEQ6ob5S9AIE9DqjcudHEB2HM1MXIcvFGmDXKQ6CJwtvwzR6iCNj_KEbKOeVDmzo7lTGCtViGycsAW6B9hQayQ",
                 	"tx-auth:expiresIn": "300",
                 	"authorization": "eyJraWQiOiJ0b2tlblNpZ25lclB1YmxpY0tleSIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiJCUE5MMDAwMDAwMDNBWVJFIiwiYXVkIjoiQlBOTDAwMDAwMDAzQVlSRSIsInN1YiI6IkJQTkwwMDAwMDAwM0FZUkUiLCJleHAiOjE3MjAwODQ4NDgsImlhdCI6MTcyMDA4NDU0OCwianRpIjoiZmEzZjRiMGQtNjYxNy00ZTJhLWE5OGQtMDEzNDA5YmUyNGE4In0.EpClrA8M13UQzjSU17962XdfxWOHMXVcTaOUCsOSoaxUVbpztVT81SqBaSUnTw-mCv7AUH3u60SEYBPYcNa7S1_VEOJnQ9UuK1egeOGVPWHPWwakpTvSxYlRA_ELiAvOjVHv397roPBZnpej7AEr2tZTbJcS2j7D5XeFOKXtcnsG3yyqKzJFlGsuylYqewDuhZbDWN9yx3QQPFV8vhblJw2vlfOdnn_t7JdfpygKHHgwfJqcQ3kqrArgg0hA4JfJV6bJU2eI5Z9wPbOtJA98Uq7sqOh_OTRohGdRtbglPBMPDfwKqO2REC7TlOBCgeZbdTAa3gRVvKjUHu1L5gNPCA",
                 	"tx-auth:refreshAudience": "did:web:mock-util-service/BPNL00000003AYRE",
                 	"@context": {
                 		"@vocab": "https://w3id.org/edc/v0.0.1/ns/",
                 		"edc": "https://w3id.org/edc/v0.0.1/ns/",
                 		"tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                 		"tx-auth": "https://w3id.org/tractusx/auth/",
                 		"cx-policy": "https://w3id.org/catenax/policy/",
                 		"odrl": "http://www.w3.org/ns/odrl/2/"
                 	}
                 }
                """;
    }
}
