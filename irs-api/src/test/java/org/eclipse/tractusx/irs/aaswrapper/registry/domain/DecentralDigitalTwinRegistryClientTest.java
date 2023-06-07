package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class DecentralDigitalTwinRegistryClientTest {

    RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    @Test
    void shouldCallForAssetAdministrationShellDescriptor() {
        // given
        DecentralDigitalTwinRegistryClient client = new DecentralDigitalTwinRegistryClient(restTemplate);
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .build();
        Mockito.when(
                       restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(AssetAdministrationShellDescriptor.class)))
               .thenReturn(ResponseEntity.of(Optional.of(AssetAdministrationShellDescriptor.builder().build())));

        // when
        client.getAssetAdministrationShellDescriptor(endpointDataReference, "aas-id");

        // then
        verify(restTemplate).exchange(any(), eq(HttpMethod.GET), any(), eq(AssetAdministrationShellDescriptor.class));
    }

    @Test
    void shouldCallForAllAssetAdministrationShellIdsByAssetLink() {
        // given
        DecentralDigitalTwinRegistryClient client = new DecentralDigitalTwinRegistryClient(restTemplate);
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .build();
        Mockito.when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(List.class)))
               .thenReturn(ResponseEntity.of(Optional.of(new ArrayList<>())));

        // when
        client.getAllAssetAdministrationShellIdsByAssetLink(endpointDataReference, new ArrayList<>());

        // then
        verify(restTemplate).exchange(any(), eq(HttpMethod.GET), any(), eq(List.class));
    }

}