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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.TransferProcessException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.Response;
import org.eclipse.tractusx.irs.edc.client.model.TransferProcessResponse;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractNegotiationServiceTest {

    private static final String CONNECTOR_URL = "dummyConnectorUrl";

    @InjectMocks
    private ContractNegotiationService testee;
    @Mock
    private EdcControlPlaneClient edcControlPlaneClient;
    @Spy
    private EdcConfiguration config = new EdcConfiguration();

    @Mock
    private PolicyCheckerService policyCheckerService;

    private static Policy createPolicy(final String assetId) {
        final Permission permission = Permission.Builder.newInstance().target(assetId).build();
        return Policy.Builder.newInstance().permission(permission).build();
    }

    private static CatalogItem createCatalogItem(final String assetId, final String offerId) {
        final Policy policy = createPolicy(assetId);
        return CatalogItem.builder().itemId(assetId).policy(policy).assetPropId(assetId).offerId(offerId).build();
    }

    @Test
    void shouldNegotiateSuccessfully()
            throws ContractNegotiationException, UsagePolicyException, TransferProcessException {
        // arrange
        final var assetId = "testTarget";
        final String offerId = "offerId";
        final CatalogItem catalogItem = createCatalogItem(assetId, offerId);
        when(policyCheckerService.isValid(any())).thenReturn(Boolean.TRUE);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                Response.builder().responseId("negotiationId").build());
        CompletableFuture<NegotiationResponse> response = CompletableFuture.completedFuture(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(response);
        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                Response.builder().responseId("transferProcessId").build());
        when(edcControlPlaneClient.getTransferProcess(any())).thenReturn(
                CompletableFuture.completedFuture(TransferProcessResponse.builder().build()));

        // act
        NegotiationResponse result = testee.negotiate(CONNECTOR_URL, catalogItem,
                new EndpointDataReferenceStatus(null, EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW));

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getContractAgreementId()).isEqualTo("agreementId");
    }

    @Test
    void shouldThrowErrorWhenRetrievingNegotiationResult() {
        // arrange
        final var assetId = "testTarget";
        final String offerId = "offerId";
        final CatalogItem catalogItem = createCatalogItem(assetId, offerId);
        when(policyCheckerService.isValid(any())).thenReturn(Boolean.TRUE);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                Response.builder().responseId("negotiationId").build());
        CompletableFuture<NegotiationResponse> response = CompletableFuture.failedFuture(
                new RuntimeException("Test exception"));
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(response);

        // act & assert
        assertThatThrownBy(() -> testee.negotiate(CONNECTOR_URL, catalogItem, new EndpointDataReferenceStatus(null,
                EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW))).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldThrowErrorWhenRetrievingTransferResult() {
        // arrange
        final var assetId = "testTarget";
        final String offerId = "offerId";
        final CatalogItem catalogItem = createCatalogItem(assetId, offerId);
        when(policyCheckerService.isValid(any())).thenReturn(Boolean.TRUE);

        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                Response.builder().responseId("negotiationId").build());
        CompletableFuture<NegotiationResponse> negotiationResponse = CompletableFuture.completedFuture(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(negotiationResponse);

        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                Response.builder().responseId("transferProcessId").build());
        CompletableFuture<TransferProcessResponse> transferError = CompletableFuture.failedFuture(
                new RuntimeException("Test exception"));
        when(edcControlPlaneClient.getTransferProcess(any())).thenReturn(transferError);

        // act & assert
        assertThatThrownBy(() -> testee.negotiate(CONNECTOR_URL, catalogItem, new EndpointDataReferenceStatus(null,
                EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW))).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldThrowErrorWhenPolicyCheckerReturnFalse() {
        // arrange
        final var assetId = "testTarget";
        final CatalogItem catalogItem = CatalogItem.builder()
                                                   .itemId(assetId)
                                                   .policy(createPolicy(assetId))
                                                   .assetPropId(assetId)
                                                   .build();
        when(policyCheckerService.isValid(any())).thenReturn(Boolean.FALSE);

        // act & assert
        assertThatThrownBy(() -> testee.negotiate(CONNECTOR_URL, catalogItem, new EndpointDataReferenceStatus(null,
                EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW))).isInstanceOf(EdcClientException.class);
    }

    @Test
    void shouldStartNegotiationProcessWhenTokenStatusIsRequiredNew()
            throws TransferProcessException, UsagePolicyException, ContractNegotiationException {
        // given
        final var assetId = "testTarget";
        final String offerId = "offerId";
        final CatalogItem catalogItem = createCatalogItem(assetId, offerId);
        when(policyCheckerService.isValid(any())).thenReturn(Boolean.TRUE);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                Response.builder().responseId("negotiationId").build());
        CompletableFuture<NegotiationResponse> negotiationResponse = CompletableFuture.completedFuture(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(negotiationResponse);
        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                Response.builder().responseId("transferProcessId").build());
        when(edcControlPlaneClient.getTransferProcess(any())).thenReturn(
                CompletableFuture.completedFuture(TransferProcessResponse.builder().build()));

        // when
        testee.negotiate(CONNECTOR_URL, catalogItem,
                new EndpointDataReferenceStatus(null, EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW));

        // then
        verify(edcControlPlaneClient).startNegotiations(any());
    }

    @Test
    void shouldStartNegotiationProcessWhenTokenStatusIsMissing()
            throws TransferProcessException, UsagePolicyException, ContractNegotiationException {
        // given
        final var assetId = "testTarget";
        final String offerId = "offerId";
        final CatalogItem catalogItem = createCatalogItem(assetId, offerId);
        when(policyCheckerService.isValid(any())).thenReturn(Boolean.TRUE);
        when(edcControlPlaneClient.startNegotiations(any())).thenReturn(
                Response.builder().responseId("negotiationId").build());
        CompletableFuture<NegotiationResponse> negotiationResponse = CompletableFuture.completedFuture(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        when(edcControlPlaneClient.getNegotiationResult(any())).thenReturn(negotiationResponse);
        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                Response.builder().responseId("transferProcessId").build());
        when(edcControlPlaneClient.getTransferProcess(any())).thenReturn(
                CompletableFuture.completedFuture(TransferProcessResponse.builder().build()));

        // when
        testee.negotiate(CONNECTOR_URL, catalogItem, null);

        // then
        verify(edcControlPlaneClient).startNegotiations(any());
    }

    @Test
    void shouldNotStartNewNegotiationWhenTokenIsExpired()
            throws TransferProcessException, UsagePolicyException, ContractNegotiationException {
        // given
        final var assetId = "testTarget";
        final String offerId = "offerId";
        final CatalogItem catalogItem = createCatalogItem(assetId, offerId);

        when(edcControlPlaneClient.startTransferProcess(any())).thenReturn(
                Response.builder().responseId("transferProcessId").build());
        when(edcControlPlaneClient.getTransferProcess(any())).thenReturn(
                CompletableFuture.completedFuture(TransferProcessResponse.builder().build()));
        final String encodedAuthCode = "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE3MDA3NDc0NjMsImRhZCI6IkFXanRhclZySVdtaVE4V1R4VGV2YVhUS1p5SERUZ3pkWG1oMWpkdTR3QUxkTTZVaEgyVHVCOXhhS2Z6TmJQQTZVQVhnVDc2NytPMTgwUXltMGNFdks0NGxzakZQbkROTFQwOEpBOGxvazg0a3hScktFdSswRDZFMmlzTUNPM1Zaa2ZmNDB1U2d6YmJVTDR1djNGNGYxdVp6RnRZT2VvcDdjOUFUc2k1WHhyaGZLdkdDOERrRi9idTBaQmY1US9nMy9xS3QwY0FmcW9TNUxWSlN1SVhKdUk4S2JNSldob2hLZ1NRb2tLMWxNQzVpSVRhbWZ2L0FvZUNXMnB1bkc1R0twM1NTak9Da1hJL3ZXQlRTNWVFTzRpYkwvL1JSZGdJdVp3K2dzcHVVMkFtYm04YzZFQjViQjlPYWhhYjRzRCtnTldDcWFZazZWQ1p4Ty9xaUlKT1RZVGo0b3pDVnovcE5VN0l6R1hBWjNaamtNRWRMbUJVclhDSFRtaU1GeGd5bkxQN2hBVmN5M2NOVGhIb0FtZDI1c2ZwbUdTeHViS1FmSHM2RUNFajByYS9lT001dHNqZ2l5N3JOOUhQT25zWFppL01yMWR1UDE4c0hGQmVLeWFNNkwveFN6TTlCUVplb0Z2TVE5VmlmSm1hMUQ5WklrNUhhTnBmc0RQSElBK0VLL0hGSG1mRWk1TGhoS3lVY3Q2VGpQb0VKa25WamJBSU01VXUzSW1oV3NFdkVLR3lvZXQ0UEhYeHBVWlhQWFdjSjd0dk8zZms3YjczOEVRVEV6Y29KTFdZd0wrRDZac1hJVjd4UzFOOTV4WDlqcU9aUjBuSGsxb3lmT21KUTg5UkpxZy91eE01L3lPcFJUWU01OWJGTzJBRWVDa0UwM0k2aUY0NE1xQU1VVzM4bGk4eDFkY3A0ajQ3Z0lKMlFrWTM5bHI1VXRpbEFzcjVZMkN5Nm5hcVFIeFU2TW1LS0RFdVQrUXdxTFZGYVB5SC9ZM2dTOFpZdlh3TlVOams4S2k4T2JFTTVUY25nUWxVK0Y0dE9BeTQ0bjNPckpWYlhIcVBud1N4L2ZmbTdKdVRnZjRlMVpPcThhdz09IiwiY2lkIjoiT1dZeFlqa3dZelV0TldFNVlTMDBaR1UyTFRoaVpXTXROalprWTJaaVlqTXdPREZtOmNtVm5hWE4wY25rdFlYTnpaWFE9Ok1XWXlZMll5TmpVdE56STROQzAwTnpFNUxXSTNOVGt0TWpSbFpqY3habU13WWpaaSJ9.HDhEMOGVlwOTAFIKCCUzf_twg08K-rQwElNS2foinB9hRM-htLwoXayMtbXdXS4pFevRn1AXhzcxd5ur7gslJdsNohTiwVP0lXRd0cehWMpRKdDiUCLn4lh0A2fFTYpoX4WIXvqldAADxi0qDmZqLTZdSOqkM40t-Fq8esyFMrO_uC6GL8LUQMLML1HV6nqGkqp-VELEoOMTV1-aVQ-OEv0J24epjNyesx448v0yylhS_vxPmay1zeSJgDCwqzSuY5-EkyIfCN1XqbynMZiNtD2FLbAig0KTAL2rN6WMufSWMjgLUU0mhRbd9bWvqs3JKLVzvagQgS3hMTj5a-C2Tw";

        // when
        testee.negotiate(CONNECTOR_URL, catalogItem, new EndpointDataReferenceStatus(
                EndpointDataReference.Builder.newInstance().authKey("").authCode(encodedAuthCode).endpoint("").build(),
                EndpointDataReferenceStatus.TokenStatus.EXPIRED));

        // then
        verify(edcControlPlaneClient, never()).startNegotiations(any());
    }

    @Test
    void shouldThrowInvalidStateExceptionWhenTokenIsValid() {
        // given
        final var assetId = "testTarget";
        final String offerId = "offerId";
        final CatalogItem catalogItem = createCatalogItem(assetId, offerId);

        // when
        // then
        assertThatThrownBy(() -> testee.negotiate(CONNECTOR_URL, catalogItem, new EndpointDataReferenceStatus(
                EndpointDataReference.Builder.newInstance().authKey("").endpoint("").authCode("").build(),
                EndpointDataReferenceStatus.TokenStatus.VALID))).isInstanceOf(IllegalStateException.class);
    }

}