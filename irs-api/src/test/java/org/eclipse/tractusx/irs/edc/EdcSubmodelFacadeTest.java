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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

import com.github.jknack.handlebars.internal.Files;
import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.RelationshipAspect;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.configuration.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.exceptions.TimeoutException;
import org.eclipse.tractusx.irs.services.AsyncPollingService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdcSubmodelFacadeTest {

    private static final String ENDPOINT_ADDRESS = "dummyAddress/urn:123456/submodel";

    @Mock
    private ContractNegotiationService contractNegotiationService;
    @Mock
    private EdcDataPlaneClient edcDataPlaneClient;
    private final EndpointDataReferenceStorage endpointDataReferenceStorage = new EndpointDataReferenceStorage();
    private final JsonUtil jsonUtil = new JsonUtil();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final TimeMachine clock = new TimeMachine();

    private final AsyncPollingService pollingService = new AsyncPollingService(clock, scheduler);

    @Spy
    private final EdcConfiguration config = new EdcConfiguration();

    private EdcSubmodelFacade testee;

    @BeforeEach
    void setUp() {
        config.setControlPlaneEndpointData("https://irs-consumer-controlplane.dev.demo.catena-x.net/data");
        config.setSubmodelPath("/submodel");
        config.setSubmodelUrnPrefix("/urn");
        config.setSubmodelRequestTtl(Duration.ofMinutes(10));
        config.setControlPlaneRequestTtl(Duration.ofMinutes(10));
        testee = new EdcSubmodelFacade(config, contractNegotiationService, edcDataPlaneClient,
                endpointDataReferenceStorage, jsonUtil, pollingService);
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
        final var result = testee.getRelationships(ENDPOINT_ADDRESS, RelationshipAspect.AssemblyPartRelationship);
        final List<Relationship> resultingRelationships = result.get(5, TimeUnit.SECONDS);

        // assert
        final List<Relationship> expectedRelationships = jsonUtil.fromString(assemblyPartRelationshipJson,
                RelationshipAspect.AssemblyPartRelationship.getSubmodelClazz()).asRelationships();
        assertThat(resultingRelationships).isNotNull().containsAll(expectedRelationships);
    }

    @Test
    void shouldTimeOut() throws Exception {
        // arrange
        when(contractNegotiationService.negotiate(any(), any())).thenReturn(
                NegotiationResponse.builder().contractAgreementId("agreementId").build());

        // act
        final var result = testee.getRelationships(ENDPOINT_ADDRESS, RelationshipAspect.AssemblyPartRelationship);
        clock.travelToFuture(Duration.ofMinutes(20));

        // assert
        assertThatThrownBy(result::get).isInstanceOf(ExecutionException.class)
                                       .hasCauseInstanceOf(TimeoutException.class);
    }

    @NotNull
    private String readAssemblyPartRelationshipData() throws IOException {
        final InputStream resourceAsStream = getClass().getResourceAsStream("/__files/assemblyPartRelationship.json");
        Objects.requireNonNull(resourceAsStream);
        return Files.read(resourceAsStream, StandardCharsets.UTF_8);
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