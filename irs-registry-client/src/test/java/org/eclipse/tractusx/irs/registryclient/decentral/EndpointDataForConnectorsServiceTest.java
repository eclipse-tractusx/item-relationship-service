/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.registryclient.decentral;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.PreferredConnectorEndpointsCache;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class EndpointDataForConnectorsServiceTest {

    private static final String CONNECTION_ONE_ADDRESS = "connectionOneAddress";
    private static final String CONNECTION_TWO_ADDRESS = "connectionTwoAddress";
    private static final String CONNECTION_ONE_CONTRACT_ID = "id1";
    private static final String CONNECTION_TWO_CONTRACT_ID = "id2";
    private static final String BPN = "bpn";

    private static final EndpointDataReference CONNECTION_ONE_DATA_REF = //
            EndpointDataReference.Builder.newInstance()
                                         .endpoint(CONNECTION_ONE_ADDRESS)
                                         .contractId(CONNECTION_ONE_CONTRACT_ID)
                                         .id("test1")
                                         .authKey(HttpHeaders.AUTHORIZATION)
                                         .authCode("authCode")
                                         .build();

    private static final EndpointDataReference CONNECTION_TWO_DATA_REF =  //
            EndpointDataReference.Builder.newInstance()
                                         .endpoint(CONNECTION_TWO_ADDRESS)
                                         .contractId(CONNECTION_TWO_CONTRACT_ID)
                                         .id("test1")
                                         .authKey(HttpHeaders.AUTHORIZATION)
                                         .authCode("authCode")
                                         .build();

    private final EdcEndpointReferenceRetriever edcSubmodelFacade = mock(EdcEndpointReferenceRetriever.class);

    private final EndpointDataForConnectorsService sut = new EndpointDataForConnectorsService(edcSubmodelFacade, new PreferredConnectorEndpointsCache());

    @Test
    void shouldReturnExpectedEndpointDataReference() throws EdcRetrieverException {

        // GIVEN
        when(edcSubmodelFacade.getEndpointReferencesForAsset(CONNECTION_ONE_ADDRESS, BPN)).thenReturn(
                List.of(CompletableFuture.completedFuture(CONNECTION_ONE_DATA_REF)));

        // WHEN
        final List<CompletableFuture<EndpointDataReference>> endpointDataReferences = sut.createFindEndpointDataForConnectorsFutures(
                Collections.singletonList(CONNECTION_ONE_ADDRESS), BPN);

        // THEN
        assertThat(endpointDataReferences).isNotEmpty()
                                          .extracting(CompletableFuture::get)
                                          .isNotEmpty()
                                          .extracting(EndpointDataReference::getEndpoint)
                                          .contains(CONNECTION_ONE_ADDRESS);
    }

    @Test
    void shouldReturnExpectedEndpointDataReferenceFromSecondConnectionEndpoint() throws EdcRetrieverException {

        // GIVEN

        // a first endpoint failing (1)
        when(edcSubmodelFacade.getEndpointReferencesForAsset(CONNECTION_ONE_ADDRESS, BPN)).thenThrow(
                new EdcRetrieverException.Builder(new EdcClientException("EdcClientException")).build());

        // and a second endpoint returning successfully (2)
        when(edcSubmodelFacade.getEndpointReferencesForAsset(CONNECTION_TWO_ADDRESS, BPN)).thenReturn(
                List.of(CompletableFuture.completedFuture(CONNECTION_TWO_DATA_REF)));

        // WHEN
        final List<CompletableFuture<EndpointDataReference>> dataRefFutures = //
                sut.createFindEndpointDataForConnectorsFutures(List.of(CONNECTION_ONE_ADDRESS, // (1)
                        CONNECTION_TWO_ADDRESS // (2)
                ), BPN);

        // THEN
        final List<EndpointDataReference> dataReferences = //
                dataRefFutures.stream()
                              .map(EndpointDataForConnectorsServiceTest::executeFutureMappingErrorsToNull)
                              .filter(Objects::nonNull)
                              .toList();

        assertThat(dataReferences).isNotEmpty() //
                                  .extracting(EndpointDataReference::getEndpoint) //
                                  .contains(CONNECTION_TWO_ADDRESS);
    }

    private static EndpointDataReference executeFutureMappingErrorsToNull(
            final CompletableFuture<EndpointDataReference> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            // ignore
            return null;
        }
    }

    @Test
    void shouldThrowExceptionWhenConnectorEndpointsNotReachable() throws EdcRetrieverException {

        // GIVEN
        when(edcSubmodelFacade.getEndpointReferencesForAsset(anyString(), eq(BPN))).thenThrow(
                new EdcRetrieverException.Builder(new EdcClientException("EdcClientException")).build());

        // WHEN
        final List<Exception> exceptions = new ArrayList<>();
        final List<String> connectorEndpoints = List.of(CONNECTION_ONE_ADDRESS, CONNECTION_TWO_ADDRESS);
        sut.createFindEndpointDataForConnectorsFutures(connectorEndpoints, BPN) //
           .forEach(future -> {
               try {
                   future.get();
               } catch (InterruptedException | ExecutionException e) {
                   exceptions.add(e);
               }
           });

        // THEN
        assertThat(exceptions).hasSize(connectorEndpoints.size())
                              .extracting(Exception::getCause)
                              .allMatch(exception -> exception instanceof EdcRetrieverException)
                              .extracting("bpn", "edcUrl")
                              .containsExactlyInAnyOrder(tuple(BPN, CONNECTION_ONE_ADDRESS),
                                      tuple(BPN, CONNECTION_TWO_ADDRESS));

    }

}
