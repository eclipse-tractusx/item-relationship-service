//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.NonNull;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;

/**
 * IrsPartTreeQueryService interface
 */
public interface IIrsItemGraphQueryService {

    JobHandle registerItemJob(@NonNull RegisterJob request);

    Jobs jobLifecycle(@NonNull String jobId);

    Optional<List<Job>> getJobsByJobState(@NonNull String processingState);

    Job cancelJobById(@NonNull UUID jobId);

    Jobs getJobForJobId(UUID jobId);
}
