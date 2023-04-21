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
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.edc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.edc.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.edc.exceptions.TimeoutException;
import org.eclipse.tractusx.edc.exceptions.UsagePolicyException;
import org.eclipse.tractusx.edc.model.NegotiationResponse;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.edc.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.common.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.testing.containers.LocalTestDataConfigurationAware;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdcSubmodelClientTest extends LocalTestDataConfigurationAware {

    private static final String ENDPOINT_ADDRESS = "http://localhost/urn:123456/submodel";

    @Mock
    private ContractNegotiationService contractNegotiationService;
    @Mock
    private EdcDataPlaneClient edcDataPlaneClient;
    private final EndpointDataReferenceStorage endpointDataReferenceStorage = new EndpointDataReferenceStorage(
            Duration.ofMinutes(1));
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final TimeMachine clock = new TimeMachine();

    private final AsyncPollingService pollingService = new AsyncPollingService(clock, scheduler);

    @Spy
    private final EdcConfiguration config = new EdcConfiguration();

    private EdcSubmodelClient testee;

    @Mock
    private OutboundMeterRegistryService meterRegistry;
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();

    EdcSubmodelClientTest() throws IOException {
        super();
    }

    @BeforeEach
    void setUp() {
        config.setControlplane(new EdcConfiguration.ControlplaneConfig());
        config.getControlplane().setEndpoint(new EdcConfiguration.ControlplaneConfig.EndpointConfig());
        config.getControlplane().getEndpoint().setData("https://irs-consumer-controlplane.dev.demo.catena-x.net/data");
        config.getControlplane().setRequestTtl(Duration.ofMinutes(10));

        config.setSubmodel(new EdcConfiguration.SubmodelConfig());
        config.getSubmodel().setPath("/submodel");
        config.getSubmodel().setUrnPrefix("/urn");
        config.getSubmodel().setRequestTtl(Duration.ofMinutes(10));
        testee = new EdcSubmodelClientImpl(config, contractNegotiationService, edcDataPlaneClient,
                endpointDataReferenceStorage, pollingService, meterRegistry, retryRegistry);
    }

    @Test
    void shouldRetrieveValidRelationship() throws Exception {
        // arrange
        when(contractNegotiationService.negotiate(any(), any())).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        final EndpointDataReference ref = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put("agreementId", ref);
        final String assemblyPartRelationshipJson = readAssemblyPartRelationshipData();
        when(edcDataPlaneClient.getData(eq(ref), any())).thenReturn(assemblyPartRelationshipJson);

        // act
        final var result = testee.getRelationships(ENDPOINT_ADDRESS, RelationshipAspect.ASSEMBLY_PART_RELATIONSHIP);
        final List<Relationship> resultingRelationships = result.get(5, TimeUnit.SECONDS);

        // assert
        final List<Relationship> expectedRelationships = StringMapper.mapFromString(assemblyPartRelationshipJson,
                RelationshipAspect.ASSEMBLY_PART_RELATIONSHIP.getSubmodelClazz()).asRelationships();
        assertThat(resultingRelationships).isNotNull().containsAll(expectedRelationships);
    }

    @Test
    void shouldSendNotificationSuccessfully() throws Exception {
        // arrange
        final EdcNotification notification = EdcNotification.builder().build();
        when(contractNegotiationService.negotiate(any(), any())).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        final EndpointDataReference ref = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put("agreementId", ref);
        when(edcDataPlaneClient.sendData(eq(ref), any(), eq(notification))).thenReturn(() -> true);

        // act
        final var result = testee.sendNotification(ENDPOINT_ADDRESS, "notify-request-asset", notification);
        final EdcNotificationResponse response = result.get(5, TimeUnit.SECONDS);

        // assert
        assertThat(response.deliveredSuccessfully()).isTrue();
    }

    @Test
    void shouldTimeOut() throws Exception {
        // arrange
        when(contractNegotiationService.negotiate(any(), any())).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());

        // act
        final var result = testee.getRelationships(ENDPOINT_ADDRESS, RelationshipAspect.ASSEMBLY_PART_RELATIONSHIP);
        clock.travelToFuture(Duration.ofMinutes(20));

        // assert
        assertThatThrownBy(result::get).isInstanceOf(ExecutionException.class)
                                       .hasCauseInstanceOf(TimeoutException.class);
    }

    @NotNull
    private String readAssemblyPartRelationshipData() throws IOException {
        final URL resourceAsStream = getClass().getResource("/__files/assemblyPartRelationship.json");
        Objects.requireNonNull(resourceAsStream);
        try {
            return Files.readString(Paths.get(resourceAsStream.toURI()), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingWithCatenaXIdAndAssemblyPartRelationship() throws Exception {
        final String existingCatenaXId = "urn:uuid:15090ed9-0b5c-4761-ad71-c085cf84fa63";
        prepareTestdata(existingCatenaXId, "_assemblyPartRelationship");

        final List<Relationship> submodelResponse = testee.getRelationships(
                                                                  "http://localhost/" + existingCatenaXId + "/submodel", RelationshipAspect.ASSEMBLY_PART_RELATIONSHIP)
                                                          .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).isNotEmpty();
        assertThat(submodelResponse.get(0).getCatenaXId().getGlobalAssetId()).isEqualTo(existingCatenaXId);
        assertThat(submodelResponse).hasSize(32);
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelBomAsPlanned() throws Exception {
        final String catenaXId = "urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c";
        prepareTestdata(catenaXId, "_singleLevelBomAsPlanned");

        final List<Relationship> submodelResponse = testee.getRelationships(
                                                                  "http://localhost/" + catenaXId + "/submodel", RelationshipAspect.SINGLE_LEVEL_BOM_AS_PLANNED)
                                                          .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).isNotEmpty();
        final List<String> childIds = submodelResponse.stream()
                                                      .map(Relationship::getLinkedItem)
                                                      .map(LinkedItem::getChildCatenaXId)
                                                      .map(GlobalAssetIdentification::getGlobalAssetId)
                                                      .collect(Collectors.toList());
        assertThat(childIds).containsAnyOf("urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97");
    }

    @Test
    void shouldReturnEmptyRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelUsageAsBuilt() throws Exception {
        final String catenaXId = "urn:uuid:d7977805-ef52-43c4-9e1d-2f7f0e82c17c";
        prepareTestdata(catenaXId, "_singleLevelUsageAsBuilt");

        final List<Relationship> submodelResponse = testee.getRelationships(
                                                                  "http://localhost/" + catenaXId + "/submodel", RelationshipAspect.SINGLE_LEVEL_USAGE_AS_BUILT)
                                                          .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyRelationshipsWhenRequestingWithNotExistingCatenaXIdAndAssemblyPartRelationship()
            throws Exception {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        prepareTestdata(catenaXId, "_assemblyPartRelationship");

        final List<Relationship> submodelResponse = testee.getRelationships(
                                                                  "http://localhost/" + catenaXId + "/submodel", RelationshipAspect.ASSEMBLY_PART_RELATIONSHIP)
                                                          .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).isEmpty();
    }

    @Test
    void shouldReturnRawSerialPartTypizationWhenExisting() throws Exception {
        final String existingCatenaXId = "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b";
        prepareTestdata(existingCatenaXId, "_serialPartTypization");

        final String submodelResponse = testee.getSubmodelRawPayload(
                "http://localhost/" + existingCatenaXId + "/submodel").get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).startsWith(
                "{\"localIdentifiers\":[{\"value\":\"BPNL00000003AYRE\",\"key\":\"manufacturerId\"}");
    }

    @Test
    void shouldUseDecodedTargetId() throws Exception {
        final String existingCatenaXId = "urn:uuid:4132cd2b-cbe7-4881-a6b4-39fdc31cca2b";
        prepareTestdata(existingCatenaXId, "_serialPartTypization");
        final String target = URLEncoder.encode(existingCatenaXId, StandardCharsets.UTF_8);

        final String submodelResponse = testee.getSubmodelRawPayload("http://localhost/" + target + "/submodel")
                                              .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).startsWith(
                "{\"localIdentifiers\":[{\"value\":\"BPNL00000003AYRE\",\"key\":\"manufacturerId\"}");
    }

    @Test
    void shouldReturnSameRelationshipsForDifferentDirections() throws Exception {
        final String parentCatenaXId = "urn:uuid:bd661150-8491-4442-943b-3e6e96fb0049";
        final BomLifecycle asBuilt = BomLifecycle.AS_BUILT;

        prepareTestdata(parentCatenaXId, "_assemblyPartRelationship");
        final List<Relationship> assemblyPartRelationships = testee.getRelationships(
                "http://localhost/" + parentCatenaXId + "/submodel",
                RelationshipAspect.from(asBuilt, Direction.DOWNWARD)).get(5, TimeUnit.SECONDS);

        final GlobalAssetIdentification childCatenaXId = assemblyPartRelationships.stream()
                                                                                  .findAny()
                                                                                  .map(Relationship::getLinkedItem)
                                                                                  .map(LinkedItem::getChildCatenaXId)
                                                                                  .orElseThrow();

        prepareTestdata(childCatenaXId.getGlobalAssetId(), "_singleLevelUsageAsBuilt");
        final List<Relationship> singleLevelUsageRelationships = testee.getRelationships(
                "http://localhost/" + childCatenaXId.getGlobalAssetId() + "/submodel",
                RelationshipAspect.from(asBuilt, Direction.UPWARD)).get(5, TimeUnit.SECONDS);

        assertThat(assemblyPartRelationships).isNotEmpty();
        assertThat(singleLevelUsageRelationships).isNotEmpty();
        assertThat(assemblyPartRelationships.get(0).getCatenaXId()).isEqualTo(
                singleLevelUsageRelationships.get(0).getCatenaXId());
        assertThat(assemblyPartRelationships.get(0).getLinkedItem().getChildCatenaXId()).isEqualTo(
                singleLevelUsageRelationships.get(0).getLinkedItem().getChildCatenaXId());
    }

    private void prepareTestdata(final String catenaXId, final String submodelDataSuffix)
            throws ContractNegotiationException, IOException, UsagePolicyException {
        when(contractNegotiationService.negotiate(any(), any())).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        final EndpointDataReference ref = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put("agreementId", ref);
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator(
                localTestDataConfiguration.cxTestDataContainer());
        final String data = StringMapper.mapToString(
                submodelTestdataCreator.createSubmodelForId(catenaXId + submodelDataSuffix));
        when(edcDataPlaneClient.getData(eq(ref), any())).thenReturn(data);
    }
}

class TimeMachine extends Clock {

    private Instant currentTime = Instant.now();

    @Override
    public ZoneId getZone() {
        return ZoneId.systemDefault();
    }

    @Override
    public Clock withZone(final ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return currentTime;
    }

    public void travelToFuture(Duration timeToAdd) {
        currentTime = currentTime.plus(timeToAdd);
    }
}