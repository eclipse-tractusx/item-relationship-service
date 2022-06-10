package net.catenax.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;

import org.junit.jupiter.api.Test;

class AASWrapperUriAddressRewritePolicyTest {

    final AASWrapperUriAddressRewritePolicy aasWrapperUriAddressRewritePolicy = new AASWrapperUriAddressRewritePolicy();

    @Test
    void shouldRewriteValidZFEndpointAddressToAASWrapperUri() {
        final String endpointAddress = "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";

        final URI uri = aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(endpointAddress);

        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getHost()).isEqualTo("aaswrapper");
        assertThat(uri.getPort()).isEqualTo(9191);
        assertThat(uri.getPath()).isEqualTo("/api/service/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel");
        assertThat(uri.getQuery()).isEqualTo("content=value&extent=withBlobValue&provider-connector-url=https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM");
    }

    @Test
    void shouldRewriteValidBMWEndpointAddressToAASWrapperUri() {
        final String endpointAddress = "http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:ea09239f-1dcf-4e51-9907-d7d1d276e53d/submodel?content=value&extent=WithBLOBValue";

        final URI uri = aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(endpointAddress);

        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getHost()).isEqualTo("aaswrapper");
        assertThat(uri.getPort()).isEqualTo(9191);
        assertThat(uri.getPath()).isEqualTo("/api/service/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:ea09239f-1dcf-4e51-9907-d7d1d276e53d/submodel");
        assertThat(uri.getQuery()).isEqualTo("content=value&extent=WithBLOBValue&provider-connector-url=http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE");
    }

    @Test
    void shouldRewriteValidEncodedBMWEndpointAddressToAASWrapperUri() {
        final String endpointAddress = "http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE/urn%3Auuid%3A1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn%3Auuid%3A8147495d-d9c1-4b36-ada5-635d2ef3212f/submodel?content=value&extent=WithBLOBValue";

        final URI uri = aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(endpointAddress);

        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getHost()).isEqualTo("aaswrapper");
        assertThat(uri.getPort()).isEqualTo(9191);
        assertThat(uri.getPath()).isEqualTo("/api/service/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:8147495d-d9c1-4b36-ada5-635d2ef3212f/submodel");
        assertThat(uri.getQuery()).isEqualTo("content=value&extent=WithBLOBValue&provider-connector-url=http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE");
    }

    @Test
    void shouldCreateAASWrapperUriWhenValidEndpointAddress() {
        final String endpointAddress = "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue";

        final AASWrapperUriAddressRewritePolicy.AASWrapperUri aasWrapperUri = new AASWrapperUriAddressRewritePolicy.AASWrapperUri(endpointAddress);

        assertThat(aasWrapperUri.getProviderConnectorUrl()).isEqualTo("https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM");
        assertThat(aasWrapperUri.getPath()).isEqualTo("/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel");
        assertThat(aasWrapperUri.getQuery()).isEqualTo("content=value&extent=withBlobValue");
    }

    @Test
    void shouldThrowRuntimeExceptionWhenMalformedEndpointAddress() {
        final String malformedEndpointAddress = "http://xxxxNOURN.pl?x=2";

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> new AASWrapperUriAddressRewritePolicy.AASWrapperUri(malformedEndpointAddress));
    }

}
