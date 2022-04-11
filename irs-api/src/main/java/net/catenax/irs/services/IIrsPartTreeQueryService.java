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

import lombok.NonNull;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.requests.IrsPartsTreeRequest;

/**
 * IrsPartTreeQueryService interface
 */
public interface IIrsPartTreeQueryService {

    JobHandle registerItemJob(@NonNull IrsPartsTreeRequest request);

    Jobs jobLifecycle(@NonNull String jobId);

    Optional<List<Job>> getJobsByProcessingState(@NonNull String processingState);

    Optional<Job> cancelJobById(@NonNull String jobId);

}
