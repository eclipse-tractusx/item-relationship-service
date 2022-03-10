//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.provider;

import io.micrometer.jmx.JmxMeterRegistry;
import net.catenax.prs.client.ApiClient;
import net.catenax.prs.client.api.PartsRelationshipServiceApi;
import net.catenax.prs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.prs.connector.http.HttpClientFactory;
import net.catenax.prs.connector.metrics.MeterRegistryFactory;
import net.catenax.prs.connector.util.JsonUtil;
import okhttp3.OkHttpClient;
import org.eclipse.dataspaceconnector.policy.model.Action;
import org.eclipse.dataspaceconnector.policy.model.AtomicConstraint;
import org.eclipse.dataspaceconnector.policy.model.LiteralExpression;
import org.eclipse.dataspaceconnector.policy.model.Permission;
import org.eclipse.dataspaceconnector.policy.model.Policy;
import org.eclipse.dataspaceconnector.spi.metadata.MetadataStore;
import org.eclipse.dataspaceconnector.spi.policy.PolicyRegistry;
import org.eclipse.dataspaceconnector.spi.security.Vault;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtension;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;
import org.eclipse.dataspaceconnector.spi.transfer.flow.DataFlowManager;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;

import java.util.Set;

import static net.catenax.prs.connector.constants.PrsConnectorConstants.PRS_REQUEST_ASSET_ID;
import static net.catenax.prs.connector.constants.PrsConnectorConstants.PRS_REQUEST_POLICY_ID;
import static org.eclipse.dataspaceconnector.policy.model.Operator.IN;

/**
 * Extension to call PRS API and save the results.
 */
@SuppressWarnings("PMD.GuardLogStatement") // Monitor doesn't offer guard statements
@ExcludeFromCodeCoverageGeneratedReport
public class PartsRelationshipServiceApiExtension implements ServiceExtension {

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> requires() {
        return Set.of("edc:webservice", PolicyRegistry.FEATURE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final ServiceExtensionContext context) {
        /*
          Register JmxMeterRegistry in global context for re-use.
         */
        context.registerService(JmxMeterRegistry.class, new MeterRegistryFactory().jmxMeterRegistry());

        final var httpClient = new HttpClientFactory().okHttpClient(context.getService(JmxMeterRegistry.class));
        /*
            Overrides edc core OkHttpClient to expose micrometer metrics.
         */
        context.registerService(OkHttpClient.class, httpClient);

        final var monitor = context.getMonitor();
        final var prsApiUrl = context.getSetting("PRS_API_URL", "http://localhost:8080");
        final var prsClient = new PartsRelationshipServiceApi(new ApiClient(httpClient));
        prsClient.getApiClient().setBasePath(prsApiUrl);

        final var jsonUtil = new JsonUtil(monitor);
        final var vault = context.getService(Vault.class);
        final var blobClientFactory = new BlobClientFactory();
        final var blobStorageClient = new BlobStorageClient(monitor, jsonUtil, vault, blobClientFactory);

        final var dataFlowMgr = context.getService(DataFlowManager.class);
        final var flowController = new PartsRelationshipServiceApiToFileFlowController(monitor, prsClient, blobStorageClient);
        dataFlowMgr.register(flowController);

        registerDataEntries(context);
        savePolicies(context);
        monitor.info(getClass().getName() + " initialized!");
    }

    private void savePolicies(final ServiceExtensionContext context) {
        final PolicyRegistry policyRegistry = context.getService(PolicyRegistry.class);

        // Hard-coded policy allowing use in Europe only, for demonstration purposes.
        final LiteralExpression spatialExpression = new LiteralExpression("ids:absoluteSpatialPosition");
        final var euConstraint = AtomicConstraint.Builder.newInstance().leftExpression(spatialExpression).operator(IN).rightExpression(new LiteralExpression("eu")).build();
        final var euUsePermission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("idsc:USE").build()).constraint(euConstraint).build();
        final var euPolicy = Policy.Builder.newInstance().id(PRS_REQUEST_POLICY_ID).permission(euUsePermission).build();
        policyRegistry.registerPolicy(euPolicy);
    }

    private void registerDataEntries(final ServiceExtensionContext context) {
        final var metadataStore = context.getService(MetadataStore.class);
        final DataEntry entry1 = DataEntry.Builder.newInstance().id(PRS_REQUEST_ASSET_ID).policyId(PRS_REQUEST_POLICY_ID).build();
        metadataStore.save(entry1);
    }
}
