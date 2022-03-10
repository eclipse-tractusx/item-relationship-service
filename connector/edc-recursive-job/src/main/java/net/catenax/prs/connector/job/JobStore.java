//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.job;

import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Manages storage of {@link MultiTransferJob} state.
 */
public interface JobStore {
    /**
     * Retrieve a job by its identifier.
     *
     * @param jobId the identifier of the job to retrieve.
     * @return the job if found, otherwise empty.
     * @see MultiTransferJob#getJobId()
     */
    Optional<MultiTransferJob> find(String jobId);

    /**
     * Retrieve a job given a transfer id. Only retrieves jobs
     * for which the transfer has not been completed
     * with {@link #completeTransferProcess(String, TransferProcess)}.
     *
     * @param processId the transfer process identifier.
     * @return the job if found, otherwise empty.
     */
    Optional<MultiTransferJob> findByProcessId(String processId);

    /**
     * Create a job.
     *
     * @param job the job to create and manage.
     */
    void create(MultiTransferJob job);

    /**
     * Add a transfer process identifier to a job.
     *
     * @param jobId     the job identifier.
     * @param processId identifier of the transfer process to attach.
     */
    void addTransferProcess(String jobId, String processId);

    /**
     * Mark transfer process completed for the job.
     *
     * @param jobId     the job identifier.
     * @param processId identifier of the transfer process to mark completed.
     */
    void completeTransferProcess(String jobId, TransferProcess processId);

    /**
     * Mark job as completed.
     *
     * @param jobId the job identifier.
     * @see JobState#COMPLETED
     */
    void completeJob(String jobId);

    /**
     * Mark job as in error.
     *
     * @param jobId       the job identifier.
     * @param errorDetail an optional error message.
     * @see JobState#ERROR
     */
    void markJobInError(String jobId, @Nullable String errorDetail);
}
