package org.eclipse.tractusx.esr.irs;

import java.util.List;

import org.eclipse.tractusx.esr.irs.model.relationship.LinkedItem;
import org.eclipse.tractusx.esr.irs.model.relationship.Relationship;
import org.eclipse.tractusx.esr.irs.model.shell.Endpoint;
import org.eclipse.tractusx.esr.irs.model.shell.IdentifierKeyValuePair;
import org.eclipse.tractusx.esr.irs.model.shell.ListOfValues;
import org.eclipse.tractusx.esr.irs.model.shell.ProtocolInformation;
import org.eclipse.tractusx.esr.irs.model.shell.Shell;
import org.eclipse.tractusx.esr.irs.model.shell.SubmodelDescriptor;

public class IrsFixture {

    private static final String DEFAULT_SEMANTIC_ID = "urn:bamm:com.catenax.serial_part_typization:1.0.0";

    public static Relationship exampleRelationship(final String globalAssetId, final String childId) {
        return Relationship.builder()
                           .catenaXId(globalAssetId)
                           .aspectType("aspectType")
                           .linkedItem(LinkedItem.builder()
                                                 .childCatenaXId(childId)
                                                 .lifecycleContext("asBuilt")
                                                 .build())
                           .build();
    }

    public static Relationship exampleRelationship() {
        return exampleRelationship("urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447", "urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9");
    }

    public static Shell exampleShellWithGlobalAssetId(final String globalAssetId) {
        return exampleShellWithGlobalAssetId(globalAssetId, DEFAULT_SEMANTIC_ID);
    }

    public static Shell exampleShellWithGlobalAssetId(final String globalAssetId, final String semanticId) {
        return Shell.builder()
                    .globalAssetId(ListOfValues.builder()
                                               .value(List.of(globalAssetId))
                                               .build())
                    .identification("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                    .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                    .key("manufacturerId")
                                                                    .subjectId(ListOfValues.builder().value(List.of("sub-id")).build())
                                                                    .value("BPNL00000003AYRE")
                                                                    .semanticId(ListOfValues.builder().value(List.of("urn:bamm:com.catenax.serial_part_typization:1.0.0")).build())
                                                                    .build()))
                    .submodelDescriptors(List.of(SubmodelDescriptor.builder()
                                                                   .identification("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                                                                   .semanticId(ListOfValues.builder().value(List.of(semanticId)).build())
                                                                   .endpoints(List.of(Endpoint.builder()
                                                                                              .protocolInformation(
                                                                                                      ProtocolInformation.builder()
                                                                                                                         .endpointAddress("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                                                                                                                         .endpointProtocol("AAS/SUBMODEL")
                                                                                                                         .endpointProtocolVersion("1.0RC02")
                                                                                                                         .subprotocol("sub protocol")
                                                                                                                         .subprotocolBody("sub protocol body")
                                                                                                                         .subprotocolBodyEncoding("UTF-8")
                                                                                                                         .build())
                                                                                              .interfaceInfo("https://TEST.connector")
                                                                                              .build()))
                                                                   .build()))
                    .build();
    }

    public static Shell exampleShell() {
        return exampleShellWithGlobalAssetId("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf");
    }
}
