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


import net.catenax.prs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.prs.connector.consumer.configuration.PartitionDeploymentsConfig;
import net.catenax.prs.connector.consumer.configuration.PartitionsConfig;
import net.catenax.prs.connector.consumer.registry.StubRegistryClient;
import net.catenax.prs.connector.util.JsonUtil;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.eclipse.dataspaceconnector.spi.system.ServiceExtensionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Optional.ofNullable;
import static net.catenax.prs.connector.consumer.extension.ExtensionUtils.fatal;

/**
 * Factory for {@link StubRegistryClient}.
 */
@ExcludeFromCodeCoverageGeneratedReport
public final class StubRegistryClientFactory {

    /***
     * The configuration property used to reference
     * the {@literal cd/dataspace-partitions.json} configuration file.
     * */
    private static final String DATASPACE_PARTITIONS = "prs.dataspace.partitions";

    /**
     * The configuration property used to reference
     * the {@literal dataspace-deployments.json} file
     * generated from Terraform outputs in CD pipeline.
     */
    private static final String DATASPACE_PARTITION_DEPLOYMENTS = "prs.dataspace.partition.deployments";

    private StubRegistryClientFactory() {
    }

    static /* package */ StubRegistryClient getRegistryClient(final ServiceExtensionContext context, final JsonUtil jsonUtil) {
        final var partitionsConfig = readJson(
                context,
                jsonUtil,
                DATASPACE_PARTITIONS,
                "../../cd/dataspace-partitions.json",
                PartitionsConfig.class,
                "");
        final var partitionDeploymentsConfig = readJson(
                context,
                jsonUtil,
                DATASPACE_PARTITION_DEPLOYMENTS,
                "../dataspace-deployments.json",
                PartitionDeploymentsConfig.class,
                "For development, see README.md for instructions on downloading the file.");
        return new StubRegistryClient(partitionsConfig, partitionDeploymentsConfig);
    }

    private static <T> T readJson(
            final ServiceExtensionContext context,
            final JsonUtil jsonUtil,
            final String property,
            final String defaultValue,
            final Class<T> type,
            final String message) {
        final var path = ofNullable(context.getSetting(property, defaultValue))
                .orElseThrow(() -> fatal(context, "Missing property " + property, null));
        try {
            final var json = Files.readString(Paths.get(path));
            return jsonUtil.fromString(json, type);
        } catch (IOException | EdcException e) {
            throw fatal(context, "Couldn't parse " + path + ". " + message, e);
        }
    }
}
