package org.eclipse.tractusx.esr.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        String jobId = "job-id";
        IrsResponse expectedResponse = IrsResponse.builder()
                .job(Job.builder().jobState("COMPLETED").build())
                .relationships(new ArrayList<>())
                .shells(new ArrayList<>())
                .build();

        given(irsClient.startJob(
                IrsRequest.builder().globalAssetId(globalAssetId).bomLifecycle(bomLifecycle).depth(1).build())
        ).willReturn(StartJobResponse.builder().jobId(jobId).build());

        given(irsClient.getJobDetails(jobId)
        ).willReturn(expectedResponse);

        // when
        IrsResponse actualResponse = irsFacade.getIrsResponse(globalAssetId, bomLifecycle);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

}