package net.catenax.irs.aaswrapper.job;

import static net.catenax.irs.util.TestMother.jobParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.component.assetadministrationshell.AdministrativeInformation;
import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import net.catenax.irs.component.assetadministrationshell.LangString;
import net.catenax.irs.component.assetadministrationshell.ProtocolInformation;
import net.catenax.irs.component.assetadministrationshell.Reference;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.util.JsonUtil;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AASTransferProcessManagerTest {

    private final TestMother generate = new TestMother();

    DigitalTwinRegistryFacade digitalTwinRegistryFacade = mock(DigitalTwinRegistryFacade.class);

    SubmodelFacade submodelFacade = mock(SubmodelFacade.class);

    // ExecutorService pool = mock(ExecutorService.class);
    ExecutorService pool = Executors.newCachedThreadPool();

    AssetAdministrationShellDescriptor aasShell = mock(AssetAdministrationShellDescriptor.class);

    InMemoryBlobStore blobStore = new InMemoryBlobStore();
    final AASTransferProcessManager manager = new AASTransferProcessManager(digitalTwinRegistryFacade, submodelFacade,
            pool, blobStore);

    @Test
    void shouldExecuteThreadForProcessing() {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(UUID.randomUUID().toString());

        // when
        manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, jobParameter());

        // then
        verify(pool, times(1)).execute(any(Runnable.class));
    }

    @Test
    void shouldInitiateProcessingAndReturnOkStatus() {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(UUID.randomUUID().toString());

        // when
        final TransferInitiateResponse initiateResponse = manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, jobParameter());

        assertThat(initiateResponse.getStatus()).withFailMessage().isNotNull().
                // then
                        assertThat(initiateResponse.getTransferId()).isNotBlank();
        assertThat(initiateResponse.getStatus()).isEqualTo(ResponseStatus.OK);
    }

    @Test
    void shouldRetryExecutionOfGetSubmodelAndRecordNumbersOfTrials() {
        // given
        final String assemblyPartRelationshipIdWithAspectName = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship";
        final AssetAdministrationShellDescriptor shellDescriptor = createShellDescriptor(
                List.of(createSubmodelDescriptorWithoutEndpoint(assemblyPartRelationshipIdWithAspectName)));

        when(digitalTwinRegistryFacade.getAAShellDescriptor(any(), any())).thenReturn(fakeShellDescriptorBuilder());

        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(UUID.randomUUID().toString());

        given(submodelFacade.getSubmodel(anyString(), any())).willThrow(IllegalArgumentException.class);

        // when
        TransferInitiateResponse tranfer = manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, jobParameter());

        byte[] dataByte = blobStore.getStore().entrySet().stream().findFirst().get().getValue();
        JsonUtil jsonUtil = new JsonUtil();

        ItemContainer item = jsonUtil.fromString(new String(dataByte, StandardCharsets.UTF_8), ItemContainer.class);
        assertThat(item).isNotNull();

        Tombstone tombStone = item.getTombstones().get(0);
    }

    private AssetAdministrationShellDescriptor createShellDescriptor(
            final List<SubmodelDescriptor> submodelDescriptors) {
        return AssetAdministrationShellDescriptor.builder().submodelDescriptors(submodelDescriptors).build();
    }

    private SubmodelDescriptor createSubmodelDescriptor(final String semanticId, final String endpointAddress) {
        final Reference semanticIdSerial = Reference.builder().value(List.of(semanticId)).build();
        final List<Endpoint> endpointSerial = List.of(createEndpoint(endpointAddress));
        return SubmodelDescriptor.builder().semanticId(semanticIdSerial).endpoints(endpointSerial).build();
    }

    private SubmodelDescriptor createSubmodelDescriptorWithoutEndpoint(final String semanticId) {
        return createSubmodelDescriptor(semanticId, "http://localhost:8888/dummy/test");
    }

    private Endpoint createEndpoint(String endpointAddress) {
        return Endpoint.builder()
                       .protocolInformation(ProtocolInformation.builder().endpointAddress(endpointAddress).build())
                       .build();
    }

    private AssetAdministrationShellDescriptor fakeShellDescriptorBuilder() {
        return AssetAdministrationShellDescriptor.builder()
                                                 .administration(AdministrativeInformation.builder()
                                                                                          .revision("4")
                                                                                          .version("1.0")
                                                                                          .build())
                                                 .description(List.of(LangString.builder()
                                                                                .language("English")
                                                                                .text("Fake description")
                                                                                .build()))
                                                 .globalAssetId(Reference.builder()
                                                                         .value(List.of(
                                                                                 "urn:uuid:5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99"))
                                                                         .build())
                                                 .idShort("5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99")
                                                 .identification("8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .key("fakeKey")
                                                                                                 .subjectId(
                                                                                                         Reference.builder()
                                                                                                                  .value(List.of(
                                                                                                                          "urn:uuid:5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99"))
                                                                                                                  .build())
                                                                                                 .value("urn:uuid:5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99")
                                                                                                 .semanticId(
                                                                                                         Reference.builder()
                                                                                                                  .value(List.of(
                                                                                                                          "urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship#"))
                                                                                                                  .build())
                                                                                                 .build()))
                                                 .submodelDescriptors(List.of(fakeSubmodelDescriptor()))
                                                 .build();
    }

    private SubmodelDescriptor fakeSubmodelDescriptor() {
        return SubmodelDescriptor.builder()
                                 .administration(
                                         AdministrativeInformation.builder().revision("4").version("1.0").build())
                                 .description(List.of(LangString.builder()
                                                                .language("English")
                                                                .text("Fake description")
                                                                .build()))
                                 .idShort("5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99")
                                 .identification("8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                                 .semanticId(Reference.builder()
                                                      .value(List.of(
                                                              "urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship#"))
                                                      .build())
                                 .endpoints(List.of(Endpoint.builder()
                                                            .interfaceInformation(
                                                                    "http://localhost/dummy/interfaceinformation")
                                                            .protocolInformation(fakeProtocolInformation())
                                                            .build()))

                                 .build();
    }

    private ProtocolInformation fakeProtocolInformation() {
        return ProtocolInformation.builder()
                                  .endpointProtocol("Https")
                                  .endpointAddress(
                                          "http://localhost/dummy/interfaceinformation/urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6")
                                  .endpointProtocolVersion("v2")
                                  .build();
    }

}
