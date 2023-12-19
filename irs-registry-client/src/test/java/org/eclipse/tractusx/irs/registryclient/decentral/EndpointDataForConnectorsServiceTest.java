/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.web.client.RestClientException;

class EndpointDataForConnectorsServiceTest {

    private static final String DT_REGISTRY_ASSET_TYPE = "https://w3id.org/edc/v0.0.1/ns/type";
    private static final String DT_REGISTRY_ASSET_VALUE = "data.core.digitalTwinRegistry";

    private static final String connectionOneAddress = "connectionOneAddress";
    private static final String connectionTwoAddress = "connectionTwoAddress";
    private static final String connectionThreeAddress = "connectionThreeAddress";

    private final EdcEndpointReferenceRetriever edcSubmodelFacade = mock(EdcEndpointReferenceRetriever.class);

    private final EndpointDataForConnectorsService endpointDataForConnectorsService = new EndpointDataForConnectorsService(
            edcSubmodelFacade);

    @Test
    void shouldReturnExpectedEndpointDataReference() throws EdcRetrieverException {
        // given
        when(edcSubmodelFacade.getEndpointReferenceForAsset(connectionOneAddress, DT_REGISTRY_ASSET_TYPE,
                DT_REGISTRY_ASSET_VALUE)).thenReturn(
                EndpointDataReference.Builder.newInstance().endpoint(connectionOneAddress).build());

        // when
        final EndpointDataReference endpointDataReference = endpointDataForConnectorsService.findEndpointDataForConnectors(
                Collections.singletonList(connectionOneAddress));

        // then
        assertThat(endpointDataReference).isNotNull();
        assertThat(endpointDataReference.getEndpoint()).isEqualTo(connectionOneAddress);
    }

    @Test
    void shouldReturnExpectedEndpointDataReferenceFromSecondConnectionEndpoint() throws EdcRetrieverException {

        { // given
            // a failing answer
            when(edcSubmodelFacade.getEndpointReferenceForAsset(connectionOneAddress, DT_REGISTRY_ASSET_TYPE,
                    DT_REGISTRY_ASSET_VALUE)).thenThrow(edcRetrieverException());
            // and a successful answer
            when(edcSubmodelFacade.getEndpointReferenceForAsset(connectionTwoAddress, DT_REGISTRY_ASSET_TYPE,
                    DT_REGISTRY_ASSET_VALUE)).thenReturn(
                    EndpointDataReference.Builder.newInstance().endpoint(connectionTwoAddress).build());
        }

        // when
        final EndpointDataReference endpointDataReference = endpointDataForConnectorsService.findEndpointDataForConnectors(
                List.of(connectionOneAddress, connectionTwoAddress));

        // then
        assertThat(endpointDataReference).isNotNull();
        assertThat(endpointDataReference.getEndpoint()).isEqualTo(connectionTwoAddress);
    }

    @Test
    void shouldReturnExpectedEndpointDataReferenceFromThirdConnectionEndpoint() throws EdcRetrieverException {

        // TODO #214 @mfischer can we improve this test without sleeping?

        { // given
            // a slow answer
            when(edcSubmodelFacade.getEndpointReferenceForAsset(connectionOneAddress, DT_REGISTRY_ASSET_TYPE,
                    DT_REGISTRY_ASSET_VALUE)).thenAnswer((Answer<EndpointDataReference>) invocation -> {
                Thread.sleep(5000);
                return EndpointDataReference.Builder.newInstance().endpoint(connectionOneAddress).build();
            });

            // and a failing answer
            when(edcSubmodelFacade.getEndpointReferenceForAsset(connectionTwoAddress, DT_REGISTRY_ASSET_TYPE,
                    DT_REGISTRY_ASSET_VALUE)).thenThrow(edcRetrieverException());

            // and the fastest answer
            when(edcSubmodelFacade.getEndpointReferenceForAsset(connectionThreeAddress, DT_REGISTRY_ASSET_TYPE,
                    DT_REGISTRY_ASSET_VALUE)).thenAnswer((Answer<EndpointDataReference>) invocation -> {
                Thread.sleep(50);
                return EndpointDataReference.Builder.newInstance().endpoint(connectionThreeAddress).build();
            });
        }

        // when
        final EndpointDataReference endpointDataReference = endpointDataForConnectorsService.findEndpointDataForConnectors(
                List.of(connectionOneAddress, connectionTwoAddress, connectionThreeAddress));

        // then
        assertThat(endpointDataReference).isNotNull();
        assertThat(endpointDataReference.getEndpoint()).isEqualTo(connectionThreeAddress);
    }

    @Test
    void shouldThrowExceptionWhenConnectorEndpointsNotReachable() throws EdcRetrieverException {

        // given #
        // all answers failing
        when(edcSubmodelFacade.getEndpointReferenceForAsset(anyString(), eq(DT_REGISTRY_ASSET_TYPE),
                eq(DT_REGISTRY_ASSET_VALUE))).thenThrow(edcRetrieverException());
        final List<String> connectorEndpoints = List.of(connectionOneAddress, connectionTwoAddress);

        // when + then
        assertThatThrownBy(
                () -> endpointDataForConnectorsService.findEndpointDataForConnectors(connectorEndpoints)).isInstanceOf(
                RestClientException.class).hasMessageContainingAll(connectionOneAddress, connectionTwoAddress);
    }

    private static EdcRetrieverException edcRetrieverException() {
        return new EdcRetrieverException(new EdcClientException("EdcClientException"));
    }

}
