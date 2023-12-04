package org.eclipse.tractusx.irs.edc.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.edc.client.EndpointDataReferenceStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
@ExtendWith(MockitoExtension.class)
class EndpointDataReferenceCacheServiceTest {

    @Mock
    private EndpointDataReferenceStorage endpointDataReferenceStorage;

    @InjectMocks
    private EndpointDataReferenceCacheService endpointDataReferenceCacheService;

    @Test
    void shouldReturnStatusRequiredNewWhenThereIsNoRecordInCache() {
        // given
        final String assetId = "assetId";
        when(endpointDataReferenceStorage.get(assetId)).thenReturn(Optional.empty());

        // when
        final EndpointDataReferenceStatus endpointDataReference = endpointDataReferenceCacheService.getEndpointDataReference(
                assetId);

        // then
        assertThat(endpointDataReference.endpointDataReference()).isNull();
        assertThat(endpointDataReference.tokenStatus()).isEqualTo(EndpointDataReferenceStatus.TokenStatus.REQUIRED_NEW);
    }

    @Test
    void shouldReturnStatusExpiredAndCorrectEndpointDataReferenceWhenThereIsRecordInCacheWithExpiredToken() {
        // given
        final String assetId = "assetId";
        final String authKey = "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjE3MDA3NDc0NjMsImRhZCI6IkFXanRhclZySVdtaVE4V1R4VGV2YVhUS1p5SERUZ3pkWG1oMWpkdTR3QUxkTTZVaEgyVHVCOXhhS2Z6TmJQQTZVQVhnVDc2NytPMTgwUXltMGNFdks0NGxzakZQbkROTFQwOEpBOGxvazg0a3hScktFdSswRDZFMmlzTUNPM1Zaa2ZmNDB1U2d6YmJVTDR1djNGNGYxdVp6RnRZT2VvcDdjOUFUc2k1WHhyaGZLdkdDOERrRi9idTBaQmY1US9nMy9xS3QwY0FmcW9TNUxWSlN1SVhKdUk4S2JNSldob2hLZ1NRb2tLMWxNQzVpSVRhbWZ2L0FvZUNXMnB1bkc1R0twM1NTak9Da1hJL3ZXQlRTNWVFTzRpYkwvL1JSZGdJdVp3K2dzcHVVMkFtYm04YzZFQjViQjlPYWhhYjRzRCtnTldDcWFZazZWQ1p4Ty9xaUlKT1RZVGo0b3pDVnovcE5VN0l6R1hBWjNaamtNRWRMbUJVclhDSFRtaU1GeGd5bkxQN2hBVmN5M2NOVGhIb0FtZDI1c2ZwbUdTeHViS1FmSHM2RUNFajByYS9lT001dHNqZ2l5N3JOOUhQT25zWFppL01yMWR1UDE4c0hGQmVLeWFNNkwveFN6TTlCUVplb0Z2TVE5VmlmSm1hMUQ5WklrNUhhTnBmc0RQSElBK0VLL0hGSG1mRWk1TGhoS3lVY3Q2VGpQb0VKa25WamJBSU01VXUzSW1oV3NFdkVLR3lvZXQ0UEhYeHBVWlhQWFdjSjd0dk8zZms3YjczOEVRVEV6Y29KTFdZd0wrRDZac1hJVjd4UzFOOTV4WDlqcU9aUjBuSGsxb3lmT21KUTg5UkpxZy91eE01L3lPcFJUWU01OWJGTzJBRWVDa0UwM0k2aUY0NE1xQU1VVzM4bGk4eDFkY3A0ajQ3Z0lKMlFrWTM5bHI1VXRpbEFzcjVZMkN5Nm5hcVFIeFU2TW1LS0RFdVQrUXdxTFZGYVB5SC9ZM2dTOFpZdlh3TlVOams4S2k4T2JFTTVUY25nUWxVK0Y0dE9BeTQ0bjNPckpWYlhIcVBud1N4L2ZmbTdKdVRnZjRlMVpPcThhdz09IiwiY2lkIjoiT1dZeFlqa3dZelV0TldFNVlTMDBaR1UyTFRoaVpXTXROalprWTJaaVlqTXdPREZtOmNtVm5hWE4wY25rdFlYTnpaWFE9Ok1XWXlZMll5TmpVdE56STROQzAwTnpFNUxXSTNOVGt0TWpSbFpqY3habU13WWpaaSJ9.HDhEMOGVlwOTAFIKCCUzf_twg08K-rQwElNS2foinB9hRM-htLwoXayMtbXdXS4pFevRn1AXhzcxd5ur7gslJdsNohTiwVP0lXRd0cehWMpRKdDiUCLn4lh0A2fFTYpoX4WIXvqldAADxi0qDmZqLTZdSOqkM40t-Fq8esyFMrO_uC6GL8LUQMLML1HV6nqGkqp-VELEoOMTV1-aVQ-OEv0J24epjNyesx448v0yylhS_vxPmay1zeSJgDCwqzSuY5-EkyIfCN1XqbynMZiNtD2FLbAig0KTAL2rN6WMufSWMjgLUU0mhRbd9bWvqs3JKLVzvagQgS3hMTj5a-C2Tw";
        final EndpointDataReference endpointDataReferenceWithExpiredToken = EndpointDataReference.Builder.newInstance()
                                                                                          .endpoint("")
                                                                                          .authCode(authKey)
                                                                                          .authKey("")
                                                                                          .build();
        when(endpointDataReferenceStorage.get(assetId)).thenReturn(Optional.of(endpointDataReferenceWithExpiredToken));

        // when
        final EndpointDataReferenceStatus endpointDataReference = endpointDataReferenceCacheService.getEndpointDataReference(
                assetId);

        // then
        assertThat(endpointDataReference.endpointDataReference()).isEqualTo(endpointDataReferenceWithExpiredToken);
        assertThat(endpointDataReference.tokenStatus()).isEqualTo(EndpointDataReferenceStatus.TokenStatus.EXPIRED);
    }

    @Test
    void shouldReturnStatusValidAndCorrectEndpointDataReferenceWhenThereIsRecordInCacheWithValidToken() {
        // given
        final String assetId = "assetId";
        final String authCode = "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjk5OTk5OTk5OTksImRhZCI6IkFXanRhclZySVdtaVE4V1R4VGV2YVhUS1p5SERUZ3pkWG1oMWpkdTR3QUxkTTZVaEgyVHVCOXhhS2Z6TmJQQTZVQVhnVDc2NytPMTgwUXltMGNFdks0NGxzakZQbkROTFQwOEpBOGxvazg0a3hScktFdSswRDZFMmlzTUNPM1Zaa2ZmNDB1U2d6YmJVTDR1djNGNGYxdVp6RnRZT2VvcDdjOUFUc2k1WHhyaGZLdkdDOERrRi9idTBaQmY1US9nMy9xS3QwY0FmcW9TNUxWSlN1SVhKdUk4S2JNSldob2hLZ1NRb2tLMWxNQzVpSVRhbWZ2L0FvZUNXMnB1bkc1R0twM1NTak9Da1hJL3ZXQlRTNWVFTzRpYkwvL1JSZGdJdVp3K2dzcHVVMkFtYm04YzZFQjViQjlPYWhhYjRzRCtnTldDcWFZazZWQ1p4Ty9xaUlKT1RZVGo0b3pDVnovcE5VN0l6R1hBWjNaamtNRWRMbUJVclhDSFRtaU1GeGd5bkxQN2hBVmN5M2NOVGhIb0FtZDI1c2ZwbUdTeHViS1FmSHM2RUNFajByYS9lT001dHNqZ2l5N3JOOUhQT25zWFppL01yMWR1UDE4c0hGQmVLeWFNNkwveFN6TTlCUVplb0Z2TVE5VmlmSm1hMUQ5WklrNUhhTnBmc0RQSElBK0VLL0hGSG1mRWk1TGhoS3lVY3Q2VGpQb0VKa25WamJBSU01VXUzSW1oV3NFdkVLR3lvZXQ0UEhYeHBVWlhQWFdjSjd0dk8zZms3YjczOEVRVEV6Y29KTFdZd0wrRDZac1hJVjd4UzFOOTV4WDlqcU9aUjBuSGsxb3lmT21KUTg5UkpxZy91eE01L3lPcFJUWU01OWJGTzJBRWVDa0UwM0k2aUY0NE1xQU1VVzM4bGk4eDFkY3A0ajQ3Z0lKMlFrWTM5bHI1VXRpbEFzcjVZMkN5Nm5hcVFIeFU2TW1LS0RFdVQrUXdxTFZGYVB5SC9ZM2dTOFpZdlh3TlVOams4S2k4T2JFTTVUY25nUWxVK0Y0dE9BeTQ0bjNPckpWYlhIcVBud1N4L2ZmbTdKdVRnZjRlMVpPcThhdz09IiwiY2lkIjoiT1dZeFlqa3dZelV0TldFNVlTMDBaR1UyTFRoaVpXTXROalprWTJaaVlqTXdPREZtOmNtVm5hWE4wY25rdFlYTnpaWFE9Ok1XWXlZMll5TmpVdE56STROQzAwTnpFNUxXSTNOVGt0TWpSbFpqY3habU13WWpaaSJ9.OnqBmy76W6s7e8U9xYxlRPFBS-DyFoVzIMtnw8fSNQ8yd2HkqrRRFvGb8ttTQ0aAxFQjvyN6o59e0j_tAPQkNkXvJf7ahlJGjhlK9sPaFiXqdfYIH9cX0Czc-zsum248Q1WQfcQA8veDFHv09sO3Adr48hLa4-0ZLBu6GX6x1gUT10qKc4pCSzMJFqFLUst6Bi6nFwDHa68V9GjYxmyU-b3wXk5Nt5lkt1ak3rb0Kpd84wyEsNN09x_EC3PPMkI1UtgevhYN_lvtuweRvLgJjCbIHWmxTgewPY7GmYye_IhrgRvum6danyCUjEwp0-vPUpfzUNsvF60CJnk9Jq-jnQ";
        final EndpointDataReference endpointDataReferenceWithExpiredToken = EndpointDataReference.Builder.newInstance()
                                                                                                         .endpoint("")
                                                                                                         .authCode(authCode)
                                                                                                         .authKey("")
                                                                                                         .build();
        when(endpointDataReferenceStorage.get(assetId)).thenReturn(Optional.of(endpointDataReferenceWithExpiredToken));

        // when
        final EndpointDataReferenceStatus endpointDataReference = endpointDataReferenceCacheService.getEndpointDataReference(
                assetId);

        // then
        assertThat(endpointDataReference.endpointDataReference()).isEqualTo(endpointDataReferenceWithExpiredToken);
        assertThat(endpointDataReference.tokenStatus()).isEqualTo(EndpointDataReferenceStatus.TokenStatus.VALID);
    }
}