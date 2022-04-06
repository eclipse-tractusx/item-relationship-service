//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.jobs;

import java.util.concurrent.Executors;
import java.util.stream.Stream;

import net.catenax.irs.aaswrapper.AASWrapperClient;
import net.catenax.irs.connector.job.DataRequest;
import net.catenax.irs.connector.job.InMemoryJobStore;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.connector.job.MultiTransferJob;
import net.catenax.irs.connector.job.RecursiveJobHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobConfiguration {

    @Bean
    public JobOrchestrator<ItemDataRequest, AASTransferProcess> jobOrchestrator(final AASWrapperClient aasClient) {

        AASTransferProcessManager manager = new AASTransferProcessManager(aasClient, Executors.newCachedThreadPool());

        JobStore jobStore = new InMemoryJobStore();

        RecursiveJobHandler<ItemDataRequest, AASTransferProcess> handler = new RecursiveJobHandler<>() {
            @Override
            public Stream<ItemDataRequest> initiate(final MultiTransferJob job) {
                final String partId = job.getJobData().get("partId");
                var dr = new ItemDataRequest(partId);
                return Stream.of(dr);
            }

            @Override
            public Stream<ItemDataRequest> recurse(final MultiTransferJob job, final AASTransferProcess transferProcess) {
                return Stream.empty();
            }

            @Override
            public void complete(final MultiTransferJob job) {

            }
        };

        return new JobOrchestrator<>(manager, jobStore, handler);
    }
}
