package net.catenax.irs.services;

import static net.catenax.irs.util.CommonConstant.EMPTY_STRING;

import net.catenax.irs.connector.job.JobInitiateResponse;
import net.catenax.irs.connector.job.ResponseStatus;

public class ResponseAnswer implements IResponseAnswer {

    public void onSuccess(final String jobId) {
        JobInitiateResponse.builder()
                           .jobId(jobId)
                           .status(ResponseStatus.OK)
                           .error(EMPTY_STRING)
                           .build();
    }

    @Override
    public void onSuccess(final JobInitiateResponse response) {

    }

    @Override
    public void onFail(final JobInitiateResponse response) {

    }
}
