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

class EdcContractAgreementNegotiationResponseTest {
    @Test
    void shouldParseEdcContractAgreementNegotiationResponse() throws JsonProcessingException {
        //GIVEN
        String contractAgreementResponse = """
                {
                	"@type": "edc:ContractNegotiation",
                	"@id": "a521a424-a07b-4a08-a845-676f2ddd0e89",
                	"edc:type": "CONSUMER",
                	"edc:protocol": "dataspace-protocol-http",
                	"edc:state": "FINALIZED",
                	"edc:counterPartyId": "BPNL00000003CML1",
                	"edc:counterPartyAddress": "https://trace-x-edc-e2e-a.dev.demo.catena-x.net/api/v1/dsp",
                	"edc:callbackAddresses": [],
                	"edc:createdAt": 1708590580001,
                	"edc:contractAgreementId": "ODA3MmUyNTQtOGNlZi00YzQ2LTljNGYtNGYzNjE2YjQ5NTZl:cmVnaXN0cnktYXNzZXQ=:MDljNDMzY2EtODI5OS00OGE3LWI0MjYtNzZmZjJmODE1ZWE2",
                	"@context": {
                		"dct": "https://purl.org/dc/terms/",
                		"tx": "https://w3id.org/tractusx/v0.0.1/ns/",
                		"edc": "https://w3id.org/edc/v0.0.1/ns/",
                		"dcat": "https://www.w3.org/ns/dcat/",
                		"odrl": "http://www.w3.org/ns/odrl/2/",
                		"dspace": "https://w3id.org/dspace/v0.8/"
                	}
                }
                """;

        final ObjectMapper objectMapper = new ObjectMapper();

        //WHEN
        final EdcContractAgreementNegotiationResponse contractAgreementNegotiation = objectMapper.readValue(
                contractAgreementResponse, EdcContractAgreementNegotiationResponse.class);
        //THEN
        assertThat(contractAgreementNegotiation).isNotNull();
        assertThat(contractAgreementNegotiation.correlationId()).isEqualTo("a521a424-a07b-4a08-a845-676f2ddd0e89");

    }

}