package net.catenax.irs.services;

import static net.catenax.irs.util.CommonConstant.ERROR_RESPONSE;
import static net.catenax.irs.util.TestMother.registerJobWithoutDepth;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.catenax.irs.TestConfig;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.config.AsyncConfigTest;
import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.persistence.BlobPersistence;
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

    private final UUID jobId = UUID.randomUUID();

    private MockMvc mockMvc;

    @Mock
    private Executor executorService;

    @Mock
    AsyncJobHandlerService asyncHandlerService;

    @BeforeEach
    void setUp() {
        executorService = mock(Executor.class);
        asyncHandlerService = mock(AsyncJobHandlerService.class);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void registerJob() throws Exception {
        final RegisterJob registerJob = registerJobWithoutDepth();
        JobInitiateResponse response = JobInitiateResponse.builder()
                                                          .jobId(jobId.toString())
                                                          .status(ResponseStatus.OK)
                                                          .error(EMPTY_STRING)
                                                          .build();

        JobInitiateResponse errorResponse = JobInitiateResponse.builder()
                                                               .jobId(jobId.toString())
                                                               .status(ResponseStatus.ERROR_RETRY)
                                                               .error(ERROR_RESPONSE)
                                                               .build();

        /*executorService.execute(new Runnable() {
            @Override
            public void run() {
                response.complete(JobInitiateResponse.builder()
                                                     .status(ResponseStatus.OK)
                                                     .jobId(jobId.toString())
                                                     .error(EMPTY_STRING)
                                                     .build());
            }
        });*/

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
    void cancelJob() {
    }

    @Test
    void interruptJob() {
    }

    @Test
    void getPartialJobResult() {
    }

    @Test
    void getCompleteJobResult() {

    }

    public static class ResponseAnswer {

    }
}