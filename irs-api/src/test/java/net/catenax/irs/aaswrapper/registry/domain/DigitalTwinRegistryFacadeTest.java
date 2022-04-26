package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalTwinRegistryFacadeTest {

    private DigitalTwinRegistryFacade digitalTwinRegistryFacade;

    @Mock
    private DigitalTwinRegistryClient dtRegistryClientMock;

    private DigitalTwinRegistryFacade dtRegistryFacadeWithMock;

    @BeforeEach
    void setUp() {
        digitalTwinRegistryFacade = new DigitalTwinRegistryFacade(new DigitalTwinRegistryClientLocalStub());
        dtRegistryFacadeWithMock = new DigitalTwinRegistryFacade(dtRegistryClientMock);
    }

//    @Test
//    void shouldReturnSubmodelEndpointsWhenRequestingWithCatenaXId() {
//        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
//        final List<AbstractAAS> shellEndpoints = digitalTwinRegistryFacade.getAASSubmodelEndpoints(catenaXId);
//        assertThat(shellEndpoints).isNotNull().hasSize(1);
//        final AbstractAAS shell = shellEndpoints.get(0);
//        assertThat(shell).isInstanceOf(AasSubmodelDescriptor.class);
//        final AasSubmodelDescriptor aasSubmodelDescriptor = (AasSubmodelDescriptor) shell;
//        assertThat(aasSubmodelDescriptor.getSubmodelEndpointAddress()).isEqualTo(catenaXId);
//    }
//
//    @Test
//    void shouldReturnTombstoneWhenClientThrowsFeignException() {
//        final String catenaXId = "test";
//        final Request request = Request.create(Request.HttpMethod.GET, "url", Map.of(), new byte[0],
//                Charset.defaultCharset(), new RequestTemplate());
//        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
//                new FeignException.NotFound("not found", request, new byte[0], Map.of()));
//        final List<AbstractAAS> shell = dtRegistryFacadeWithMock.getAASSubmodelEndpoints(catenaXId);
//        assertThat(shell).hasSize(1);
//        final AbstractAAS abstractAAS = shell.get(0);
//        assertThat(abstractAAS.getIdentification()).isEqualTo(catenaXId);
//        assertThat(abstractAAS).isInstanceOf(AasTombstone.class);
//        final AasTombstone tombstone = (AasTombstone) abstractAAS;
//        assertThat(tombstone.getProcessingError().getException()).isEqualTo("NotFound");
//    }
//
//    @Test
//    void shouldReturnTombstoneWhenClientReturnsEmptyDescriptor() {
//        final String catenaXId = "test";
//        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
//        shellDescriptor.setSubmodelDescriptors(List.of());
//        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);
//        final List<AbstractAAS> shell = dtRegistryFacadeWithMock.getAASSubmodelEndpoints(catenaXId);
//        assertThat(shell).hasSize(1);
//        final AbstractAAS abstractAAS = shell.get(0);
//        assertThat(abstractAAS.getIdentification()).isEqualTo(catenaXId);
//        assertThat(abstractAAS).isInstanceOf(AasTombstone.class);
//        final AasTombstone tombstone = (AasTombstone) abstractAAS;
//        assertThat(tombstone.getProcessingError().getException()).isEqualTo("Unknown");
//        assertThat(tombstone.getProcessingError().getErrorDetail()).isEqualTo(
//                "No AssemblyPartRelationship Descriptor found");
//    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String catenaXId = "9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d";
        final DigitalTwinRegistryClientLocalStub client = new DigitalTwinRegistryClientLocalStub();

        assertThatExceptionOfType(FeignException.NotFound.class).isThrownBy(
                () -> client.getAssetAdministrationShellDescriptor(catenaXId));
    }
}
