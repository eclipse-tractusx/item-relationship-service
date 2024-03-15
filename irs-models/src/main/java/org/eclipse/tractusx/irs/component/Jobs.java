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
package org.eclipse.tractusx.irs.component;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * List of Job and relationship to parts
 */
@Schema(description = "Container for a job with item graph.", example = Jobs.EXAMPLE)
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
@SuppressWarnings("PMD.ShortClassName")
public class Jobs {

    @Schema(description = "Executable unit with meta information and item graph result.", implementation = Job.class)
    private Job job;

    @ArraySchema(arraySchema = @Schema(description = "Relationships between parent and child items."), maxItems = Integer.MAX_VALUE)
    @Singular
    private List<Relationship> relationships;

    @ArraySchema(arraySchema = @Schema(description = "AAS shells."), maxItems = Integer.MAX_VALUE)
    private List<Shell> shells;

    @ArraySchema(arraySchema = @Schema(description = "Collection of not resolvable endpoints as tombstones. Including cause of error and endpoint URL."), maxItems = Integer.MAX_VALUE)
    @Singular
    private List<Tombstone> tombstones;

    @ArraySchema(arraySchema = @Schema(description = "Collection of requested Submodels"), maxItems = Integer.MAX_VALUE)
    @Singular
    private List<Submodel> submodels;

    @ArraySchema(arraySchema = @Schema(description = "Collection of bpn mappings"), maxItems = Integer.MAX_VALUE)
    @Singular
    private Set<Bpn> bpns;

    /* package */ static final String EXAMPLE = "{\"bpns\"=[{\"manufacturerId\"=\"BPNL00000003AAXX\", \"manufacturerName\"=\"AB CD\"}], \"job\"={\"completedOn\"=\"2022-02-03T14:48:54.709Z\", \"createdOn\"=\"2022-02-03T14:48:54.709Z\", \"exception\"={\"errorDetail\"=\"Timeout while requesting Digital Registry\", \"exception\"=\"IrsTimeoutException\", \"exceptionDate\"=\"2022-02-03T14:48:54.709Z\"}, \"globalAssetId\"=\"urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0\", \"id\"=\"e5347c88-a921-11ec-b909-0242ac120002\", \"lastModifiedOn\"=\"2022-02-03T14:48:54.709Z\", \"parameter\"={\"aspects\"=[\"SerialPart\", \"AddressAspect\"], \"auditContractNegotiation\"=false, \"bomLifecycle\"=\"asBuilt\", \"collectAspects\"=false, \"depth\"=1, \"direction\"=\"downward\", \"lookupBPNs\"=false}, \"startedOn\"=\"2022-02-03T14:48:54.709Z\", \"state\"=\"COMPLETED\", \"summary\"={\"asyncFetchedItems\"={\"completed\"=3, \"failed\"=0, \"running\"=0}, \"bpnLookups\"={\"completed\"=3, \"failed\"=0}}}, \"relationships\"=[{\"catenaXId\"=\"urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447\", \"linkedItem\"={\"assembledOn\"=\"2022-02-03T14:48:54.709Z\", \"childCatenaXId\"=\"urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9\", \"hasAlternatives\"=false, \"lastModifiedOn\"=\"2022-02-03T14:48:54.709Z\", \"lifecycleContext\"=\"asBuilt\", \"quantity\"={\"measurementUnit\"={\"datatypeURI\"=\"urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece\", \"lexicalValue\"=\"piece\"}, \"quantityNumber\"=1}}}], \"shells\"=[{\"contractAgreementId\"=\"f253718e-a270-4367-901b-9d50d9bd8462\", \"payload\"={\"description\"=[{\"language\"=\"en\", \"text\"=\"The shell for a vehicle\"}], \"globalAssetId\"=\"urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9\", \"id\"=\"urn:uuid:882fc530-b69b-4707-95f6-5dbc5e9baaa8\", \"idShort\"=\"future concept x\", \"specificAssetIds\"=[{\"name\"=\"engineserialid\", \"value\"=\"12309481209312\"}], \"submodelDescriptors\"=[{\"description\"=[{\"language\"=\"en\", \"text\"=\"Provides base vehicle information\"}], \"endpoints\"=[{\"interface\"=\"HTTP\", \"protocolInformation\"={\"endpointProtocol\"=\"HTTPS\", \"endpointProtocolVersion\"=[\"1.0\"], \"href\"=\"https://catena-x.net/vehicle/basedetails/\", \"subprotocol\"=\"DSP\", \"subprotocolBody\"=\"id=urn:uuid:c8159379-4613-48b8-ad52-6baed7afe923;dspEndpoint=https://irs-provider-controlplane3.dev.demo.catena-x.net\", \"subprotocolBodyEncoding\"=\"plain\"}}], \"id\"=\"urn:uuid:5d25a897-6571-4800-b98c-a3352fbf996d\", \"idShort\"=\"SingleLevelBomAsPlanned\", \"semanticId\"={\"keys\"=[{\"type\"=\"ExternalReference\", \"value\"=\"urn:bamm:io.catenax.single_level_bom_as_planned:2.0.0#SingleLevelBomAsPlanned\"}], \"type\"=\"ModelReference\"}},\n"
            + "    {\"description\"=[{\"language\"=\"en\", \"text\"=\"Provides base vehicle information\"}], \"endpoints\"=[{\"interface\"=\"HTTP\", \"protocolInformation\"={\"endpointProtocol\"=\"HTTPS\", \"endpointProtocolVersion\"=[\"1.0\"], \"href\"=\"https://catena-x.net/vehicle/partdetails/\", \"subprotocol\"=\"DSP\", \"subprotocolBody\"=\"id=urn:uuid:c8159379-4613-48b8-ad52-6baed7afe923;dspEndpoint=https://irs-provider-controlplane3.dev.demo.catena-x.net\", \"subprotocolBodyEncoding\"=\"plain\"}}], \"id\"=\"urn:uuid:dae4d249-6d66-4818-b576-bf52f3b9ae90\", \"idShort\"=\"vehicle part details\", \"semanticId\"={\"keys\"=[{\"type\"=\"Submodel\", \"value\"=\"urn:bamm:com.catenax.vehicle:0.1.1#PartDetails\"}], \"type\"=\"ModelReference\"}}]}}], \"submodels\"=[{\"aspectType\"=\"supply_chain_impacted\", \"contractAgreementId\"=\"f253718e-a270-4367-901b-9d50d9bd8462\", \"identification\"=\"urn:uuid:fc784d2a-5506-4e61-8e34-21600f8cdeff\", \"payload\"={\"supplyChainImpacted\"=\"YES\"}}], \"tombstones\"=[{\"catenaXId\"=\"urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0\", \"endpointURL\"=\"https://catena-x.net/vehicle/partdetails/\", \"processingError\"={\"errorDetail\"=\"Details to reason of failure\", \"lastAttempt\"=\"2022-02-03T14:48:54.709Z\", \"processStep\"=\"SchemaValidation\", \"retryCounter\"=0}}]}";

}
