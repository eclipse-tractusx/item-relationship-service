package net.catenax.irs;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.controllers.IrsController;
import net.catenax.irs.testing.containers.MinioContainer;
import net.catenax.irs.util.TestMother;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = IrsFunctionalTest.MinioConfigInitializer.class)
@ActiveProfiles(profiles = { "local" })
class IrsFunctionalTest {
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_KEY = "secretKey";

    private static final MinioContainer minioContainer = new MinioContainer(
            new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY)).withReuse(true);
    @Autowired
    private IrsController controller;

    @BeforeAll
    static void startContainer() {
        minioContainer.start();
    }

    @AfterAll
    static void stopContainer() {
        minioContainer.stop();
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldStartJobAndRetrieveResult() {
        final RegisterJob registerJob = TestMother.registerJobWithoutDepth();

        final JobHandle jobHandle = controller.registerJobForGlobalAssetId(registerJob);
        final Optional<Jobs> finishedJob = Awaitility.await()
                                                     .pollDelay(500, TimeUnit.MILLISECONDS)
                                                     .pollInterval(500, TimeUnit.MILLISECONDS)
                                                     .atMost(5, TimeUnit.SECONDS)
                                                     .until(getJobDetails(jobHandle),
                                                             jobs -> jobs.isPresent() && jobs.get()
                                                                                             .getJob()
                                                                                             .getJobState()
                                                                                             .equals(JobState.COMPLETED));

        assertThat(finishedJob).isPresent();
        assertThat(finishedJob.get().getRelationships()).isNotEmpty();
    }

    @NotNull
    private Callable<Optional<Jobs>> getJobDetails(final JobHandle jobHandle) {
        return () -> {
            try {
                return Optional.ofNullable(controller.getJobById(jobHandle.getJobId(), true));
            } catch (Exception e) {
                e.printStackTrace();
                return Optional.empty();
            }
        };
    }

    public static class MinioConfigInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            final String hostAddress = minioContainer.getHostAddress();
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
                    "blobstore.endpoint=http://" + hostAddress, "blobstore.accessKey=" + ACCESS_KEY,
                    "blobstore.secretKey=" + SECRET_KEY);
        }
    }

}
