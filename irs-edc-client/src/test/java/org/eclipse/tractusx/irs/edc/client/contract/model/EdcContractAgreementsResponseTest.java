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
package org.eclipse.tractusx.irs.edc.client.contract.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EdcContractAgreementsResponseTest {

    @Test
    void shouldParseEdcContractAgreementsResponse() throws JsonProcessingException {
        //GIVEN
        String contractAgreementResponse = """
                [
                	{
                		"@type": "edc:ContractAgreement",
                		"@id": "OWY1Y2U2OWUtZjI2Yy00MzQ5LTg1MTktNjY2Y2Q3MDgzNWEx:cmVnaXN0cnktYXNzZXQ=:MWYwNmMyYjktN2I2OS00YjhiLTk0NmUtM2FmNzFiYjA2NWU4",
                		"edc:assetId": "registry-asset",
                		"edc:policy": {
                			"@id": "eb0c8486-914a-4d36-84c0-b4971cbc52e4",
                			"@type": "odrl:Set",
                			"odrl:permission": {
                				"odrl:target": "registry-asset",
                				"odrl:action": {
                					"odrl:type": "use"
                				},
                				"odrl:constraint": {
                					"odrl:or": {
                						"odrl:leftOperand": "PURPOSE",
                						"odrl:operator": {
                							"@id": "odrl:eq"
                						},
                						"odrl:rightOperand": "ID 3.1 Trace"
                					}
                				}
                			},
                			"odrl:prohibition": [],
                			"odrl:obligation": [],
                			"odrl:target": "registry-asset"
                		},
                		"edc:contractSigningDate": 1708951087,
                		"edc:consumerId": "BPNL00000003CML1",
                		"edc:providerId": "BPNL00000003CML1",
                		"@context": {
                			"dct": "http://purl.org/dc/terms/",
                			"tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                			"edc": "https://w3id.org/edc/v0.0.1/ns/",
                			"dcat": "https://www.w3.org/ns/dcat/",
                			"odrl": "http://www.w3.org/ns/odrl/2/",
                			"dspace": "https://w3id.org/dspace/v0.8/"
                		}
                	}
                ]
                """;

        final ObjectMapper objectMapper = new ObjectMapper();

        //WHEN
        final EdcContractAgreementsResponse[] contractAgreements = objectMapper.readValue(contractAgreementResponse,
                EdcContractAgreementsResponse[].class);
        //THEN
        assertThat(contractAgreements).isNotNull();
        assertThat(contractAgreements[0].contractAgreementId()).isEqualTo(
                "OWY1Y2U2OWUtZjI2Yy00MzQ5LTg1MTktNjY2Y2Q3MDgzNWEx:cmVnaXN0cnktYXNzZXQ=:MWYwNmMyYjktN2I2OS00YjhiLTk0NmUtM2FmNzFiYjA2NWU4");

    }

}