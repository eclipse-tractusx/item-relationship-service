package net.catenax.irs.aaswrapper.registry.domain;

import java.util.List;

import net.catenax.irs.aaswrapper.registry.domain.model.Endpoint;
import net.catenax.irs.aaswrapper.registry.domain.model.IdentifierKeyValuePair;
import net.catenax.irs.aaswrapper.registry.domain.model.LangString;
import net.catenax.irs.aaswrapper.registry.domain.model.ProtocolInformation;
import net.catenax.irs.aaswrapper.registry.domain.model.Reference;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelDescriptor;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import org.springframework.stereotype.Service;

/**
 * Digital Twin Registry Rest Client Stub used in local environment
 */
//@Profile("local")
@Service
@ExcludeFromCodeCoverageGeneratedReport
public class DigitalTwinRegistryClientLocalStub implements DigitalTwinRegistryClient {

    @Override
    public AssetAdministrationShellDescriptor getAssetAdministrationShellDescriptor(final String aasIdentifier) {
        final SubmodelDescriptor descriptor1 = new SubmodelDescriptor("assembly-part-relationship",
                "assemblyPartRelationship",
                new Reference(List.of("urn:bamm:com.catenax.assembly_part_relationtship:1.0.0")),
                List.of(new Endpoint("https://OEM.connector", new ProtocolInformation(
                        "edc://offer-trace-assembly-part-relationship/shells/d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447/aas/assembly-part-relationship",
                        "AAS/SUBMODEL", "1.0RC02"))));
        final SubmodelDescriptor descriptor2 = new SubmodelDescriptor("serial-part-typization", "serialPartTypization",
                new Reference(List.of("urn:bamm:com.catenax.serial_part_typization:1.0.0")),
                List.of(new Endpoint("https://OEM.connector", new ProtocolInformation(
                        "edc://offer-trace-serial-part-typization/shells/d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447/aas/serial-part-typization",
                        "AAS/SUBMODEL", "1.0RC02"))));

        return new AssetAdministrationShellDescriptor.AssetAdministrationShellDescriptorBuilder().description(
                                                                                                         List.of(new LangString("en", "The shell for a brake system")))
                                                                                                 .globalAssetId(
                                                                                                         new Reference(
                                                                                                                 List.of("urn:twin:com.tsystems#d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447")))
                                                                                                 .idShort(
                                                                                                         "brake_dt_2019_snr.asm")
                                                                                                 .identification(
                                                                                                         aasIdentifier)
                                                                                                 .specificAssetIds(
                                                                                                         List.of(new IdentifierKeyValuePair(
                                                                                                                         "http://pwc.t-systems.com/datamodel/common",
                                                                                                                         "0000000251"),
                                                                                                                 new IdentifierKeyValuePair(
                                                                                                                         "urn:VR:wt.part.WTPart#",
                                                                                                                         "25054146@nis11c130.epdm-d.edm.dsh.de")))
                                                                                                 .submodelDescriptors(
                                                                                                         List.of(descriptor1,
                                                                                                                 descriptor2))
                                                                                                 .build();
    }
}
