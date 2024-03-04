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
package org.eclipse.tractusx.irs.registryclient.decentral;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.junit.jupiter.api.Test;

class EndpointDataForConnectorsServiceTest {

    private static final String DT_REGISTRY_ASSET_TYPE = "https://w3id.org/edc/v0.0.1/ns/type";
    private static final String DT_REGISTRY_ASSET_VALUE = "data.core.digitalTwinRegistry";

    private static final String connectionOneAddress = "connectionOneAddress";
    private static final String connectionTwoAddress = "connectionTwoAddress";

    private static final EndpointDataReference CONNECTION_ONE_DATA_REF = //
            EndpointDataReference.Builder.newInstance().endpoint(connectionOneAddress).build();

    private static final EndpointDataReference CONNECTION_TWO_DATA_REF =  //
            EndpointDataReference.Builder.newInstance().endpoint(connectionTwoAddress).build();

    private final EdcEndpointReferenceRetriever edcSubmodelFacade = mock(EdcEndpointReferenceRetriever.class);

    private final EndpointDataForConnectorsService sut = new EndpointDataForConnectorsService(edcSubmodelFacade);

    @Test
    void shouldReturnExpectedEndpointDataReference() throws EdcRetrieverException {

        // GIVEN
        when(edcSubmodelFacade.getEndpointReferencesForAsset(connectionOneAddress, DT_REGISTRY_ASSET_TYPE,
                DT_REGISTRY_ASSET_VALUE)).thenReturn(
                List.of(CompletableFuture.completedFuture(CONNECTION_ONE_DATA_REF)));

        // WHEN
        final List<CompletableFuture<EndpointDataReference>> endpointDataReferences = sut.createFindEndpointDataForConnectorsFutures(
                Collections.singletonList(connectionOneAddress));

        // THEN
        assertThat(endpointDataReferences).isNotEmpty()
                                          .extracting(CompletableFuture::get)
                                          .isNotEmpty()
                                          .extracting(EndpointDataReference::getEndpoint)
                                          .contains(connectionOneAddress);
    }

    @Test
    void shouldReturnExpectedEndpointDataReferenceFromSecondConnectionEndpoint() throws EdcRetrieverException {

        // GIVEN

        // a first endpoint failing (1)
        when(edcSubmodelFacade.getEndpointReferencesForAsset(connectionOneAddress, DT_REGISTRY_ASSET_TYPE,
                DT_REGISTRY_ASSET_VALUE)).thenThrow(
                new EdcRetrieverException(new EdcClientException("EdcClientException")));

        // and a second endpoint returning successfully (2)
        when(edcSubmodelFacade.getEndpointReferencesForAsset(connectionTwoAddress, DT_REGISTRY_ASSET_TYPE,
                DT_REGISTRY_ASSET_VALUE)).thenReturn(
                List.of(CompletableFuture.completedFuture(CONNECTION_TWO_DATA_REF)));

        // WHEN
        final List<CompletableFuture<EndpointDataReference>> dataRefFutures = //
                sut.createFindEndpointDataForConnectorsFutures(List.of(connectionOneAddress, // (1)
                        connectionTwoAddress // (2)
                ));

        // THEN
        final List<EndpointDataReference> dataReferences = //
                dataRefFutures.stream()
                              .map(EndpointDataForConnectorsServiceTest::executeFutureMappingErrorsToNull)
                              .filter(Objects::nonNull)
                              .toList();

        assertThat(dataReferences).isNotEmpty() //
                                  .extracting(EndpointDataReference::getEndpoint) //
                                  .contains(connectionTwoAddress);
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
        when(edcSubmodelFacade.getEndpointReferencesForAsset(anyString(), eq(DT_REGISTRY_ASSET_TYPE),
                eq(DT_REGISTRY_ASSET_VALUE))).thenThrow(
                new EdcRetrieverException(new EdcClientException("EdcClientException")));

        // WHEN
        final var exceptions = new ArrayList<>();

        // THEN
        final List<String> connectorEndpoints = List.of(connectionOneAddress, connectionTwoAddress);
        sut.createFindEndpointDataForConnectorsFutures(connectorEndpoints) //
           .forEach(future -> {
               try {
                   future.get();
               } catch (InterruptedException | ExecutionException e) {
                   exceptions.add(e);
               }
           });

        assertThat(exceptions).hasSize(connectorEndpoints.size());
    }

}
