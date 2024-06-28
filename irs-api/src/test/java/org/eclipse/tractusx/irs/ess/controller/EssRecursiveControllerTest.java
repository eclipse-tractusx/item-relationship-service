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
package org.eclipse.tractusx.irs.ess.controller;

import static io.restassured.RestAssured.given;
import static org.springframework.http.HttpStatus.CREATED;

import java.util.List;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.eclipse.tractusx.irs.ControllerTest;
import org.eclipse.tractusx.irs.TestConfig;
import org.eclipse.tractusx.irs.common.auth.IrsRoles;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationHeader;
import org.eclipse.tractusx.irs.edc.client.model.notification.InvestigationNotificationContent;
import org.eclipse.tractusx.irs.ess.service.EssRecursiveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties = { "digitalTwinRegistry.type=central" })
@ActiveProfiles(profiles = { "test", "local" })
@Import(TestConfig.class)
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class EssRecursiveControllerTest extends ControllerTest {

    private final String path = "/ess/notification/receive-recursive";

    @MockBean
    private EssRecursiveService essRecursiveService;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void shouldHandleRecursiveBpnInvestigationByNotification() throws Exception {
        authenticateWith(IrsRoles.VIEW_IRS);

        given().port(port).contentType(ContentType.JSON).body(prepareNotification()).post(path)
               .then().statusCode(CREATED.value());
    }

    private EdcNotification<InvestigationNotificationContent> prepareNotification() {
        final EdcNotificationHeader header = EdcNotificationHeader.builder()
                                                                  .notificationId("notification-id")
                                                                  .senderEdc("senderEdc")
                                                                  .senderBpn("senderBpn")
                                                                  .recipientBpn("recipientBpn")
                                                                  .replyAssetId("ess-response-asset")
                                                                  .replyAssetSubPath("")
                                                                  .notificationType("ess-supplier-request")
                                                                  .build();

        return EdcNotification.<InvestigationNotificationContent>builder()
                              .header(header)
                              .content(InvestigationNotificationContent.builder()
                                                                       .concernedCatenaXIds(List.of("cat1", "cat2"))
                                                                       .incidentBPNSs(List.of("BPNS000000000BBB"))
                                                                       .build())
                              .build();
    }

}