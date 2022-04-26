package net.catenax.irs.connector.job;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.javafaker.Faker;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.Test;

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