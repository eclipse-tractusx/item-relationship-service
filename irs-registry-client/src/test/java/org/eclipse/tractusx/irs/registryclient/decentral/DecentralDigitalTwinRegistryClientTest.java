package org.eclipse.tractusx.irs.registryclient.decentral;

import static org.eclipse.tractusx.irs.registryclient.TestMother.endpointDataReference;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class DecentralDigitalTwinRegistryClientTest {

    public static final String SHELL_DESCRIPTORS = "/shell-descriptors/{aasIdentifier}";
    public static final String LOOKUP_SHELLS = "/lookup/shells";
    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private DecentralDigitalTwinRegistryClient client;

    @BeforeEach
    void setUp() {
        client = new DecentralDigitalTwinRegistryClient(restTemplate, SHELL_DESCRIPTORS, LOOKUP_SHELLS);
    }

    @Test
    void shouldCallForAssetAdministrationShellDescriptor() {
        // given
        final String contractAgreementId = "contractAgreementId";
        final String endpointUrl = "url.to.host";
        final EndpointDataReference endpointDataReference = endpointDataReference(contractAgreementId, endpointUrl);
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
        final EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                   .endpoint("url.to.host")
                                                                                   .contractId("contractId")
                                                                                   .id("test1")
                                                                                   .authKey("Authorization")
                                                                                   .authCode("authCode")
                                                                                   .build();
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(LookupShellsResponse.class))).thenReturn(
                ResponseEntity.of(Optional.of(LookupShellsResponse.builder().result(Collections.emptyList()).build())));

        // when
        client.getAllAssetAdministrationShellIdsByAssetLink(endpointDataReference, IdentifierKeyValuePair.builder().build());

        // then
        verify(restTemplate).exchange(any(), eq(HttpMethod.GET), any(), eq(LookupShellsResponse.class));
    }

    @Test
    void shouldCallForAllAssetAdministrationShellFilterBy() {
        // given
        final EndpointDataReference endpointDataReference = EndpointDataReference.Builder.newInstance()
                                                                                         .endpoint("url.to.host")
                                                                                         .contractId("contractId")
                                                                                         .id("test1")
                                                                                         .authKey("Authorization")
                                                                                         .authCode("authCode")
                                                                                         .build();
        when(restTemplate.exchange(any(), eq(HttpMethod.GET), any(), eq(LookupShellsResponse.class))).thenReturn(
                ResponseEntity.of(Optional.of(LookupShellsResponse.builder().result(Collections.emptyList()).build())));


        IdentifierKeyValuePairLite digitalTwinTypeKeyValue = IdentifierKeyValuePairLite.builder()
                                                                                       .name("digitalTwinType")
                                                                                       .value(("PartInstance"))
                                                                                       .build();

        IdentifierKeyValuePairLite bpnKeyValue = IdentifierKeyValuePairLite.builder()
                                                                           .name("manufacturerId")
                                                                           .value(("ABC"))
                                                                           .build();

        LookupShellsFilter lookupShellsFilter = LookupShellsFilter.builder()
                                                                  .cursor("cursor")
                                                                  .limit(10)
                                                                  .identifierKeyValuePairs(List.of(digitalTwinTypeKeyValue, bpnKeyValue))
                                                                  .build();
        // when
        client.getAllAssetAdministrationShellIdsByFilter(endpointDataReference, lookupShellsFilter);

        // then
        verify(restTemplate).exchange(any(), eq(HttpMethod.GET), any(), eq(LookupShellsResponse.class));
    }

}