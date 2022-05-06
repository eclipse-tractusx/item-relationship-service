package net.catenax.irs.connector.job;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.github.javafaker.Faker;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.dto.JobDataDTO;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.Test;

class MultiTransferJobTest {

    Faker faker = new Faker();
    TestMother generate = new TestMother();

    MultiTransferJob job = generate.job();
    JobDataDTO jobDataDTO = generate.jobDataDTO();

    @Test
    void getTransferProcessIds_Immutable() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() ->
                        job.getTransferProcessIds().add(faker.lorem().word()));
    }

    @Test
    void getJobData_Immutable() {
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> jobDataDTO.getAspectTypes().add(faker.lorem().word()));
    }

}