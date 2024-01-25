/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.connector.job;

import static org.eclipse.tractusx.irs.configuration.JobConfiguration.JOB_BLOB_PERSISTENCE;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.services.MeterRegistryService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Stores Job data using persistent blob storage.
 */
@Service
@Slf4j
public class PersistentJobStore extends BaseJobStore {

    /**
     * The prefix for job IDs used as key in the blobstore
     */
    private static final String JOB_PREFIX = "job:";

    private final BlobPersistence blobStore;

    private final JsonUtil json = new JsonUtil();

    private final MeterRegistryService meterService;

    public PersistentJobStore(@Qualifier(JOB_BLOB_PERSISTENCE) final BlobPersistence blobStore,
            final MeterRegistryService meterService) {
        super();
        this.blobStore = blobStore;
        this.meterService = meterService;
    }

    @Override
    protected Optional<MultiTransferJob> get(final String jobId) {
        try {
            return blobStore.getBlob(toBlobId(jobId)).flatMap(this::toJob);
        } catch (BlobPersistenceException e) {
            log.error("Error while trying to get job from blobstore", e);
            return Optional.empty();
        }
    }

    @Override
    protected Collection<MultiTransferJob> getAll() {
        try {
            final Collection<byte[]> allBlobs = blobStore.findBlobByPrefix(JOB_PREFIX);
            return allBlobs.stream().map(this::toJob).flatMap(Optional::stream).toList();
        } catch (BlobPersistenceException e) {
            log.error("Cannot search for jobs in blobstore", e);
            return Collections.emptyList();
        }
    }

    @Override
    protected void put(final String jobId, final MultiTransferJob job) {
        final byte[] blob = toBlob(job);
        try {
            if (!isLastStateSameAsCurrentState(jobId, job.getJob().getState())) {
                meterService.recordJobStateMetric(job.getJob().getState());
            }
            blobStore.putBlob(toBlobId(jobId), blob);
        } catch (BlobPersistenceException e) {
            log.error("Cannot create job in BlobStore", e);
        }
    }

    @Override
    protected Optional<MultiTransferJob> remove(final String jobId) {
        try {
            final Optional<MultiTransferJob> job = blobStore.getBlob(toBlobId(jobId)).flatMap(this::toJob);

            if (job.isPresent()) {
                final List<String> ids = Stream.concat(job.get().getTransferProcessIds().stream(),
                                                       job.get().getCompletedTransfers().stream().map(TransferProcess::getId))
                                               .collect(Collectors.toList());
                ids.add(jobId);

                blobStore.delete(toBlobId(jobId), ids);
            }
            return job;
        } catch (BlobPersistenceException e) {
            throw new JobException("Blob persistence error", e);
        }
    }

    private Optional<MultiTransferJob> toJob(final byte[] blob) {
        try {
            return Optional.of(json.fromString(new String(blob, StandardCharsets.UTF_8), MultiTransferJob.class));
        } catch (JsonParseException exception) {
            log.warn("Stored Job could not be parsed to Job object.");
            return Optional.empty();
        }
    }

    private byte[] toBlob(final MultiTransferJob job) {
        final String jobString = this.json.asString(job);
        return jobString.getBytes(StandardCharsets.UTF_8);
    }

    private String toBlobId(final String jobId) {
        return JOB_PREFIX + jobId;
    }

    private boolean isLastStateSameAsCurrentState(final String jobId, final JobState state) {
        final Optional<MultiTransferJob> optJob = get(jobId);
        return optJob.isPresent() && optJob.get().getJob().getState().equals(state);
    }

}
