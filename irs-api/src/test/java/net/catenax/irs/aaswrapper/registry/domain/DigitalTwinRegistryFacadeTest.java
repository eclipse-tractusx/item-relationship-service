package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void shouldReturnSubmodelEndpointsWhenRequestingWithCatenaXId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final List<AbstractAasShell> shellEndpoints = digitalTwinRegistryFacade.getAASSubmodelEndpoint(catenaXId);
        assertThat(shellEndpoints).isNotNull().hasSize(1);
        final AbstractAasShell shell = shellEndpoints.get(0);
        assertThat(shell).isInstanceOf(AasShellSubmodelDescriptor.class);
        final AasShellSubmodelDescriptor aasShellSubmodelDescriptor = (AasShellSubmodelDescriptor) shell;
        assertThat(aasShellSubmodelDescriptor.getSubmodelEndpointAddress()).isEqualTo(catenaXId);
    }

    @Test
    void shouldReturnTombstoneWhenCallingFacadeWithFeignClientLocally() {
        final String catenaXId = "test";
        final Request request = Request.create(Request.HttpMethod.GET, "url", Map.of(), new byte[0],
                Charset.defaultCharset(), new RequestTemplate());
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
                new FeignException.NotFound("not found", request, new byte[0], Map.of()));
        final List<AbstractAasShell> shell = dtRegistryFacadeWithMock.getAASSubmodelEndpoint(catenaXId);
        assertThat(shell).hasSize(1);
        final AbstractAasShell abstractAasShell = shell.get(0);
        assertThat(abstractAasShell.getIdentification()).isEqualTo(catenaXId);
        assertThat(abstractAasShell).isInstanceOf(AasShellTombstone.class);
        final AasShellTombstone tombstone = (AasShellTombstone) abstractAasShell;
        assertThat(tombstone.getProcessingError().getException()).isPresent();
        assertThat(tombstone.getProcessingError().getException().get()).isInstanceOf(FeignException.class);
    }

    @Test
    void shouldReturnTombstoneWhenFeignClientReturnsEmptyDescriptor() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);
        final List<AbstractAasShell> shell = dtRegistryFacadeWithMock.getAASSubmodelEndpoint(catenaXId);
        assertThat(shell).hasSize(1);
        final AbstractAasShell abstractAasShell = shell.get(0);
        assertThat(abstractAasShell.getIdentification()).isEqualTo(catenaXId);
        assertThat(abstractAasShell).isInstanceOf(AasShellTombstone.class);
        final AasShellTombstone tombstone = (AasShellTombstone) abstractAasShell;
        assertThat(tombstone.getProcessingError().getException()).isEmpty();
        assertThat(tombstone.getProcessingError().getErrorDetail()).isEqualTo("No AssemblyPartRelationship Descriptor found");
    }
}
