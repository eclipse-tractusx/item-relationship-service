//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.configuration;

import java.util.concurrent.Executors;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.tractusx.irs.aaswrapper.job.AASHandler;
import org.eclipse.tractusx.irs.aaswrapper.job.AASRecursiveJobHandler;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcessManager;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemDataRequest;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemTreesAssembler;
import org.eclipse.tractusx.irs.aaswrapper.job.TreeRecursiveLogic;
import org.eclipse.tractusx.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.irs.bpdm.BpdmFacade;
import org.eclipse.tractusx.irs.connector.job.JobOrchestrator;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.MeterRegistryService;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration for job-related beans.
 */
@Configuration
public class JobConfiguration {

    @Bean
    public JobOrchestrator<ItemDataRequest, AASTransferProcess> jobOrchestrator(
            final DigitalTwinRegistryFacade registryFacade, final SubmodelFacade submodelFacade,
            final SemanticsHubFacade semanticsHubFacade, final BpdmFacade bpdmFacade,
            final JsonValidatorService jsonValidatorService, final BlobPersistence blobStore,
            final JobStore jobStore, final MeterRegistryService meterService) {

        final var aasHandler = new AASHandler(registryFacade, submodelFacade, semanticsHubFacade, bpdmFacade, jsonValidatorService, jsonUtil());
        final var manager = new AASTransferProcessManager(aasHandler, Executors.newCachedThreadPool(), blobStore);
        final var logic = new TreeRecursiveLogic(blobStore, new JsonUtil(), new ItemTreesAssembler());
        final var handler = new AASRecursiveJobHandler(logic);

        return new JobOrchestrator<>(manager, jobStore, handler, meterService);
    }

    @Profile("!test")
    @Bean
    public BlobPersistence blobStore(final BlobstoreConfiguration config) throws BlobPersistenceException {
        return new MinioBlobPersistence(config.getEndpoint(), config.getAccessKey(), config.getSecretKey(),
                config.getBucketName());
    }

    @Bean
    public JsonUtil jsonUtil() {
        return new JsonUtil();
    }

    @Bean
    public TimedAspect timedAspect(final MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
