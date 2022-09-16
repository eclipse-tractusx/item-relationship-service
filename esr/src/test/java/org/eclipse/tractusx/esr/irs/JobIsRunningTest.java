package org.eclipse.tractusx.esr.irs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JobIsRunningTest {

    @Test
    public void shouldRecognizeWhenJobIsRunning() {
        // given
        JobIsRunning jobIsRunning = new JobIsRunning();

        // when
        boolean result = jobIsRunning.test(IrsResponse.builder().job(Job.builder().jobState("RUNNING").build()).build());

        // then
        assertThat(result).isTrue();
    }

    @Test
    public void shouldRecognizeWhenJobIsCompleted() {
        // given
        JobIsRunning jobIsRunning = new JobIsRunning();

        // when
        boolean result = jobIsRunning.test(IrsResponse.builder().job(Job.builder().jobState("COMPLETED").build()).build());

        // then
        assertThat(result).isFalse();
    }

}