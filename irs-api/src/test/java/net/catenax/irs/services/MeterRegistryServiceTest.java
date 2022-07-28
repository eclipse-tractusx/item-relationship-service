package net.catenax.irs.services;

import static org.assertj.core.api.Assertions.assertThat;

import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MeterRegistryServiceTest {

    static MeterRegistryService meterRegistryService; // = TestMother.fakeMeterRegistryService();

    @BeforeAll
    static void setup() {
        meterRegistryService = TestMother.simpleMeterRegistryService();
    }

    @Test
    void checkJobStateMetricsIfCorrectlyIncremented() {
        meterRegistryService.recordJobStateMetric(JobState.RUNNING);
        assertThat(meterRegistryService.getJobMetric().getJobRunning().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.TRANSFERS_FINISHED);
        assertThat(meterRegistryService.getJobMetric().getJobProcessed().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.COMPLETED);
        assertThat(meterRegistryService.getJobMetric().getJobSuccessful().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.RUNNING);
        assertThat(meterRegistryService.getJobMetric().getJobRunning().count()).isEqualTo(2);

        meterRegistryService.recordJobStateMetric(JobState.ERROR);
        assertThat(meterRegistryService.getJobMetric().getJobProcessed().count()).isEqualTo(1);

        meterRegistryService.recordJobStateMetric(JobState.CANCELED);
        assertThat(meterRegistryService.getJobMetric().getJobCancelled().count()).isEqualTo(1);

        meterRegistryService.incrementNumberOfJobsInJobStore();
        assertThat(meterRegistryService.getJobMetric().getJobInJobStore().value()).isEqualTo(1);

        meterRegistryService.incrementNumberOfJobsInJobStore();
        meterRegistryService.incrementNumberOfJobsInJobStore();
        meterRegistryService.incrementNumberOfJobsInJobStore();
        assertThat(meterRegistryService.getJobMetric().getJobInJobStore().value()).isEqualTo(4);

        meterRegistryService.decrementNumberOfJobsInJobStore();
        assertThat(meterRegistryService.getJobMetric().getJobInJobStore().value()).isEqualTo(3);

    }

}