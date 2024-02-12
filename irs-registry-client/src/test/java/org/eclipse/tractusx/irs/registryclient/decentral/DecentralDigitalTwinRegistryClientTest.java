package org.eclipse.tractusx.irs.registryclient.decentral;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class DecentralDigitalTwinRegistryClientTest {

    public static final String SHELL_DESCRIPTORS = "/shell-descriptors/{aasIdentifier}";
    public static final String LOOKUP_SHELLS = "/lookup/shells";
    RestTemplate restTemplate = mock(RestTemplate.class);
    private DecentralDigitalTwinRegistryClient client;

    @BeforeEach
    void setUp() {
        client = new DecentralDigitalTwinRegistryClient(restTemplate, SHELL_DESCRIPTORS, LOOKUP_SHELLS);
    }

    @Test
    void shouldCallForAssetAdministrationShellDescriptor() {
        // given
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .build();
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(),
                eq(AssetAdministrationShellDescriptor.class))).thenReturn(
                ResponseEntity.of(Optional.of(AssetAdministrationShellDescriptor.builder().build())));

        // when
        client.getAssetAdministrationShellDescriptor(endpointDataReference, "aas-id");

        // then
        verify(restTemplate).exchange(eq(URI.create("url.to.host/shell-descriptors/YWFzLWlk")), eq(HttpMethod.GET),
                any(), eq(AssetAdministrationShellDescriptor.class));
    }

    @Test
    void shouldCallForAllAssetAdministrationShellIdsByAssetLink() {
        // given
        EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .build();
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(LookupShellsResponse.class))).thenReturn(
                ResponseEntity.of(Optional.of(LookupShellsResponse.builder().result(Collections.emptyList()).build())));

        // when
        client.getAllAssetAdministrationShellIdsByAssetLink(endpointDataReference, new ArrayList<>());

        // then
        verify(restTemplate).exchange(any(), eq(HttpMethod.GET), any(), eq(LookupShellsResponse.class));
    }

}