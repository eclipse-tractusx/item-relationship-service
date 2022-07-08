package net.catenax.irs.connector.job;

import static net.catenax.irs.util.TestMother.jobParameter;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import net.catenax.irs.util.TestMother;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;

class MultiTransferJobTest {

    Faker faker = new Faker();
    TestMother generate = new TestMother();

    MultiTransferJob job = generate.job();

    @Test
    void getTransferProcessIds_Immutable() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> job.getTransferProcessIds().add(faker.lorem().word()));
    }

    @Test
    void getJobData_Immutable() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(
                () -> jobParameter().getAspectTypes().add(faker.lorem().word()));
    }

}