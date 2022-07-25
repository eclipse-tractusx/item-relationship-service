package net.catenax.irs.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.catenax.irs.component.enums.JobState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeterRegistryServiceTest {

    @Mock
    MeterRegistryService meterRegistryService; // = TestMother.fakeMeterRegistryService();

    @Test
    void recordJobStateMetricTest() {
        meterRegistryService.recordJobStateMetric(JobState.RUNNING);
        verify(meterRegistryService, times(1)).recordJobStateMetric(any());

        meterRegistryService.incrementJobsProcessed();
        verify(meterRegistryService, times(1)).incrementJobsProcessed();

        meterRegistryService.incrementJobSuccessful();
        verify(meterRegistryService, times(1)).incrementJobSuccessful();

        meterRegistryService.incrementJobRunning();
        verify(meterRegistryService, times(1)).incrementJobRunning();

        meterRegistryService.incrementJobFailed();
        verify(meterRegistryService, times(1)).incrementJobFailed();

        meterRegistryService.incrementJobCancelled();
        verify(meterRegistryService, times(1)).incrementJobCancelled();

        meterRegistryService.incrementJobInJobStore(4.0);
        verify(meterRegistryService, times(1)).incrementJobInJobStore(any());

    }

}