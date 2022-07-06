//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package net.catenax.irs.aaswrapper.submodel.domain;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.any;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.tomakehurst.wiremock.WireMockServer;
import net.catenax.irs.util.JsonUtil;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

class SubmodelFacadeWiremockTest {

    private final static String url = "https://edc.io/BPNL0000000BB2OK/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";
    private final JsonUtil jsonUtil = new JsonUtil();
    private final RestTemplate restTemplate = new RestTemplate();
    private WireMockServer wireMockServer;
    private SubmodelFacade submodelFacade;

    @BeforeEach
    void configureSystemUnderTest() {
        this.wireMockServer = new WireMockServer(options().dynamicPort());
        this.wireMockServer.start();
        configureFor(this.wireMockServer.port());
        SubmodelClient submodelClient = new SubmodelClientImpl(restTemplate, buildApiMethodUrl() + "/api/service",
                jsonUtil);
        this.submodelFacade = new SubmodelFacade(submodelClient);
    }

    @AfterEach
    void tearDown() {
        this.wireMockServer.stop();
    }

    @Test
    void shouldReturnAssemblyPartRelationshipAsString() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBodyFile("assemblyPartRelationship.json")));

        // Act
        final String submodel = submodelFacade.getSubmodelAsString(url);

        // Assert
        assertThat(submodel).contains("\"catenaXId\": \"urn:uuid:fe99da3d-b0de-4e80-81da-882aebcca978\"");
    }

    @Test
    void shouldReturnMaterialForRecyclingAsString() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBodyFile("materialForRecycling.json")));

        // Act
        final String submodel = submodelFacade.getSubmodelAsString(url);

        // Assert
        assertThat(submodel).contains("\"materialName\": \"Cooper\",");
    }

    @Test
    void shouldReturnObjectAsStringWhenResponseNotJSON() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(200)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBody("test")));

        // Act
        final String submodel = submodelFacade.getSubmodelAsString(url);

        // Assert
        assertThat(submodel).isEqualTo("test");
    }

    @Test
    void shouldThrowExceptionWhenResponse_400() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(400)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBody("{ error: '400'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> submodelFacade.getSubmodelAsString(url);

        // Assert
        assertThatExceptionOfType(RestClientException.class).isThrownBy(throwingCallable);
    }

    @Test
    void shouldThrowExceptionWhenResponse_500() {
        // Arrange
        givenThat(any(anyUrl()).willReturn(aResponse().withStatus(500)
                                                      .withHeader("Content-Type", "application/json;charset=UTF-8")
                                                      .withBody("{ error: '500'}")));

        // Act
        final ThrowableAssert.ThrowingCallable throwingCallable = () -> submodelFacade.getSubmodelAsString(url);

        // Assert
        assertThatExceptionOfType(RestClientException.class).isThrownBy(throwingCallable);
    }

    private String buildApiMethodUrl() {
        return String.format("http://localhost:%d", this.wireMockServer.port());
    }
}
