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
import net.catenax.irs.dto.JobDataDTO;
import net.catenax.irs.dto.SubmodelEndpoint;
import net.catenax.irs.dto.SubmodelType;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DigitalTwinRegistryFacadeTest {

    private DigitalTwinRegistryFacade digitalTwinRegistryFacade;

    private final TestMother generate = new TestMother();

    private JobDataDTO jobDataDTO;

    private JobDataDTO jobDataFilter;

    @Mock
    private DigitalTwinRegistryClient dtRegistryClientMock;

    private DigitalTwinRegistryFacade dtRegistryFacadeWithMock;

    @BeforeEach
    void setUp() {
        digitalTwinRegistryFacade = new DigitalTwinRegistryFacade(new DigitalTwinRegistryClientLocalStub());
        dtRegistryFacadeWithMock = new DigitalTwinRegistryFacade(dtRegistryClientMock);
        jobDataDTO = generate.jobDataDTO();
        jobDataFilter = generate.jobDataFilter();
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenRequestingWithCatenaXId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelEndpoint> shellEndpoints = digitalTwinRegistryFacade.getAASSubmodelEndpoints(catenaXId, jobDataDTO);

        assertThat(shellEndpoints).isNotNull().hasSize(1);
        final SubmodelEndpoint endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getAddress()).isEqualTo(catenaXId);
        assertThat(endpoint.getSubmodelType()).isEqualTo(SubmodelType.ASSEMBLY_PART_RELATIONSHIP);
    }

    @Test
    void shouldThrowExceptionWhenRequestError() {
        final String catenaXId = "test";
        final Request request = Request.create(Request.HttpMethod.GET, "url", Map.of(), new byte[0],
                Charset.defaultCharset(), new RequestTemplate());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
                new FeignException.NotFound("not found", request, new byte[0], Map.of()));

        assertThatExceptionOfType(FeignException.class).isThrownBy(
                () -> dtRegistryFacadeWithMock.getAASSubmodelEndpoints(catenaXId, jobDataDTO));
    }

    @Test
    void shouldReturnTombstoneWhenClientReturnsEmptyDescriptor() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);

        final List<SubmodelEndpoint> submodelEndpoints = dtRegistryFacadeWithMock.getAASSubmodelEndpoints(catenaXId, jobDataDTO);
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String catenaXId = "9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d";
        final DigitalTwinRegistryClientLocalStub client = new DigitalTwinRegistryClientLocalStub();

        assertThatExceptionOfType(FeignException.NotFound.class).isThrownBy(
                () -> client.getAssetAdministrationShellDescriptor(catenaXId));
    }

    @Test
    void shouldReturnEmptySubmodelEndpointsWhenFilteringByNotMatchingAspectType() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelEndpoint> shellEndpoints = digitalTwinRegistryFacade.getAASSubmodelEndpoints(catenaXId, jobDataFilter);

        assertThat(shellEndpoints).isEmpty();
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenFilteringByAspectType() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelEndpoint> shellEndpoints = digitalTwinRegistryFacade.getAASSubmodelEndpoints(catenaXId, jobDataDTO);

        assertThat(shellEndpoints).isNotNull().hasSize(1);
        final SubmodelEndpoint endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getSubmodelType()).isEqualTo(SubmodelType.ASSEMBLY_PART_RELATIONSHIP);
    }
}
