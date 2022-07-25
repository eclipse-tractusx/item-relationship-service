package net.catenax.irs.aaswrapper.registry.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.catenax.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import net.catenax.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import net.catenax.irs.services.OutboundMeterRegistryService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class DigitalTwinRegistryClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final OutboundMeterRegistryService meterRegistry = mock(OutboundMeterRegistryService.class);

    private final DigitalTwinRegistryClientImpl digitalTwinRegistryClient = new DigitalTwinRegistryClientImpl(
            restTemplate, "url", meterRegistry);

    @Test
    void shouldCallExternalServiceOnceAndGetAssetAdministrationShellDescriptor() {
        final String aasIdentifier = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        doReturn(new AssetAdministrationShellDescriptor()).when(restTemplate)
                                                          .getForObject(any(),
                                                                  eq(AssetAdministrationShellDescriptor.class));

        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(
                aasIdentifier);

        assertNotNull(assetAdministrationShellDescriptor);
        verify(this.restTemplate, times(1)).getForObject(any(), eq(AssetAdministrationShellDescriptor.class));
    }

    @Test
    void shouldCallExternalServiceOnceAndGetAssetAdministrationShellIds() {
        doReturn(new ArrayList<>()).when(restTemplate).getForObject(any(), eq(List.class));

        final List<String> empty = digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                Collections.emptyList());

        assertNotNull(empty);
        verify(this.restTemplate, times(1)).getForObject(any(), eq(List.class));
    }

    @Test
    void shouldCallExternalServiceOnceAndRunIntoTimeout() {
        final SocketTimeoutException timeoutException = new SocketTimeoutException("UnitTestTimeout");
        Throwable ex = new ResourceAccessException("UnitTest", timeoutException);
        doThrow(ex).when(restTemplate).getForObject(any(), eq(List.class));

        final List<IdentifierKeyValuePair> emptyList = Collections.emptyList();
        assertThrows(ResourceAccessException.class,
                () -> digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(emptyList));

        verify(this.restTemplate, times(1)).getForObject(any(), eq(List.class));
        verify(meterRegistry).incrementRegistryTimeoutCounter();
    }
}
