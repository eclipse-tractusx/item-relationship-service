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
package org.eclipse.tractusx.irs.bpdm;

import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.eclipse.tractusx.irs.testing.wiremock.WireMockConfig.responseWithStatus;

/**
 * WireMock configurations and requests used for testing BPDM flow.
 */
public final class BpdmWireMockSupport {

    public static final String BPN_PATH = "/legal-entities/";
    public static final String BPDM_URL_TEMPLATE = "http://bpdm.test" + BPN_PATH + "{partnerId}?idType={idType}";

    private BpdmWireMockSupport() {
    }

    public static void bpdmWillReturnCompanyName(final String bpn, final String companyName) {
        stubFor(get(urlPathEqualTo(BPN_PATH + bpn)).willReturn(
                responseWithStatus(200).withBody(bpdmResponse(bpn, companyName))));
    }

    public static void bpdmWillNotFindCompanyName(final String bpn) {
        stubFor(get(urlPathEqualTo(BPN_PATH + bpn)).willReturn(responseWithStatus(404)));
    }

    public static void verifyBpdmWasCalledWithBPN(final String bpn, final int times) {
        verify(exactly(times), getRequestedFor(urlPathEqualTo(BPN_PATH + bpn)));
    }

    public static String bpdmResponse(final String bpn, final String companyName) {
        return """
                {
                	"bpn": "%s",
                	"identifiers": [
                		{
                			"value": "%s",
                			"type": {
                				"technicalKey": "BPN",
                				"name": "Business Partner Number",
                				"url": ""
                			},
                			"issuingBody": {
                				"technicalKey": "CATENAX",
                				"name": "Catena-X",
                				"url": ""
                			},
                			"status": {
                				"technicalKey": "UNKNOWN",
                				"name": "Unknown"
                			}
                		}
                	],
                	"names": [
                		{
                			"value": "%s",
                			"shortName": null,
                			"type": {
                				"technicalKey": "OTHER",
                				"name": "Any other alternative name used for a company, such as a specific language variant.",
                				"url": ""
                			},
                			"language": {
                				"technicalKey": "undefined",
                				"name": "Undefined"
                			}
                		}
                	],
                	"legalForm": null,
                	"status": null,
                	"profileClassifications": [],
                	"types": [
                		{
                			"technicalKey": "UNKNOWN",
                			"name": "Unknown",
                			"url": ""
                		}
                	],
                	"bankAccounts": [],
                	"roles": [],
                	"relations": [],
                	"currentness": "2022-07-26T08:17:38.737578Z"
                }
                 """.formatted(bpn, bpn, companyName);
    }

}