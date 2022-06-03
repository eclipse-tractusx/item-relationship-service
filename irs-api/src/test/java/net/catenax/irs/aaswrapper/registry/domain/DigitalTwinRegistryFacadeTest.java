package net.catenax.irs.aaswrapper.registry.domain;

import static net.catenax.irs.util.TestMother.jobParameter;
import static net.catenax.irs.util.TestMother.jobParameterEmptyFilter;
import static net.catenax.irs.util.TestMother.jobParameterFilter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

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

        final AssetAdministrationShellDescriptor aasShellDescriptor = digitalTwinRegistryFacade.getAASShellDescriptor(
                catenaXId, jobParameter());
        System.out.println(aasShellDescriptor);
        final List<SubmodelDescriptor> shellEndpoints = aasShellDescriptor.getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().hasSize(2);
        final Endpoint endpoint = shellEndpoints.get(0).getEndpoints().get(0);

        assertThat(endpoint.getProtocolInformation().getEndpointAddress()).isEqualTo(catenaXId);
        assertThat(shellEndpoints.get(0).getSemanticId().getValue()).containsExactly(
                SubmodelType.ASSEMBLY_PART_RELATIONSHIP.getValue());
        assertThat(shellEndpoints.get(1).getSemanticId().getValue()).containsExactly(
                SubmodelType.SERIAL_PART_TYPIZATION.getValue());
    }

    @Test
    void shouldThrowExceptionWhenRequestError() {
        final String catenaXId = "test";
        final JobParameter jobParameter = jobParameter();
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
                new RestClientException("Dummy"));

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> dtRegistryFacadeWithMock.getAASShellDescriptor(catenaXId, jobParameter));
    }

    @Test
    void shouldReturnTombstoneWhenClientReturnsEmptyDescriptor() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);

        final List<SubmodelDescriptor> submodelEndpoints = dtRegistryFacadeWithMock.getAASShellDescriptor(catenaXId,
                jobParameter()).getSubmodelDescriptors();
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
    void shouldReturnAssemblyPartRelationshipSubmodelEndpointsWhenFilteringByNotMatchingAspectType() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelDescriptor> shellEndpoints = digitalTwinRegistryFacade.getAASShellDescriptor(catenaXId,
                jobParameterFilter()).getSubmodelDescriptors();

        assertThat(shellEndpoints).hasSize(1);
        assertThat(shellEndpoints.get(0).getSemanticId().getValue()).contains(assemblyPartRelationshipURN);
    }

    @Test
    void shouldReturnAssemblyPartRelationshipSubmodelEndpointsWhenFilteringByNoAspectType() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelDescriptor> shellEndpoints = digitalTwinRegistryFacade.getAASShellDescriptor(catenaXId,
                jobParameterEmptyFilter()).getSubmodelDescriptors();

        assertThat(shellEndpoints).hasSize(1);
        assertThat(shellEndpoints.get(0).getSemanticId().getValue()).contains(assemblyPartRelationshipURN);
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenFilteringByAspectType() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelDescriptor> shellEndpoints = digitalTwinRegistryFacade.getAASShellDescriptor(catenaXId,
                jobParameter()).getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().hasSize(2);
        final SubmodelDescriptor endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getSemanticId().getValue()).containsExactly(
                SubmodelType.ASSEMBLY_PART_RELATIONSHIP.getValue());
    }
}
