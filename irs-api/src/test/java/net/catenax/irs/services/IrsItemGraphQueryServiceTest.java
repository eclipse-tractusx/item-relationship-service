package net.catenax.irs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.util.JsonUtil;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IrsItemGraphQueryServiceTest {

    private final TestMother generate = new TestMother();

    @Mock
    private JobStore jobStore;

    @Mock
    private BlobPersistence blobStore;

    @Mock
    private JobOrchestrator<ItemDataRequest, AASTransferProcess> orchestrator;

    @InjectMocks
    private IrsItemGraphQueryService testee;

    @Test
    void registerItemJobWithoutDepthShouldBuildFullTree() throws Exception {
        // given
        final var jobId = UUID.randomUUID();
        final AASTransferProcess transfer1 = generate.aasTransferProcess();
        givenTransferResultIsStored(transfer1);

        final AASTransferProcess transfer2 = generate.aasTransferProcess();
        givenTransferResultIsStored(transfer2);

        givenRunningJobHasFinishedTransfers(jobId, transfer1, transfer2);

        // when
        final Jobs jobs = testee.getJobForJobId(jobId, true);

        // then
        assertThat(jobs.getRelationships()).hasSize(2);
    }

    @Test
    void cancelJobById() {
        final Job job = Job.builder()
                           .jobId(jobId)
                           .createdOn(Instant.now())
                           .lastModifiedOn(Instant.now())
                           .jobState(JobState.CANCELED)
                           .build();

        final MultiTransferJob multiTransferJob = MultiTransferJob.builder().job(job).build();

        when(jobStore.cancelJob(jobId.toString())).thenReturn(Optional.ofNullable(multiTransferJob));
        final Job canceledJob = service.cancelJobById(jobId);
    private void givenRunningJobHasFinishedTransfers(final UUID jobId, final AASTransferProcess... transfers) {
        final MultiTransferJob job = MultiTransferJob.builder()
                                                     .completedTransfers(Arrays.asList(transfers))
                                                     .job(generate.fakeJob(JobState.RUNNING))
                                                     .build();
        when(jobStore.find(jobId.toString())).thenReturn(Optional.of(job));
    }

        assertNotNull(canceledJob);
        assertEquals(canceledJob.getJobId(), jobId);
        assertEquals(canceledJob.getJobState().name(), JobState.CANCELED.name());
    }

    @Test
    void cancelJobById_throwEntityNotFoundException() {
        when(jobStore.cancelJob(jobId.toString())).thenThrow(
                new EntityNotFoundException("No job exists with id " + jobId));

        assertThrows(EntityNotFoundException.class, () -> service.cancelJobById(jobId));
    private void givenTransferResultIsStored(final AASTransferProcess transfer1) throws BlobPersistenceException {
        final AssemblyPartRelationshipDTO relationship1 = generate.assemblyPartRelationshipDTO();
        final ItemContainer itemContainer1 = ItemContainer.builder().assemblyPartRelationship(relationship1).build();
        when(blobStore.getBlob(transfer1.getId())).thenReturn(Optional.of(toBlob(itemContainer1)));
    }

    private byte[] toBlob(final Object transfer) {
        return new JsonUtil().asString(transfer).getBytes(StandardCharsets.UTF_8);
    }

}