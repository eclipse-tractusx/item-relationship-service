//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.configuration;

import java.util.concurrent.Executors;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import net.catenax.irs.aaswrapper.job.AASHandler;
import net.catenax.irs.aaswrapper.job.AASRecursiveJobHandler;
import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.AASTransferProcessManager;
import net.catenax.irs.aaswrapper.job.BpdmProcessor;
import net.catenax.irs.aaswrapper.job.DigitalTwinProcessor;
import net.catenax.irs.aaswrapper.job.ItemDataRequest;
import net.catenax.irs.aaswrapper.job.ItemTreesAssembler;
import net.catenax.irs.aaswrapper.job.RelationshipProcessor;
import net.catenax.irs.aaswrapper.job.SubmodelProcessor;
import net.catenax.irs.aaswrapper.job.TreeRecursiveLogic;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.bpdm.BpdmFacade;
import net.catenax.irs.connector.job.JobOrchestrator;
import net.catenax.irs.connector.job.JobStore;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;
import net.catenax.irs.persistence.MinioBlobPersistence;
import net.catenax.irs.semanticshub.SemanticsHubFacade;
import net.catenax.irs.services.MeterRegistryService;
import net.catenax.irs.services.validation.JsonValidatorService;
import net.catenax.irs.util.JsonUtil;
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

    @Bean
    public DigitalTwinProcessor digitalTwinProcessor(
            final RelationshipProcessor relationshipProcessor, final DigitalTwinRegistryFacade digitalTwinRegistryFacade) {
        return new DigitalTwinProcessor(relationshipProcessor, digitalTwinRegistryFacade);
    }

    @Bean
    public RelationshipProcessor relationshipProcessor(
            final SubmodelProcessor submodelProcessor, final SubmodelFacade submodelFacade) {
        return new RelationshipProcessor(submodelProcessor, submodelFacade);
    }

    @Bean
    public SubmodelProcessor submodelProcessor(
            final BpdmProcessor bpdmProcessor, final SubmodelFacade submodelFacade,
            final SemanticsHubFacade semanticsHubFacade, final JsonValidatorService jsonValidatorService) {
        return new SubmodelProcessor(bpdmProcessor, submodelFacade, semanticsHubFacade, jsonValidatorService, jsonUtil());
    }

    @Bean
    public BpdmProcessor bpdmProcessor(
            final BpdmFacade bpdmFacade) {
        return new BpdmProcessor(bpdmFacade);
    }

}
