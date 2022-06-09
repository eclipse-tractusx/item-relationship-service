package net.catenax.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;

import org.junit.jupiter.api.Test;

class AASWrapperUriAddressRewritePolicyTest {

    final AASWrapperUriAddressRewritePolicy aasWrapperUriAddressRewritePolicy = new AASWrapperUriAddressRewritePolicy();

    @Test
    void shouldRewriteValidEndpointAddressToAASWrapperUri() {
        final String endpointAddress = "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";

        final URI uri = aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(endpointAddress);

        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getHost()).isEqualTo("aaswrapper");
        assertThat(uri.getPort()).isEqualTo(9191);
        assertThat(uri.getPath()).isEqualTo("/api/service/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel");
        assertThat(uri.getQuery()).isEqualTo("content=value&extent=withBlobValue&provider-connector-url=https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM");
    }

    @Test
    void shouldCreateAASWrapperUriWhenValidEndpointAddress() {
        final String endpointAddress = "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";

        final AASWrapperUriAddressRewritePolicy.AASWrapperUri aasWrapperUri = new AASWrapperUriAddressRewritePolicy.AASWrapperUri(endpointAddress);

        assertThat(aasWrapperUri.getProviderConnectUrl()).isEqualTo("https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM");
        assertThat(aasWrapperUri.getPath()).isEqualTo("/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel");
        assertThat(aasWrapperUri.getQuery()).isEqualTo("content=value&extent=withBlobValue");
    }

    @Test
    void shouldThrowRuntimeExceptionWhenMalformedEndpointAddress() {
        final String malformedEndpointAddress = "http://xxxxNOURN.pl?x=2";

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> new AASWrapperUriAddressRewritePolicy.AASWrapperUri(malformedEndpointAddress));
    }

}
