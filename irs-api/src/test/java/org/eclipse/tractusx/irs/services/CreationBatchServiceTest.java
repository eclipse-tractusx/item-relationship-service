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
package org.eclipse.tractusx.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.common.auth.SecurityHelperService;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationBatchOrder;
import org.eclipse.tractusx.irs.component.enums.BatchStrategy;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.configuration.IrsConfiguration;
import org.eclipse.tractusx.irs.connector.batch.Batch;
import org.eclipse.tractusx.irs.connector.batch.BatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.BatchStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchOrderStore;
import org.eclipse.tractusx.irs.connector.batch.InMemoryBatchStore;
import org.eclipse.tractusx.irs.connector.batch.JobProgress;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class CreationBatchServiceTest {

    public static final String FIRST_GLOBAL_ASSET_ID = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0";
    public static final String SECOND_GLOBAL_ASSET_ID = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b1";
    private BatchOrderStore batchOrderStore;
    private BatchStore batchStore;
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final JobEventLinkedQueueListener jobEventLinkedQueueListener = mock(JobEventLinkedQueueListener.class);
    private final SecurityHelperService securityHelperService = mock(SecurityHelperService.class);
    private final IrsConfiguration irsConfiguration = mock(IrsConfiguration.class);
    private final static String EXAMPLE_URL = "https://exampleUrl.com";
    private CreationBatchService service;

    @BeforeEach
    void beforeEach() {
        batchOrderStore = new InMemoryBatchOrderStore();
        batchStore = new InMemoryBatchStore();
        service = new CreationBatchService(batchOrderStore, batchStore, applicationEventPublisher,
                jobEventLinkedQueueListener, securityHelperService, irsConfiguration);
    }

    @Test
    void shouldStoreRegularBatchOrder() throws MalformedURLException {
        // given
        final RegisterBatchOrder registerBatchOrder = exampleBatchRequest();
        given(irsConfiguration.getApiUrl()).willReturn(new URL(EXAMPLE_URL));

        // when
        final UUID batchOrderId = service.create(registerBatchOrder);

        // then
        assertThat(batchOrderId).isNotNull();
        assertThat(batchOrderStore.findAll()).hasSize(1);
        assertThat(batchStore.findAll()).hasSize(1);

        Batch actual = batchStore.findAll().stream().findFirst().orElseThrow();
        assertThat(actual.getJobProgressList().stream().map(JobProgress::getIdentificationKey).map(
                PartChainIdentificationKey::getGlobalAssetId).collect(
                Collectors.toList())).containsOnly(FIRST_GLOBAL_ASSET_ID, SECOND_GLOBAL_ASSET_ID);
    }

    @Test
    void shouldStoreESSBatchOrder() throws MalformedURLException {
        // given
        final RegisterBpnInvestigationBatchOrder registerBatchOrder = exampleESSBatchRequest();
        given(irsConfiguration.getApiUrl()).willReturn(new URL(EXAMPLE_URL));

        // when
        final UUID batchOrderId = service.create(registerBatchOrder);

        // then
        assertThat(batchOrderId).isNotNull();
        assertThat(batchOrderStore.findAll()).hasSize(1);
        assertThat(batchStore.findAll()).hasSize(1);

        Batch actual = batchStore.findAll().stream().findFirst().orElseThrow();
        assertThat(actual.getJobProgressList().stream().map(JobProgress::getIdentificationKey).map(
                PartChainIdentificationKey::getGlobalAssetId).collect(
                Collectors.toList())).containsOnly(FIRST_GLOBAL_ASSET_ID, SECOND_GLOBAL_ASSET_ID);
    }

    @Test
    void shouldSplitIdentificationKeysIdIntoBatches() throws MalformedURLException {
        // given
        final List<PartChainIdentificationKey> globalAssetIds = IntStream.range(1, 21)
                                                                         .mapToObj(i -> PartChainIdentificationKey.builder()
                                                                                                                  .globalAssetId(String.valueOf(i))
                                                                                 .bpn("BPN" + i).build()
                                                                         ).toList();
        final int batchSize = 3;
        given(irsConfiguration.getApiUrl()).willReturn(new URL(EXAMPLE_URL));

        // when
        final List<Batch> batches = service.createBatches(globalAssetIds, batchSize, UUID.randomUUID(), securityHelperService.getClientIdClaim());

        // then
        assertThat(batches).hasSize(7);
        assertThat(batches.get(0).getJobProgressList().stream().map(JobProgress::getIdentificationKey).map(
                PartChainIdentificationKey::getGlobalAssetId).toList()).containsExactly("1", "2", "3");
        assertThat(batches.get(6).getJobProgressList().stream().map(JobProgress::getIdentificationKey).map(
                PartChainIdentificationKey::getGlobalAssetId).toList()).containsExactly("19", "20");
        assertThat(batches.get(0).getBatchUrl()).isEqualTo(
                EXAMPLE_URL + "/" + IrsApplication.API_PREFIX +
                        "/orders/" + batches.get(0).getBatchOrderId() + "/batches/" + batches.get(0).getBatchId()
        );

    }

    private static RegisterBatchOrder exampleBatchRequest() {
        return RegisterBatchOrder.builder()
                                 .keys(Set.of(PartChainIdentificationKey.builder().globalAssetId(FIRST_GLOBAL_ASSET_ID).build(),
                                         PartChainIdentificationKey.builder().globalAssetId(SECOND_GLOBAL_ASSET_ID).build()))
                                 .bomLifecycle(BomLifecycle.AS_PLANNED)
                                 .aspects(List.of("aspects"))
                                 .depth(1)
                                 .direction(Direction.DOWNWARD)
                                 .collectAspects(true)
                                 .timeout(1000)
                                 .jobTimeout(500)
                                 .batchStrategy(BatchStrategy.PRESERVE_JOB_ORDER)
                                 .callbackUrl(EXAMPLE_URL)
                                 .batchSize(10)
                                 .build();
    }

    private static RegisterBpnInvestigationBatchOrder exampleESSBatchRequest() {
        return RegisterBpnInvestigationBatchOrder.builder()
                                 .keys(Set.of(PartChainIdentificationKey.builder().globalAssetId(FIRST_GLOBAL_ASSET_ID).build(),
                                         PartChainIdentificationKey.builder().globalAssetId(SECOND_GLOBAL_ASSET_ID).build()))
                                 .bomLifecycle(BomLifecycle.AS_PLANNED)
                                 .timeout(1000)
                                 .jobTimeout(500)
                                 .batchStrategy(BatchStrategy.PRESERVE_JOB_ORDER)
                                 .callbackUrl(EXAMPLE_URL)
                                 .batchSize(10)
                                 .build();
    }

}