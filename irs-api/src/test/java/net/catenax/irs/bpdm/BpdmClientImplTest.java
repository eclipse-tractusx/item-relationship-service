package net.catenax.irs.bpdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class BpdmClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final BpdmClient bpdmClient = new BpdmClientImpl(restTemplate, "url");

    @Test
    void shouldCallExternalServiceOnceAndGetBusinessPartnerResponse() {
        final String bpn = "BPNL00000003AYRE";
        final BusinessPartnerResponse mockResponse = BusinessPartnerResponse.builder().bpn(bpn).build();
        doReturn(mockResponse).when(restTemplate).getForObject(any(), eq(BusinessPartnerResponse.class));

        final BusinessPartnerResponse businessPartner = bpdmClient.getBusinessPartner(bpn, "BPN");

        assertThat(businessPartner).isNotNull();
        assertThat(businessPartner.getBpn()).isEqualTo(bpn);
        verify(this.restTemplate, times(1)).getForObject(any(), eq(BusinessPartnerResponse.class));
    }

}
