//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.services;

import java.util.List;
import java.util.UUID;

import lombok.NonNull;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.JobStatusResult;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.RegisterJob;
import org.eclipse.tractusx.irs.component.enums.JobState;

/**
 * IIrsItemGraphQueryService interface
 */
public interface IIrsItemGraphQueryService {

    JobHandle registerItemJob(@NonNull RegisterJob request);

    List<JobStatusResult> getJobsByJobState(@NonNull List<JobState> jobStates);

    Job cancelJobById(@NonNull UUID jobId);

    Jobs getJobForJobId(UUID jobId, boolean includePartialResults);
}
