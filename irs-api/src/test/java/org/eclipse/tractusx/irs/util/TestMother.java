/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.util;

import static org.eclipse.tractusx.irs.controllers.IrsAppConstants.UUID_SIZE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.datafaker.Faker;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.RegisterBatchOrder;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationBatchOrder;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.DataRequest;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.connector.job.ResponseStatus;
import org.eclipse.tractusx.irs.connector.job.TransferInitiateResponse;
import org.eclipse.tractusx.irs.connector.job.TransferProcess;
import org.eclipse.tractusx.irs.edc.client.RelationshipAspect;
import org.eclipse.tractusx.irs.services.MeterRegistryService;

/**
 * Base object mother class to create objects for testing.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class TestMother {

    Faker faker = new Faker();

    public static RegisterJob registerJobWithoutDepthAndAspect() {
        return registerJobWithDepthAndAspect(null, null);
    }

    public static RegisterJob registerJobWithoutDepth() {
        return registerJobWithDepthAndAspect(null, List.of(AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString()));
    }

    public static RegisterJob registerJobWithDepthAndAspect(final Integer depth, final List<String> aspectTypes) {
        return registerJob("urn:uuid:8ddd8fe0-1b4f-44b4-90f3-a8f68e551ac7", depth, aspectTypes, false, false,
                Direction.DOWNWARD);
    }

    public static RegisterJob registerJobWithDirection(final String globalAssetId, final Direction direction) {
        return registerJob(globalAssetId, 100, List.of(), false, false, direction);
    }

    public static RegisterJob registerJobWithUrl(final String callbackUrl) {
        final RegisterJob registerJob = registerJob("urn:uuid:8ddd8fe0-1b4f-44b4-90f3-a8f68e551ac7", 100, List.of(),
                false, false, Direction.DOWNWARD);
        registerJob.setCallbackUrl(callbackUrl);
        return registerJob;
    }

    public static RegisterJob registerJobWithDepthAndAspectAndCollectAspects(final Integer depth,
            final List<String> aspectTypes) {
        return registerJob("urn:uuid:8ddd8fe0-1b4f-44b4-90f3-a8f68e551ac7", depth, aspectTypes, true, false,
                Direction.DOWNWARD);
    }

    public static RegisterJob registerJobWithLookupBPNs() {
        return registerJob("urn:uuid:8ddd8fe0-1b4f-44b4-90f3-a8f68e551ac7", null,
                List.of(AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString()), false, true, Direction.DOWNWARD);
    }

    public static RegisterJob registerJob(final String globalAssetId, final Integer depth,
            final List<String> aspectTypes, final boolean collectAspects, final boolean lookupBPNs,
            final Direction direction) {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setKey(
                PartChainIdentificationKey.builder().globalAssetId(globalAssetId).bpn("BPNL0000000001AA").build());
        registerJob.setDepth(depth);
        registerJob.setAspects(aspectTypes);
        registerJob.setCollectAspects(collectAspects);
        registerJob.setDirection(direction);
        registerJob.setLookupBPNs(lookupBPNs);
        registerJob.setIntegrityCheck(false);

        return registerJob;
    }

    public static RegisterBatchOrder registerBatchOrder(final String... globalAssetIds) {
        final RegisterBatchOrder registerBatchOrder = new RegisterBatchOrder();
        registerBatchOrder.setKeys(Arrays.stream(globalAssetIds)
                                         .map(globalAssetId -> PartChainIdentificationKey.builder()
                                                                             .globalAssetId(globalAssetId)
                                                                             .bpn("BPNL0000000001AA")
                                                                             .build())
                                         .collect(Collectors.toSet()));

        return registerBatchOrder;
    }

    public static RegisterBpnInvestigationBatchOrder registerBpnInvestigationBatchOrder(final String... globalAssetIds) {
        final RegisterBpnInvestigationBatchOrder registerBpnInvestigationBatchOrder = new RegisterBpnInvestigationBatchOrder();
        registerBpnInvestigationBatchOrder.setKeys(Arrays.stream(globalAssetIds)
                                         .map(globalAssetId -> PartChainIdentificationKey.builder()
                                                                             .globalAssetId(globalAssetId)
                                                                             .bpn("BPNL0000000001AA")
                                                                             .build())
                                         .collect(Collectors.toSet()));
        registerBpnInvestigationBatchOrder.setIncidentBPNSs(List.of("BPNL0000000002BB"));
        return registerBpnInvestigationBatchOrder;
    }

    public static JobParameter jobParameter() {
        return JobParameter.builder()
                           .depth(5)
                           .bomLifecycle(BomLifecycle.AS_BUILT)
                           .direction(Direction.DOWNWARD)
                           .aspects(List.of(AspectType.SERIAL_PART.toString(),
                                   AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString()))
                           .integrityCheck(true)
                           .build();
    }

    public static JobParameter jobParameterUpward() {
        return JobParameter.builder()
                           .depth(0)
                           .bomLifecycle(BomLifecycle.AS_BUILT)
                           .direction(Direction.UPWARD)
                           .aspects(List.of(AspectType.SERIAL_PART.toString(),
                                   AspectType.SINGLE_LEVEL_USAGE_AS_BUILT.toString()))
                           .build();
    }

    public static JobParameter jobParameterCollectAspects() {
        return JobParameter.builder()
                           .depth(0)
                           .bomLifecycle(BomLifecycle.AS_BUILT)
                           .aspects(List.of(AspectType.SERIAL_PART.toString(),
                                   AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString()))
                           .collectAspects(true)
                           .build();
    }

    public static JobParameter jobParameterFilter() {
        return JobParameter.builder()
                           .depth(0)
                           .bomLifecycle(BomLifecycle.AS_BUILT)
                           .aspects(List.of(AspectType.MATERIAL_FOR_RECYCLING.toString()))
                           .build();
    }

    public static JobParameter jobParameterCollectBpns() {
        return JobParameter.builder()
                           .depth(0)
                           .bomLifecycle(BomLifecycle.AS_BUILT)
                           .direction(Direction.DOWNWARD)
                           .aspects(List.of(AspectType.SERIAL_PART.toString(),
                                   AspectType.SINGLE_LEVEL_BOM_AS_BUILT.toString()))
                           .lookupBPNs(true)
                           .build();
    }

    public static MeterRegistryService simpleMeterRegistryService() {
        return new MeterRegistryService(new SimpleMeterRegistry());
    }

    public static Relationship relationship() {
        final LinkedItem linkedItem = LinkedItem.builder()
                                                .childCatenaXId(
                                                        GlobalAssetIdentification.of(UUID.randomUUID().toString()))
                                                .lifecycleContext(BomLifecycle.AS_BUILT)
                                                .build();

        return new Relationship(GlobalAssetIdentification.of(UUID.randomUUID().toString()), linkedItem,
                RelationshipAspect.SINGLE_LEVEL_BOM_AS_BUILT.name(), "BPN");
    }

    public static Endpoint endpoint(final String endpointAddress) {
        return Endpoint.builder()
                       .protocolInformation(ProtocolInformation.builder()
                                                               .href(endpointAddress)
                                                               .subprotocolBody(
                                                                       "other_id=fake-id;id=12345;dspEndpoint=http://edc.control.plane/")
                                                               .build())
                       .build();
    }

    public static Endpoint endpoint(final String endpointAddress, final String dspEndpoint) {
        return Endpoint.builder()
                       .protocolInformation(ProtocolInformation.builder()
                                                               .href(endpointAddress)
                                                               .subprotocolBody("other_id=fake-id;id=12345;"
                                                                       + dspEndpoint)
                                                               .build())
                       .build();
    }

    public static SubmodelDescriptor submodelDescriptorWithDspEndpoint(final String semanticId, final String endpointAddress) {
        final Reference semanticIdSerial = Reference.builder()
                                                    .keys(List.of(SemanticId.builder().value(semanticId).build()))
                                                    .build();
        final List<Endpoint> endpointSerial = List.of(endpoint(endpointAddress));
        return SubmodelDescriptor.builder().semanticId(semanticIdSerial).endpoints(endpointSerial).build();
    }

    public static SubmodelDescriptor submodelDescriptor(final String semanticId, final String endpointAddress, final String dspEndpoint) {
        final Reference semanticIdSerial = Reference.builder()
                                                    .keys(List.of(SemanticId.builder().value(semanticId).build()))
                                                    .build();
        final List<Endpoint> endpointSerial = List.of(endpoint(endpointAddress, dspEndpoint));
        return SubmodelDescriptor.builder().semanticId(semanticIdSerial).endpoints(endpointSerial).build();
    }

    public static SubmodelDescriptor submodelDescriptorWithoutHref(final String semanticId) {
        return submodelDescriptorWithDspEndpoint(semanticId, null);
    }

    public static AssetAdministrationShellDescriptor shellDescriptor(
            final List<SubmodelDescriptor> submodelDescriptors) {
        return AssetAdministrationShellDescriptor.builder()
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .name("ManufacturerId")
                                                                                                 .value("BPNL00000003AYRE")
                                                                                                 .build()))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }

    public AASTransferProcess aasTransferProcess() {
        return new AASTransferProcess(faker.lorem().characters(UUID_SIZE), faker.number().numberBetween(1, 100));
    }

    public Job fakeJob(final JobState state) {
        return Job.builder()
                  .id(UUID.randomUUID())
                  .globalAssetId(GlobalAssetIdentification.of(UUID.randomUUID().toString()))
                  .state(state)
                  .createdOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .startedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .lastModifiedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .parameter(jobParameter())
                  .completedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .build();
    }

    public MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    public MultiTransferJob job(final JobState jobState) {
        return MultiTransferJob.builder().job(fakeJob(jobState)).build();
    }

    public DataRequest dataRequest() {
        return new DataRequest() {
        };
    }

    public TransferInitiateResponse okResponse() {
        return response(ResponseStatus.OK);
    }

    public TransferInitiateResponse response(final ResponseStatus status) {
        return TransferInitiateResponse.builder().transferId(UUID.randomUUID().toString()).status(status).build();
    }

    public TransferProcess transfer() {
        final String characters = faker.lorem().characters();
        return () -> characters;
    }
}