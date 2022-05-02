package net.catenax.irs.services;

import net.catenax.irs.connector.job.JobInitiateResponse;

public interface IResponseAnswer {
    void onSuccess(JobInitiateResponse response);

    void onFail(JobInitiateResponse response);
}
