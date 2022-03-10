//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.extension;


import io.micrometer.jmx.JmxMeterRegistry;
import jakarta.validation.Validation;
import net.catenax.prs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.prs.connector.consumer.configuration.ConsumerConfiguration;
import net.catenax.prs.connector.consumer.controller.ConsumerApiController;
import net.catenax.prs.connector.consumer.middleware.RequestMiddleware;
import net.catenax.prs.connector.consumer.service.ConsumerService;
import net.catenax.prs.connector.consumer.service.DataRequestFactory;
import net.catenax.prs.connector.consumer.service.PartsTreeRecursiveJobHandler;
import net.catenax.prs.connector.consumer.service.PartsTreeRecursiveLogic;
import net.catenax.prs.connector.consumer.service.PartsTreesAssembler;
import net.catenax.prs.connector.job.InMemoryJobStore;
import net.catenax.prs.connector.job.JobOrchestrator;
import net.catenax.prs.connector.metrics.MeterRegistryFactory;
import net.catenax.prs.connector.http.HttpClientFactory;
import net.catenax.prs.connector.util.JsonUtil;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.common.azure.BlobStoreApi;
import org.eclipse.dataspaceconnector.spi.protocol.web.WebService;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessManager;
import org.eclipse.dataspaceconnector.spi.transfer.TransferProcessObservable;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.util.Set;

import static java.util.Optional.ofNullable;
import static net.catenax.prs.connector.consumer.extension.ExtensionUtils.fatal;

/**
 * Extension providing extra consumer endpoints.
 */
@ExcludeFromCodeCoverageGeneratedReport
public class ApiEndpointExtension implements ServiceExtension {

    /**
     * The configuration property used to reference the storage account name
     * for connector data exchange.
     */
    public static final String EDC_STORAGE_ACCOUNT_NAME = "edc.storage.account.name";

    @Override
    public Set<String> requires() {
        return Set.of(
                "edc:webservice",
                "dataspaceconnector:transferprocessstore",
                "dataspaceconnector:blobstoreapi"
        );
    }

    @Override
    public void initialize(final ServiceExtensionContext context) {
        /*
          Register JmxMeterRegistry in global context for re-use.
         */
        context.registerService(JmxMeterRegistry.class, new MeterRegistryFactory().jmxMeterRegistry());
        /*
            Overrides edc core OkHttpClient to expose micrometer metrics.
         */
        context.registerService(OkHttpClient.class, new HttpClientFactory().okHttpClient(context.getService(JmxMeterRegistry.class)));

        final var storageAccountName = ofNullable(context.getSetting(EDC_STORAGE_ACCOUNT_NAME, null))
                .orElseThrow(() -> fatal(context, "Missing mandatory property " + EDC_STORAGE_ACCOUNT_NAME, null));

        final var monitor = context.getMonitor();
        final var jsonUtil = new JsonUtil(monitor);

        final var validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        final var middleware = new RequestMiddleware(monitor, validator);
        final var webService = context.getService(WebService.class);
        final var processManager = context.getService(TransferProcessManager.class);
        final var transferProcessObservable = context.getService(TransferProcessObservable.class);

        final var blobStoreApi = context.getService(BlobStoreApi.class);
        final var jobStore = new InMemoryJobStore(monitor);
        final var configuration = ConsumerConfiguration.builder().storageAccountName(storageAccountName).build();
        final var registryClient = StubRegistryClientFactory.getRegistryClient(context, jsonUtil);
        final var assembler = new PartsTreesAssembler(monitor);
        final var dataRequestGenerator = new DataRequestFactory(monitor, configuration, jsonUtil, registryClient);
        final var logic = new PartsTreeRecursiveLogic(monitor, blobStoreApi, jsonUtil, dataRequestGenerator, assembler);
        final var jobHandler = new PartsTreeRecursiveJobHandler(monitor, configuration, jsonUtil, logic);
        final var jobOrchestrator = new JobOrchestrator(processManager, jobStore, jobHandler, transferProcessObservable, monitor);

        final var service = new ConsumerService(monitor, jsonUtil, jobStore, jobOrchestrator, blobStoreApi, configuration);

        webService.registerController(new ConsumerApiController(monitor, service, middleware));
    }
}
