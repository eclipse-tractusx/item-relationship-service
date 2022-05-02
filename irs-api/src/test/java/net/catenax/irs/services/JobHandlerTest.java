package net.catenax.irs.services;

import static net.catenax.irs.util.CommonConstant.ERROR_RESPONSE;
import static net.catenax.irs.util.TestMother.registerJobWithoutDepth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.catenax.irs.TestConfig;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.config.AsyncConfigTest;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.util.JobsHelper;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @TestPropertySource(locations = "classpath:asynctest.yaml")
@ActiveProfiles(profiles = { "async" })
@EnableAsync(proxyTargetClass = true)
@Import({ JobStore.class,
          BlobPersistence.class,
          AsyncConfigTest.class,
          TestConfig.class,
          AsyncJobHandlerService.class,
})
class JobHandlerTest {

    private static final String EMPTY_STRING = "";
    private final UUID JOB_ID = UUID.fromString("e5347c88-a921-11ec-b909-0242ac120002");
    private static final String GLOBAL_ASSET_ID = "urn:uuid:6c311d29-5753-46d4-b32c-19b918ea93b0";
    private static final String JOB_HANDLE_ID_1 = "6c311d29-5753-46d4-b32c-19b918ea93b0";

    private MockMvc mockMvc;

    @Mock
    private Executor executorService;

    @Mock
    AsyncJobHandlerService asyncHandlerService;

    TestMother tester;

    JobsHelper helper;

    @BeforeEach
    void setUp() {
        executorService = mock(Executor.class);
        asyncHandlerService = mock(AsyncJobHandlerService.class);
        tester = new TestMother();
        helper = new JobsHelper();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void registerJob() throws Exception {
        final RegisterJob registerJob = registerJobWithoutDepth();
        JobInitiateResponse response = JobInitiateResponse.builder()
                                                          .jobId(JOB_ID.toString())
                                                          .status(ResponseStatus.OK)
                                                          .error(EMPTY_STRING)
                                                          .build();

        JobInitiateResponse errorResponse = JobInitiateResponse.builder()
                                                               .jobId(JOB_ID.toString())
                                                               .status(ResponseStatus.ERROR_RETRY)
                                                               .error(ERROR_RESPONSE)
                                                               .build();

        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        when(asyncHandlerService.registerJob(any(RegisterJob.class))).thenReturn(
                CompletableFuture.completedFuture(response));

        CompletableFuture<JobInitiateResponse> resultResponse = asyncHandlerService.registerJob(registerJob);

        assertThat(resultResponse.get()).usingRecursiveComparison()
                                        .isEqualTo(response);
    }

    @Test
    void cancelJob() throws Exception {

        Job job = helper.createJob(JOB_ID.toString(), GLOBAL_ASSET_ID, JobState.CANCELED);

        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        when(asyncHandlerService.cancelJob(any(UUID.class))).thenReturn(
                CompletableFuture.completedFuture(Optional.of(job)));

        CompletableFuture<Optional<Job>> resultResponse = asyncHandlerService.cancelJob(JOB_ID);

        assertThat(resultResponse.get().get().getJobState()).isEqualTo(JobState.CANCELED);
        assertThat(resultResponse.get().get()).usingRecursiveComparison()
                                              .ignoringFields(job.getJobId().toString())
                                              .isEqualTo(job);
    }

    @Test
    void getPartialJobResult() throws Exception {
        Jobs jobs = helper.createPartialJobResult(JOB_ID.toString(), GLOBAL_ASSET_ID);

        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        when(asyncHandlerService.getPartialJobResult(any(UUID.class))).thenReturn(
                CompletableFuture.completedFuture(jobs));

        CompletableFuture<Jobs> resultResponse = asyncHandlerService.getPartialJobResult(JOB_ID);

        assertThat(resultResponse.get()).usingRecursiveComparison()
                                        .isEqualTo(jobs);
    }

    @Test
    void getCompleteJobResult() throws Exception {
        Jobs jobs = helper.createCompleteJobResult(JOB_ID.toString(), GLOBAL_ASSET_ID);

        doAnswer((InvocationOnMock invocation) -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executorService).execute(any(Runnable.class));

        when(asyncHandlerService.getCompleteJobResult(any(UUID.class))).thenReturn(
                CompletableFuture.completedFuture(jobs));

        CompletableFuture<Jobs> resultResponse = asyncHandlerService.getCompleteJobResult(JOB_ID);

        assertThat(resultResponse.get()).usingRecursiveComparison()
                                        .isEqualTo(jobs);

    }

}