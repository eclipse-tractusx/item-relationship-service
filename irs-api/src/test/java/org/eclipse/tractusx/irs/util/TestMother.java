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
package org.eclipse.tractusx.irs.util;

import static org.eclipse.tractusx.irs.controllers.IrsAppConstants.UUID_SIZE;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.component.GlobalAssetIdentification;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.LinkedItem;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.connector.job.DataRequest;
import org.eclipse.tractusx.irs.connector.job.MultiTransferJob;
import org.eclipse.tractusx.irs.connector.job.ResponseStatus;
import org.eclipse.tractusx.irs.connector.job.TransferInitiateResponse;
import org.eclipse.tractusx.irs.connector.job.TransferProcess;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.eclipse.tractusx.irs.dto.RelationshipAspect;
import org.eclipse.tractusx.irs.services.MeterRegistryService;
import net.datafaker.Faker;

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
        return registerJobWithDepthAndAspect(null, List.of(AspectType.ASSEMBLY_PART_RELATIONSHIP));
    }

    public static RegisterJob registerJobWithDepthAndAspect(final Integer depth, final List<AspectType> aspectTypes) {
        return registerJobWithGlobalAssetIdAndDepth("urn:uuid:b2d7176c-c48b-42f4-b485-31a2b64a0873", depth, aspectTypes,
                false);
    }

    public static RegisterJob registerJobWithDepthAndAspectAndCollectAspects(final Integer depth,
            final List<AspectType> aspectTypes) {
        return registerJobWithGlobalAssetIdAndDepth("urn:uuid:b2d7176c-c48b-42f4-b485-31a2b64a0873", depth, aspectTypes,
                true);
    }

    public static RegisterJob registerJobWithGlobalAssetIdAndDepth(final String globalAssetId, final Integer depth,
            final List<AspectType> aspectTypes, final boolean collectAspects) {
        final RegisterJob registerJob = new RegisterJob();
        registerJob.setGlobalAssetId(globalAssetId);
        registerJob.setDepth(depth);
        registerJob.setAspects(aspectTypes);
        registerJob.setCollectAspects(collectAspects);

        return registerJob;
    }

    public static JobParameter jobParameter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:b2d7176c-c48b-42f4-b485-31a2b64a0873")
                           .treeDepth(0)
                           .bomLifecycle("AsBuilt")
                           .aspectTypes(List.of(AspectType.SERIAL_PART_TYPIZATION.toString(),
                                   AspectType.ASSEMBLY_PART_RELATIONSHIP.toString()))
                           .build();
    }

    public static JobParameter jobParameterCollectAspects() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:b2d7176c-c48b-42f4-b485-31a2b64a0873")
                           .treeDepth(0)
                           .bomLifecycle("AsBuilt")
                           .aspectTypes(List.of(AspectType.SERIAL_PART_TYPIZATION.toString(),
                                   AspectType.ASSEMBLY_PART_RELATIONSHIP.toString()))
                           .collectAspects(true)
                           .build();
    }

    public static JobParameter jobParameterFilter() {
        return JobParameter.builder()
                           .rootItemId("urn:uuid:b2d7176c-c48b-42f4-b485-31a2b64a0873")
                           .treeDepth(0)
                           .bomLifecycle("AsRequired")
                           .aspectTypes(List.of(AspectType.MATERIAL_FOR_RECYCLING.toString()))
                           .build();
    }

    public static MeterRegistryService simpleMeterRegistryService() {
        return new MeterRegistryService(new SimpleMeterRegistry());
    }

    public AASTransferProcess aasTransferProcess() {
        return new AASTransferProcess(faker.lorem().characters(UUID_SIZE), faker.number().numberBetween(1, 100));
    }

    public Job fakeJob(JobState state) {
        return Job.builder()
                  .jobId(UUID.randomUUID())
                  .globalAssetId(GlobalAssetIdentification.of(UUID.randomUUID().toString()))
                  .jobState(state)
                  .createdOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .owner(faker.lorem().characters())
                  .lastModifiedOn(ZonedDateTime.now(ZoneId.of("UTC")))
                  .build();
    }

    public MultiTransferJob job() {
        return job(faker.options().option(JobState.class));
    }

    public MultiTransferJob job(JobState jobState) {
        return MultiTransferJob.builder()
                               .job(fakeJob(jobState))
                               .jobParameter(jobParameter())
                               .jobParameter(jobParameter())
                               .build();
    }

    public DataRequest dataRequest() {
        return new DataRequest() {
        };
    }

    public TransferInitiateResponse okResponse() {
        return response(ResponseStatus.OK);
    }

    public TransferInitiateResponse response(ResponseStatus status) {
        return TransferInitiateResponse.builder().transferId(UUID.randomUUID().toString()).status(status).build();
    }

    public TransferProcess transfer() {
        final String characters = faker.lorem().characters();
        return () -> characters;
    }

    public Stream<DataRequest> dataRequests(int count) {
        return IntStream.range(0, count).mapToObj(i -> dataRequest());
    }

    public static Relationship relationship() {
        final LinkedItem linkedItem = LinkedItem.builder()
                                                .childCatenaXId(
                                                        GlobalAssetIdentification.of(UUID.randomUUID().toString()))
                                                .lifecycleContext(BomLifecycle.AS_BUILT)
                                                .build();

        return new Relationship(GlobalAssetIdentification.of(UUID.randomUUID().toString()), linkedItem,
                RelationshipAspect.AssemblyPartRelationship.name());
    }

    public static Endpoint endpoint(String endpointAddress) {
        return Endpoint.builder()
                       .protocolInformation(ProtocolInformation.builder().endpointAddress(endpointAddress).build())
                       .build();
    }

    public static SubmodelDescriptor submodelDescriptor(final String semanticId, final String endpointAddress) {
        final Reference semanticIdSerial = Reference.builder().value(List.of(semanticId)).build();
        final List<Endpoint> endpointSerial = List.of(endpoint(endpointAddress));
        return SubmodelDescriptor.builder().semanticId(semanticIdSerial).endpoints(endpointSerial).build();
    }

    public static SubmodelDescriptor submodelDescriptorWithoutEndpoint(final String semanticId) {
        return submodelDescriptor(semanticId, null);
    }

    public static AssetAdministrationShellDescriptor shellDescriptor(
            final List<SubmodelDescriptor> submodelDescriptors) {
        return AssetAdministrationShellDescriptor.builder()
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .key("ManufacturerId")
                                                                                                 .value("BPNL00000003AYRE")
                                                                                                 .build()))
                                                 .submodelDescriptors(submodelDescriptors)
                                                 .build();
    }
}