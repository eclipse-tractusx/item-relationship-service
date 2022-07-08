package net.catenax.irs.aaswrapper.submodel.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AASWrapperUriAddressRewritePolicyTest {

    final AASWrapperUriAddressRewritePolicy aasWrapperUriAddressRewritePolicy = new AASWrapperUriAddressRewritePolicy("http://aaswrapper:9191/api/service");

    /**
     * @param endpointAddress input to rewrite
     * @param path expected path result
     * @param query expected query result
     */
    @ParameterizedTest
    @CsvSource({
            "https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel?content=value&extent=withBlobValue, /api/service/urn:uuid:5a7ab616-989f-46ae-bdf2-32027b9f6ee6-urn:uuid:31b614f5-ec14-4ed2-a509-e7b7780083e7/submodel, content=value&extent=withBlobValue&provider-connector-url=https://edc-ocp0900009.apps.c7von4sy.westeurope.aroapp.io/BPNL00000003B2OM",
            "http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:ea09239f-1dcf-4e51-9907-d7d1d276e53d/submodel?content=value&extent=WithBLOBValue, /api/service/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:ea09239f-1dcf-4e51-9907-d7d1d276e53d/submodel, content=value&extent=WithBLOBValue&provider-connector-url=http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE",
            "http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE/urn%3Auuid%3A1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn%3Auuid%3A8147495d-d9c1-4b36-ada5-635d2ef3212f/submodel?content=value&extent=WithBLOBValue, /api/service/urn:uuid:1e3b27b5-63a7-4ba3-85a8-0c44bf001399-urn:uuid:8147495d-d9c1-4b36-ada5-635d2ef3212f/submodel, content=value&extent=WithBLOBValue&provider-connector-url=http://connector.cx-rel.edc.aws.bmw.cloud:8282/BPNL00000003AYRE",
    })
    void shouldRewriteValidEndpointAddressToAASWrapperUri(final String endpointAddress, final String path, final String query) {
        final URI uri = aasWrapperUriAddressRewritePolicy.rewriteToAASWrapperUri(endpointAddress);

        assertThat(uri.getScheme()).isEqualTo("http");
        assertThat(uri.getHost()).isEqualTo("aaswrapper");
        assertThat(uri.getPort()).isEqualTo(9191);
        assertThat(uri.getPath()).isEqualTo(path);
        assertThat(uri.getQuery()).isEqualTo(query);
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
