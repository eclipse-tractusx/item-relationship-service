/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.edc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.dataspaceconnector.spi.types.domain.asset.Asset;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.dataspaceconnector.spi.types.domain.contract.offer.ContractOffer;
import org.eclipse.tractusx.irs.edc.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractNegotiationServiceTest {

    private static final String CONNECTOR_URL = "dummyConnectorUrl";
    @InjectMocks
    private ContractNegotiationService testee;

    @Mock
    private EdcControlPlaneClient edcControlPlaneClient;

    @Test
    void shouldNegotiateSuccessfully() {
        // arrange
        final var assetId = "testTarget";
        final var catalog = mockCatalog(assetId);
        when(edcControlPlaneClient.getCatalog(CONNECTOR_URL)).thenReturn(catalog);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                NegotiationId.builder().id("negotiationId").build());
        NegotiationResponse response = NegotiationResponse.builder().contractAgreementId("agreementId").build();
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(response);
        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                TransferProcessId.builder().id("transferProcessId").build());

        // act
        response = testee.negotiate(CONNECTOR_URL, assetId);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getContractAgreementId()).isEqualTo("agreementId");
    }

    private static Catalog mockCatalog(final String assetId) {
        final var catalog = mock(Catalog.class);
        final var contractOffer = mock(ContractOffer.class);
        final var asset = mock(Asset.class);
        when(asset.getId()).thenReturn(assetId);
        when(contractOffer.getAsset()).thenReturn(asset);
        when(catalog.getContractOffers()).thenReturn(List.of(contractOffer));
        return catalog;
    }
}