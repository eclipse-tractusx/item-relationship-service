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
package org.eclipse.tractusx.irs.aaswrapper.submodel.domain;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.tractusx.irs.TestConfig;
import org.eclipse.tractusx.irs.exceptions.JsonParseException;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = { "aasWrapper.host=http://localhost:56985/api/service",
                               "resilience4j.retry.configs.default.waitDuration=1s"
})
@Import(TestConfig.class)
@ActiveProfiles(profiles = { "test" })
class SubmodelClientImplWiremockTest {

    private final static String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
    private WireMockServer wireMockServer;

    private JsonUtil jsonUtil;

    @Autowired
    private SubmodelClient submodelClient;

    @BeforeEach
    void configureSystemUnderTest() {
        this.jsonUtil = new JsonUtil();
        this.wireMockServer = new WireMockServer(options().port(56985));
        this.wireMockServer.start();
        configureFor(this.wireMockServer.port());
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void shouldReturnCorrectAspectModelWithWireMockResponse() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBodyFile("assemblyPartRelationship.json")));

        // Act
        final AssemblyPartRelationship submodel = submodelClient.getSubmodel(url, AssemblyPartRelationship.class);

        // Assert
        assertThat(submodel.getCatenaXId()).isEqualTo("urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978");
    }

    @Test
    void shouldThrowParseExceptionWhenSubmodelContainsWrongPayload() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBodyFile("materialForRecycling.json")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> submodelClient.getSubmodel(url,
                AssemblyPartRelationship.class);

        // Assert
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(throwingCallable);
    }

    @Test
    void shouldReturnObjectAsStringWhenNotAssemblyPartRelationship() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBodyFile("materialForRecycling.json")));

        // Act
        final Object submodel = submodelClient.getSubmodel(url, Object.class);

        // Assert
        assertThat(jsonUtil.asString(submodel)).isNotBlank();
    }

    @Test
    void shouldReturnObjectAsStringWhenMalformedResponse() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBody("{name: test, {}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> submodelClient.getSubmodel(url,
                Object.class);

        // Assert
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(throwingCallable);
    }
}
