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
import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.enums.JobState;
import net.catenax.irs.connector.job.JobInitiateResponse;

/**
 * Interface for JobHandler
 */
public interface IJobHandler {

    JobInitiateResponse createJob(@NonNull GlobalAssetIdentification globalAssetId);

    void cancelJob(@NonNull JobHandle jobHandle);

    JobState interruptJob(@NonNull JobHandle jobHandle);

    Jobs getResult(@NonNull JobHandle jobHandle);

}
