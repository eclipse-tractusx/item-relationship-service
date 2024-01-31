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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus.TokenStatus;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceCacheService;
import org.eclipse.tractusx.irs.edc.client.cache.endpointdatareference.EndpointDataReferenceStatus;
import org.eclipse.tractusx.irs.edc.client.exceptions.ContractNegotiationException;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.TransferProcessException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.CatalogItem;
import org.eclipse.tractusx.irs.edc.client.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotificationResponse;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
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

    private static final String ENDPOINT_ADDRESS = "http://localhost/d46b51ae-08b6-42d7-a30d-0f8d118c8e0d-ce85f148-e3cf-42fe-9381-d1f276333fc4/submodel";
    private static final String ASSET_ID = "d46b51ae-08b6-42d7-a30d-0f8d118c8e0d-ce85f148-e3cf-42fe-9381-d1f276333fc4";
    private static final String PROVIDER_SUFFIX = "/test";

    private final static String CONNECTOR_ENDPOINT = "https://connector.endpoint.com";
    private final static String SUBMODEL_SUFIX = "/shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel";

    private final EndpointDataReferenceStorage endpointDataReferenceStorage = new EndpointDataReferenceStorage(
            Duration.ofMinutes(1));
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final TimeMachine clock = new TimeMachine();
    private final AsyncPollingService pollingService = new AsyncPollingService(clock, scheduler);
    @Spy
    private final EdcConfiguration config = new EdcConfiguration();
    private final RetryRegistry retryRegistry = RetryRegistry.ofDefaults();
    @Mock
    private ContractNegotiationService contractNegotiationService;
    @Mock
    private EdcDataPlaneClient edcDataPlaneClient;
    @Mock
    private EDCCatalogFacade catalogFacade;
    @Mock
    private EndpointDataReferenceCacheService endpointDataReferenceCacheService;
    private EdcSubmodelClient testee;

    EdcSubmodelClientTest() throws IOException {
        super();
    }

    @BeforeEach
    void setUp() {
        config.setControlplane(new EdcConfiguration.ControlplaneConfig());
        config.getControlplane().setEndpoint(new EdcConfiguration.ControlplaneConfig.EndpointConfig());
        config.getControlplane().getEndpoint().setData("https://irs-consumer-controlplane.dev.demo.catena-x.net/data");
        config.getControlplane().setRequestTtl(Duration.ofMinutes(10));
        config.getControlplane().setProviderSuffix(PROVIDER_SUFFIX);

        config.setSubmodel(new EdcConfiguration.SubmodelConfig());
        config.getSubmodel().setUrnPrefix("/urn");
        config.getSubmodel().setRequestTtl(Duration.ofMinutes(10));
        testee = new EdcSubmodelClientImpl(config, contractNegotiationService, edcDataPlaneClient,
                endpointDataReferenceStorage, pollingService, retryRegistry, catalogFacade,
                endpointDataReferenceCacheService);
    }

    @Test
    void shouldRetrieveValidRelationship() throws Exception {
        // arrange
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId("itemId").build()));
        when(contractNegotiationService.negotiate(any(), any(),
                eq(new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW)))).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        final EndpointDataReference ref = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put("agreementId", ref);
        final String singleLevelBomAsBuiltJson = readSingleLevelBomAsBuiltData();
        when(edcDataPlaneClient.getData(eq(ref), any())).thenReturn(singleLevelBomAsBuiltJson);
        when(endpointDataReferenceCacheService.getEndpointDataReference("assetId")).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        // act
        final var result = testee.getSubmodelRawPayload(ENDPOINT_ADDRESS, "suffix", "assetId");
        final String resultingRelationships = result.get(5, TimeUnit.SECONDS);

        // assert
        assertThat(resultingRelationships).isNotNull().isEqualTo(singleLevelBomAsBuiltJson);
    }

    @Test
    void shouldSendNotificationSuccessfully() throws Exception {
        // arrange
        final EdcNotification<NotificationContent> notification = EdcNotification.builder().build();
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId("itemId").build()));
        when(contractNegotiationService.negotiate(any(), any(), any())).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        final EndpointDataReference ref = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put("agreementId", ref);
        when(edcDataPlaneClient.sendData(ref, notification)).thenReturn(() -> true);
        when(endpointDataReferenceCacheService.getEndpointDataReference(any())).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        // act
        final var result = testee.sendNotification(CONNECTOR_ENDPOINT, "notify-request-asset", notification);
        final EdcNotificationResponse response = result.get(5, TimeUnit.SECONDS);

        // assert
        assertThat(response.deliveredSuccessfully()).isTrue();
    }

    @NotNull
    private String readSingleLevelBomAsBuiltData() throws IOException {
        final URL resourceAsStream = getClass().getResource("/__files/singleLevelBomAsBuilt.json");
        Objects.requireNonNull(resourceAsStream);
        try {
            return Files.readString(Paths.get(resourceAsStream.toURI()), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelBomAsBuilt() throws Exception {
        final String existingCatenaXId = "urn:uuid:b00df8b5-7826-4b87-b0f6-7c1e4cc7b444";
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId(existingCatenaXId).build()));
        prepareTestdata(existingCatenaXId, "_singleLevelBomAsBuilt");
        when(endpointDataReferenceCacheService.getEndpointDataReference(any())).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        final String submodelResponse = testee.getSubmodelRawPayload("http://localhost/", "/submodel", ASSET_ID)
                                              .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).contains(existingCatenaXId);
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelBomAsPlanned() throws Exception {
        final String catenaXId = "urn:uuid:aad27ddb-43aa-4e42-98c2-01e529ef127c";
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId(catenaXId).build()));
        prepareTestdata(catenaXId, "_singleLevelBomAsPlanned");
        when(endpointDataReferenceCacheService.getEndpointDataReference(any())).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        final String submodelResponse = testee.getSubmodelRawPayload("http://localhost/", "/submodel", ASSET_ID)
                                              .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).contains("urn:uuid:e5c96ab5-896a-482c-8761-efd74777ca97");
    }

    @Test
    void shouldReturnRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelBomAsSpecified() throws Exception {
        final String catenaXId = "urn:uuid:644c0988-6949-4586-b304-7f46f412a5cc";
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId(catenaXId).build()));
        prepareTestdata(catenaXId, "_singleLevelBomAsSpecified");
        when(endpointDataReferenceCacheService.getEndpointDataReference(any())).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        final String submodelResponse = testee.getSubmodelRawPayload("http://localhost/", "/submodel", ASSET_ID)
                                              .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).contains("urn:uuid:2afbac90-a662-4f16-9058-4f030e692631");
    }

    @Test
    void shouldReturnEmptyRelationshipsWhenRequestingWithCatenaXIdAndSingleLevelUsageAsBuilt() throws Exception {
        final String catenaXId = "urn:uuid:61c83b41-def0-4742-a1a8-e4e8a8cb210e";
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId(catenaXId).build()));
        prepareTestdata(catenaXId, "_singleLevelUsageAsBuilt");
        when(endpointDataReferenceCacheService.getEndpointDataReference(any())).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        final String submodelResponse = testee.getSubmodelRawPayload("http://localhost/", "/submodel", ASSET_ID)
                                              .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyRelationshipsWhenRequestingWithNotExistingCatenaXIdAndSingleLevelBomAsBuilt()
            throws Exception {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId(catenaXId).build()));
        prepareTestdata(catenaXId, "_singleLevelBomAsBuilt");
        when(endpointDataReferenceCacheService.getEndpointDataReference(ASSET_ID)).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        final String submodelResponse = testee.getSubmodelRawPayload("http://localhost/", "/submodel", ASSET_ID)
                                              .get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).isEqualTo("{}");
    }

    @Test
    void shouldReturnRawSerialPartWhenExisting() throws Exception {
        final String existingCatenaXId = "urn:uuid:b00df8b5-7826-4b87-b0f6-7c1e4cc7b444";
        when(catalogFacade.fetchCatalogByFilter("https://connector.endpoint.com" + PROVIDER_SUFFIX,
                "https://w3id.org/edc/v0.0.1/ns/id", ASSET_ID)).thenReturn(createCatalog(ASSET_ID, 3));
        prepareTestdata(existingCatenaXId, "_serialPart");
        when(endpointDataReferenceCacheService.getEndpointDataReference(ASSET_ID)).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        final String submodelResponse = testee.getSubmodelRawPayload("https://connector.endpoint.com",
                "/shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel", ASSET_ID).get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).startsWith(
                "{\"localIdentifiers\":[{\"value\":\"BPNL00000003AVTH\",\"key\":\"manufacturerId\"}");
    }

    @Test
    void shouldUseDecodedTargetId() throws Exception {
        final String existingCatenaXId = "urn:uuid:b00df8b5-7826-4b87-b0f6-7c1e4cc7b444";
        prepareTestdata(existingCatenaXId, "_serialPart");
        final String target = URLEncoder.encode(ASSET_ID, StandardCharsets.UTF_8);
        when(catalogFacade.fetchCatalogByFilter("https://connector.endpoint.com" + PROVIDER_SUFFIX,
                "https://w3id.org/edc/v0.0.1/ns/id", ASSET_ID)).thenReturn(createCatalog(target, 3));
        when(endpointDataReferenceCacheService.getEndpointDataReference(ASSET_ID)).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        final String submodelResponse = testee.getSubmodelRawPayload("https://connector.endpoint.com",
                "/shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel", ASSET_ID).get(5, TimeUnit.SECONDS);

        assertThat(submodelResponse).startsWith(
                "{\"localIdentifiers\":[{\"value\":\"BPNL00000003AVTH\",\"key\":\"manufacturerId\"}");
    }

    @Test
    void shouldReturnSameRelationshipsForDifferentDirections() throws Exception {
        final String parentCatenaXId = "urn:uuid:a65c35a8-8d31-4a86-899b-57912de33675";
        final BomLifecycle asBuilt = BomLifecycle.AS_BUILT;
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId(parentCatenaXId).build()));
        prepareTestdata(parentCatenaXId, "_singleLevelBomAsBuilt");
        when(endpointDataReferenceCacheService.getEndpointDataReference(ASSET_ID)).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));
        final String relationshipsJson = testee.getSubmodelRawPayload("http://localhost/", "_singleLevelBomAsBuilt",
                ASSET_ID).get(5, TimeUnit.SECONDS);

        final var relationships = StringMapper.mapFromString(relationshipsJson,
                RelationshipAspect.from(asBuilt, Direction.DOWNWARD).getSubmodelClazz()).asRelationships();

        final GlobalAssetIdentification childCatenaXId = relationships.stream()
                                                                      .findAny()
                                                                      .map(Relationship::getLinkedItem)
                                                                      .map(LinkedItem::getChildCatenaXId)
                                                                      .orElseThrow();

        prepareTestdata(childCatenaXId.getGlobalAssetId(), "_singleLevelUsageAsBuilt");
        final String singleLevelUsageRelationshipsJson = testee.getSubmodelRawPayload("http://localhost/",
                "_singleLevelUsageAsBuilt", ASSET_ID).get(5, TimeUnit.SECONDS);
        final var singleLevelUsageRelationships = StringMapper.mapFromString(singleLevelUsageRelationshipsJson,
                RelationshipAspect.from(asBuilt, Direction.UPWARD).getSubmodelClazz()).asRelationships();

        assertThat(relationships).isNotNull();
        assertThat(singleLevelUsageRelationships).isNotEmpty();
        assertThat(relationships.get(0).getCatenaXId()).isEqualTo(singleLevelUsageRelationships.get(0).getCatenaXId());
        assertThat(relationships.get(0).getLinkedItem().getChildCatenaXId()).isEqualTo(
                singleLevelUsageRelationships.get(0).getLinkedItem().getChildCatenaXId());
    }

    @Test
    void shouldRetrieveEndpointReferenceForAsset() throws Exception {
        // arrange
        final String filterKey = "filter-key";
        final String filterValue = "filter-value";
        final String agreementId = "agreementId";
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId("asset-id").build()));
        when(contractNegotiationService.negotiate(any(), any(),
                eq(new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW)))).thenReturn(
                NegotiationResponse.builder().contractAgreementId(agreementId).build());
        final EndpointDataReference expected = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put(agreementId, expected);

        // act
        final var result = testee.getEndpointReferenceForAsset(ENDPOINT_ADDRESS, filterKey, filterValue,
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));
        final EndpointDataReference actual = result.get(5, TimeUnit.SECONDS);

        // assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldRetrieveEndpointReferenceForAsset2() throws Exception {
        // arrange
        final String filterKey = "filter-key";
        final String filterValue = "filter-value";
        final String agreementId = "agreementId";
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId("asset-id").build()));
        when(contractNegotiationService.negotiate(any(), any(),
                eq(new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW)))).thenReturn(
                NegotiationResponse.builder().contractAgreementId(agreementId).build());
        final EndpointDataReference expected = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put(agreementId, expected);

        // act
        final var result = testee.getEndpointReferenceForAsset(ENDPOINT_ADDRESS, filterKey, filterValue);
        final EndpointDataReference actual = result.get(5, TimeUnit.SECONDS);

        // assert
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldUseCachedEndpointReferenceValueWhenTokenIsValid()
            throws EdcClientException, ExecutionException, InterruptedException {
        // given
        when(endpointDataReferenceCacheService.getEndpointDataReference(any())).thenReturn(
                new EndpointDataReferenceStatus(
                        EndpointDataReference.Builder.newInstance().endpoint("").authKey("").authCode("").build(),
                        TokenStatus.VALID));
        final String value = "result";
        when(edcDataPlaneClient.getData(any(), any())).thenReturn(value);

        // when
        final var resultFuture = testee.getSubmodelRawPayload(ENDPOINT_ADDRESS, "suffix", "assetId");

        // then
        final String result = resultFuture.get();
        verify(contractNegotiationService, never()).negotiate(any(), any(), any());
        assertThat(result).isEqualTo(value);
    }

    @Test
    void shouldCreateCacheRecordWhenTokenIsNotValid() throws EdcClientException {
        // given
        when(catalogFacade.fetchCatalogByFilter(any(), any(), any())).thenReturn(
                List.of(CatalogItem.builder().itemId("itemId").build()));
        when(contractNegotiationService.negotiate(any(), any(),
                eq(new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW)))).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        final EndpointDataReference ref = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put("agreementId", ref);
        when(endpointDataReferenceCacheService.getEndpointDataReference(any())).thenReturn(
                new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW));

        // when
        testee.getSubmodelRawPayload(ENDPOINT_ADDRESS, "suffix", "assetId");

        // then
        final Optional<EndpointDataReference> referenceFromStorage = endpointDataReferenceStorage.get("assetId");
        assertThat(referenceFromStorage).isPresent();
    }

    private void prepareTestdata(final String catenaXId, final String submodelDataSuffix)
            throws ContractNegotiationException, IOException, UsagePolicyException, TransferProcessException {

        when(contractNegotiationService.negotiate(any(), any(),
                eq(new EndpointDataReferenceStatus(null, TokenStatus.REQUIRED_NEW)))).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());
        final EndpointDataReference ref = mock(EndpointDataReference.class);
        endpointDataReferenceStorage.put("agreementId", ref);
        final SubmodelTestdataCreator submodelTestdataCreator = new SubmodelTestdataCreator(
                localTestDataConfiguration.cxTestDataContainer());
        final String data = StringMapper.mapToString(
                submodelTestdataCreator.createSubmodelForId(catenaXId + submodelDataSuffix));
        when(edcDataPlaneClient.getData(eq(ref), any())).thenReturn(data);
    }

    private List<CatalogItem> createCatalog(final String assetId, final int numberOfOffers) {
        final Policy policy = mock(Policy.class);

        return IntStream.range(0, numberOfOffers)
                        .boxed()
                        .map(i -> CatalogItem.builder()
                                             .offerId("offer" + i)
                                             .assetPropId(assetId)
                                             .policy(policy)
                                             .build())
                        .toList();
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