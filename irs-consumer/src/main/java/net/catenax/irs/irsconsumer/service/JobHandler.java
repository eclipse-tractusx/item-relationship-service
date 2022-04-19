//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.irsconsumer.service;

import lombok.NonNull;
import net.catenax.irs.component.GlobalAssetIdentification;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobOrchestrator;

/**
 * Service use to create Job, manipulate job state and get job result
 */
public class JobHandler implements IJobHandler {

    JobOrchestrator orchestrator;

    @Override
    public Job createJob(@NonNull final GlobalAssetIdentification globalAssetId) {
        return null;
    }

    @Override
    public void cancelJob(@NonNull final String JobId) {

    }

    @Override
    public JobState interruptJob(@NonNull final String JobId) {
        return null;
    }

    @Override
    public Jobs getResult(@NonNull final String JobId) {
        return null;
    }
}
