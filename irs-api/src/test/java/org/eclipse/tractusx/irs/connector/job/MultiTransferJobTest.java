package org.eclipse.tractusx.irs.connector.job;

import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Collection;
import java.util.List;

import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.util.TestMother;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class MultiTransferJobTest {

    final String word = new Faker().lorem().word();

    TestMother generate = new TestMother();

    MultiTransferJob job = generate.job();

    @Test
    void getTransferProcessIds_Immutable() {
        final Collection<String> transferProcessIds = job.getTransferProcessIds();

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> transferProcessIds.add(word));
    }

    @Test
    void getJobData_Immutable() {
        final List<String> aspectTypes = jobParameter().getAspectTypes();

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> aspectTypes.add(word));
    }

    @Test
    void letJobTransistFromOneStateToAnother() {
        MultiTransferJob job2 = generate.job(JobState.INITIAL);
        MultiTransferJob newJob = job2.toBuilder().transitionInProgress().build();

        assertThat(newJob.getJob().getJobState()).isEqualTo(JobState.RUNNING);
    }

}