package net.catenax.irs.aaswrapper.registry.domain;

import static net.catenax.irs.util.TestMother.jobParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.Endpoint;
import net.catenax.irs.component.assetadministrationshell.SubmodelDescriptor;
import net.catenax.irs.dto.JobParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class DigitalTwinRegistryFacadeTest {

    private final String assemblyPartRelationshipURN = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0";
    private final String serialPartTypizationURN = "urn:bamm:com.catenax.serial_part_typization:1.0.0";
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
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final AssetAdministrationShellDescriptor aasShellDescriptor = digitalTwinRegistryFacade.getAAShellDescriptor(
                catenaXId);
        final List<SubmodelDescriptor> shellEndpoints = aasShellDescriptor.getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().hasSize(2);
        final Endpoint endpoint = shellEndpoints.get(0).getEndpoints().get(0);

        assertThat(endpoint.getProtocolInformation().getEndpointAddress()).isEqualTo(catenaXId);
        assertThat(shellEndpoints.get(0).getSemanticId().getValue()).containsExactly(assemblyPartRelationshipURN);
        assertThat(shellEndpoints.get(1).getSemanticId().getValue()).containsExactly(serialPartTypizationURN);
    }

    @Test
    void shouldThrowExceptionWhenRequestError() {
        final String catenaXId = "test";
        final JobParameter jobParameter = jobParameter();
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
                new RestClientException("Dummy"));

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> dtRegistryFacadeWithMock.getAAShellDescriptor(catenaXId));
    }

    @Test
    void shouldReturnTombstoneWhenClientReturnsEmptyDescriptor() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);

        final List<SubmodelDescriptor> submodelEndpoints = dtRegistryFacadeWithMock.getAAShellDescriptor(catenaXId).getSubmodelDescriptors();
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void verifyExecutionOfRegistryClientMethods() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);

        dtRegistryFacadeWithMock.getAAShellDescriptor(catenaXId);

        verify(this.dtRegistryClientMock, times(1)).getAllAssetAdministrationShellIdsByAssetLink(anyList());
        verify(this.dtRegistryClientMock, times(1)).getAssetAdministrationShellDescriptor(catenaXId);
    }

    @Test
    void shouldReturnAssetAdministrationShellDescriptorForFoundIdentification() {
        final String identification = "identification";
        final String globalAssetId = "globalAssetId";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAllAssetAdministrationShellIdsByAssetLink(anyList())).thenReturn(
                Collections.singletonList(identification));
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(identification)).thenReturn(shellDescriptor);

        final List<SubmodelDescriptor> submodelEndpoints = dtRegistryFacadeWithMock.getAAShellDescriptor(globalAssetId).getSubmodelDescriptors();
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String globalAssetId = "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d";
        final DigitalTwinRegistryClientLocalStub client = new DigitalTwinRegistryClientLocalStub();

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> client.getAssetAdministrationShellDescriptor(globalAssetId));
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenFilteringByAspectType() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelDescriptor> shellEndpoints = digitalTwinRegistryFacade.getAAShellDescriptor(catenaXId).getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().hasSize(2);
        final SubmodelDescriptor endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getSemanticId().getValue()).containsExactly(assemblyPartRelationshipURN);
    }
}
