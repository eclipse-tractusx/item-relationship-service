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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.TestConfig;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.recursive.service.RecursiveJobService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationReceiver;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central" })
@ActiveProfiles(profiles = { "test", "local" })
@Import(TestConfig.class)
class RecursiveEndpointSecurityTest extends ControllerTest {

    private static final String JOBS_PATH = "/irs/recursive/jobs";
    private static final String NOTIFICATIONS_PATH = "/irs/recursive/notifications";
    private static final String SENDER_BPNL = "BPNL0000PARENT01";
    private static final String UNKNOWN_ROLE = "recursive_irs_unknown_role";

    @MockBean
    private RecursiveJobService jobService;

    @MockBean
    private RecursiveNotificationReceiver notificationReceiver;

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureTest() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        when(jobService.getAllJobs()).thenReturn(List.of());
        when(notificationReceiver.receive(any(), any()))
                .thenThrow(new RecursiveNotificationValidationException("Invalid recursive notification header."));
    }

    @ParameterizedTest
    @ValueSource(strings = { IrsRoles.ADMIN_IRS, IrsRoles.VIEW_IRS })
    void allowedRolesMayListRecursiveJobs(final String role) {
        authenticateWith(role);

        given().port(port)
               .get(JOBS_PATH)
               .then().statusCode(OK.value());
    }

    @Test
    void unrelatedRoleMustNotAccessRecursiveJobs() {
        authenticateWith(UNKNOWN_ROLE);

        given().port(port)
               .get(JOBS_PATH)
               .then().statusCode(FORBIDDEN.value());
    }

    @Test
    void missingAuthenticationMustNotAccessRecursiveJobs() {
        rejectAuthentication();

        given().port(port)
               .get(JOBS_PATH)
               .then().statusCode(UNAUTHORIZED.value());
    }

    @ParameterizedTest
    @ValueSource(strings = { IrsRoles.ADMIN_IRS, IrsRoles.VIEW_IRS })
    void allowedRolesReachNotificationValidation(final String role) {
        authenticateWith(role);

        notificationRequest()
                .then().statusCode(BAD_REQUEST.value());
    }

    @Test
    void unrelatedRoleMustNotAccessRecursiveNotifications() {
        authenticateWith(UNKNOWN_ROLE);

        notificationRequest()
                .then().statusCode(FORBIDDEN.value());
    }

    @Test
    void missingAuthenticationMustNotAccessRecursiveNotifications() {
        rejectAuthentication();

        notificationRequest()
                .then().statusCode(UNAUTHORIZED.value());
    }

    private Response notificationRequest() {
        return given().port(port)
                      .contentType(ContentType.JSON)
                      .header("edc-bpn", SENDER_BPNL)
                      .body("{}")
                      .post(NOTIFICATIONS_PATH);
    }

    private void rejectAuthentication() {
        when(authenticationService.getAuthentication(any(HttpServletRequest.class)))
                .thenThrow(new BadCredentialsException("Wrong ApiKey"));
    }
}
