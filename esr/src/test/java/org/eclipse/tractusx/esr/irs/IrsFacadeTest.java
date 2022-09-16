package org.eclipse.tractusx.esr.irs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IrsFacadeTest {

    @Mock
    private IrsClient irsClient;

    @InjectMocks
    private IrsFacade irsFacade;

    @Test
    public void shouldFetchIrsResponseBasedOnInputs() {
        // given
        String globalAssetId = "global-asset-id";
        String bomLifecycle = "asBuilt";
        String token = "auth-token";
        String jobId = "job-id";
        IrsResponse expectedResponse = IrsResponse.builder()
                .job(Job.builder().jobState("COMPLETED").build())
                .relationships(new ArrayList<>())
                .shells(new ArrayList<>())
                .build();

        BDDMockito.given(irsClient.startJob(
                IrsRequest.builder().globalAssetId(globalAssetId).bomLifecycle(bomLifecycle).build(),
                token)
        ).willReturn(StartJobResponse.builder().jobId(jobId).build());

        BDDMockito.given(irsClient.getJobDetails(jobId, token)
        ).willReturn(expectedResponse);

        // when
        IrsResponse actualResponse = irsFacade.getIrsResponse(globalAssetId, bomLifecycle, token);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

}