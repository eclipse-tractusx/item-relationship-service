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
package org.eclipse.tractusx.irs.controllers;

import static io.restassured.RestAssured.given;
import static org.eclipse.tractusx.irs.util.TestMother.registerJobWithoutDepthAndAspect;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.web.client.HttpServerErrorException.InternalServerError;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.TestConfig;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.services.IrsItemGraphQueryService;
import org.eclipse.tractusx.irs.services.SemanticHubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central" })
@ActiveProfiles(profiles = { "test", "local" })
@Import(TestConfig.class)
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class IrsExceptionHandlerTest extends ControllerTest {

    @MockBean
    private IrsItemGraphQueryService service;
    @MockBean
    private SemanticHubService semanticHubService;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void handleAll() {
        authenticateWith(IrsRoles.VIEW_IRS);

        when(service.registerItemJob(any())).thenThrow(InternalServerError.class);

        given().port(port).contentType(ContentType.JSON).body(registerJobWithoutDepthAndAspect()).post("/irs/jobs")
               .then().statusCode(INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldReturn500WhenGetSemanticModelsFails() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        when(semanticHubService.getAllAspectModels()).thenThrow(InternalServerError.class);

        given().port(port).get("/irs/aspectmodels")
               .then().statusCode(INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void shouldReturn400WhenProvidingBadInput() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        when(semanticHubService.getAllAspectModels()).thenThrow(IllegalArgumentException.class);

        given().port(port).get("/irs/aspectmodels")
               .then().statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldReturn400WhenCatchingIllegalStateException() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        when(semanticHubService.getAllAspectModels()).thenThrow(IllegalStateException.class);

        given().port(port).get("/irs/aspectmodels")
               .then().statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldReturn400WhenCatchingMethodArgumentTypeMismatchException() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        when(semanticHubService.getAllAspectModels()).thenThrow(MethodArgumentTypeMismatchException.class);

        given().port(port).get("/irs/aspectmodels")
               .then().statusCode(BAD_REQUEST.value());
    }

    @Test
    void shouldReturn403WhenRightsAreMissing() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        when(semanticHubService.getAllAspectModels()).thenThrow(AccessDeniedException.class);

        given().port(port).get("/irs/aspectmodels")
               .then().statusCode(FORBIDDEN.value());
    }
}