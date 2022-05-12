package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;
import static net.catenax.irs.util.TestMother.jobParameter;
import static net.catenax.irs.util.TestMother.jobParameterFilter;

import java.util.List;

import net.catenax.irs.dto.SubmodelEndpoint;
import net.catenax.irs.dto.SubmodelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

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

        final List<SubmodelEndpoint> shellEndpoints =
                digitalTwinRegistryFacade.getAASSubmodelEndpoints(catenaXId, jobParameter());

        assertThat(shellEndpoints).isNotNull().hasSize(1);
        final SubmodelEndpoint endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getAddress()).isEqualTo(catenaXId);
        assertThat(endpoint.getSubmodelType()).isEqualTo(SubmodelType.ASSEMBLY_PART_RELATIONSHIP);
    }

    @Test
    void shouldThrowExceptionWhenRequestError() {
        final String catenaXId = "test";

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
                new RestClientException("Dummy"));

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> dtRegistryFacadeWithMock.getAASSubmodelEndpoints(catenaXId, jobParameter()));
    }

    @Test
    void shouldReturnTombstoneWhenClientReturnsEmptyDescriptor() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);

        final List<SubmodelEndpoint> submodelEndpoints =
                dtRegistryFacadeWithMock.getAASSubmodelEndpoints(catenaXId, jobParameter());
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String catenaXId = "9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d";
        final DigitalTwinRegistryClientLocalStub client = new DigitalTwinRegistryClientLocalStub();

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> client.getAssetAdministrationShellDescriptor(catenaXId));
    }

    @Test
    void shouldReturnEmptySubmodelEndpointsWhenFilteringByNotMatchingAspectType() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelEndpoint> shellEndpoints =
                digitalTwinRegistryFacade.getAASSubmodelEndpoints(catenaXId, jobParameterFilter());

        assertThat(shellEndpoints).isEmpty();
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenFilteringByAspectType() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelEndpoint> shellEndpoints =
                digitalTwinRegistryFacade.getAASSubmodelEndpoints(catenaXId, jobParameter());

        assertThat(shellEndpoints).isNotNull().hasSize(1);
        final SubmodelEndpoint endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getSubmodelType()).isEqualTo(SubmodelType.ASSEMBLY_PART_RELATIONSHIP);
    }
}
