package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;

import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

class DecentralDigitalTwinRegistryClientTest {

    RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    @Test
    void shouldCallForAssetAdministrationShellDescriptor() {
        // given
        DecentralDigitalTwinRegistryClient client = new DecentralDigitalTwinRegistryClient(restTemplate);
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance().endpoint("url.to.host").build();
        Mockito.when(restTemplate.getForObject(any(), any())).thenReturn(AssetAdministrationShellDescriptor.builder().build());

        // when
        client.getAssetAdministrationShellDescriptor(endpointDataReference, "aas-id");

        // then
        verify(restTemplate).getForObject(any(), any());
    }

    @Test
    void shouldCallForAllAssetAdministrationShellIdsByAssetLink() {
        // given
        DecentralDigitalTwinRegistryClient client = new DecentralDigitalTwinRegistryClient(restTemplate);
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance().endpoint("url.to.host").build();
        Mockito.when(restTemplate.getForObject(any(), any())).thenReturn(new ArrayList<String>());

        // when
        client.getAllAssetAdministrationShellIdsByAssetLink(endpointDataReference, new ArrayList<>());

        // then
        verify(restTemplate).getForObject(any(), any());
    }

}