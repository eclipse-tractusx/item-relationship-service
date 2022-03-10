package net.catenax.prs.connector.job;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MultiTransferJobTest {

    Faker faker = new Faker();
    TestMother generate = new TestMother();

    MultiTransferJob job = generate.job();

    @Test
    void getTransferProcessIds_Immutable() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        job.getTransferProcessIds().add(faker.lorem().word()));
    }

    @Test
    void getJobData_Immutable() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        job.getJobData().put(
                                faker.lorem().word(),
                                faker.lorem().word()));
    }
}