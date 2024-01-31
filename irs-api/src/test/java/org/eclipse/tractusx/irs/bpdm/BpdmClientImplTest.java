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
package org.eclipse.tractusx.irs.bpdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class BpdmClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final BpdmClient bpdmClient = new BpdmClientImpl(restTemplate, "url/{idType}/{partnerId}");

    @Test
    void shouldCallExternalServiceOnceAndGetBusinessPartnerResponse() {
        final String bpn = "BPNL00000003AYRE";
        final BusinessPartnerResponse mockResponse = BusinessPartnerResponse.builder().bpn(bpn).build();
        doReturn(mockResponse).when(restTemplate).getForObject(any(), eq(BusinessPartnerResponse.class));

        final BusinessPartnerResponse businessPartner = bpdmClient.getBusinessPartner(bpn, "BPN");

        assertThat(businessPartner).isNotNull();
        assertThat(businessPartner.getBpn()).isEqualTo(bpn);
        verify(this.restTemplate, times(1)).getForObject(any(), eq(BusinessPartnerResponse.class));
    }

}
