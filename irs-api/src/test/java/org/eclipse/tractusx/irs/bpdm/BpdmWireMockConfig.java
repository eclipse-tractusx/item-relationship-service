/********************************************************************************
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

/**
 * WireMock configurations and requests used for testing BPDM flow.
 */
public final class BpdmWireMockConfig {

    public static final String BPDM_TEST = "http://bpdm.test/legal-entities/{partnerId}?idType={idType}";

    private BpdmWireMockConfig() {
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