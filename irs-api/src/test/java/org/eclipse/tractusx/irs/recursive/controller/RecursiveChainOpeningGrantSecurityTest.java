/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.OK;

import java.time.ZonedDateTime;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.TestConfig;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChainOpeningGrant;
import org.eclipse.tractusx.irs.recursive.model.RecursiveUseCase;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Verifies that chain opening grant write operations (register/replace/delete) require the
 * {@code admin_irs} role, while reading (list) stays available to {@code view_irs}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central" })
@ActiveProfiles(profiles = { "test", "local" })
@Import(TestConfig.class)
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class RecursiveChainOpeningGrantSecurityTest extends ControllerTest {

    private static final String GRANTS_PATH = "/irs/recursive/chain-openings/grants";
    private static final String VALID_GRANT_BODY =
            "{\"openingId\":\"opening-42\",\"useCase\":\"PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE\","
                    + "\"requesterBpn\":\"BPNL0000ATLS0001\","
                    + "\"globalAssetId\":\"urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b\"}";

    @MockBean
    private RecursiveChainOpeningGrantService grantService;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void viewRoleMustNotRegisterGrant() {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port).contentType(ContentType.JSON).body(VALID_GRANT_BODY)
               .post(GRANTS_PATH)
               .then().statusCode(FORBIDDEN.value())
               .body("code", equalTo("AUTHORIZATION_FAILED"));
    }

    @Test
    void viewRoleMustNotReplaceGrant() {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port).contentType(ContentType.JSON).body(VALID_GRANT_BODY)
               .put(GRANTS_PATH)
               .then().statusCode(FORBIDDEN.value());
    }

    @Test
    void viewRoleMustNotDeleteGrant() {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port).queryParam("openingId", "opening-42")
               .queryParam("globalAssetId", "urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
               .queryParam("requesterBpn", "BPNL0000ATLS0001")
               .queryParam("useCase", "PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE")
               .delete(GRANTS_PATH)
               .then().statusCode(FORBIDDEN.value());
    }

    @Test
    void viewRoleMayListGrants() {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port).queryParam("openingId", "opening-42")
               .get(GRANTS_PATH)
               .then().statusCode(OK.value());
    }

    @Test
    void adminRoleMayRegisterGrant() {
        authenticateWith(IrsRoles.ADMIN_IRS);
        final ZonedDateTime now = ZonedDateTime.now();
        Mockito.when(grantService.registerGrant(any())).thenReturn(RecursiveChainOpeningGrant.builder()
                .openingId("opening-42")
                .useCase(RecursiveUseCase.PURIS_ITEM_STOCK_ANONYMIZED_RECURSIVE)
                .requesterBpn("BPNL0000ATLS0001")
                .globalAssetId("urn:uuid:68904173-ad59-4a77-8412-3e73fcafbd8b")
                .createdAt(now)
                .updatedAt(now)
                .build());

        given().port(port).contentType(ContentType.JSON).body(VALID_GRANT_BODY)
               .post(GRANTS_PATH)
               .then().statusCode(CREATED.value());
    }

    @Test
    void unsupportedRecursiveMethodUsesRecursiveErrorResponse() {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port)
               .delete("/irs/recursive/jobs")
               .then().statusCode(METHOD_NOT_ALLOWED.value())
               .body("code", equalTo("METHOD_NOT_ALLOWED"));
    }
}
