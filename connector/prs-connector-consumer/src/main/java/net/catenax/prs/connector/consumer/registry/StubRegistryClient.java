//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.registry;

import net.catenax.prs.client.model.PartId;
import net.catenax.prs.connector.consumer.configuration.PartitionDeploymentsConfig;
import net.catenax.prs.connector.consumer.configuration.PartitionsConfig;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A stub registry client.
 */
public class StubRegistryClient {
    /**
     * The key in {@link PartitionDeploymentsConfig} for the Provider Connector URL.
     */
    public static final String CONNECTOR_URL_ATTRIBUTE_KEY = "connector_url";

    /**
     * Configuration for mapping from OneIDs to data space URLs.
     */
    private final Map<String, String> oneIdToUrlMappings;

    /**
     * Creates a new instance of {@link StubRegistryClient}.
     *
     * @param config      partition configuration.
     * @param deployments partition deployments configuration.
     */
    public StubRegistryClient(final PartitionsConfig config, final PartitionDeploymentsConfig deployments) {
        if (config.getPartitions().isEmpty()) {
            throw new EdcConfigurationException("No partitions defined");
        }
        this.oneIdToUrlMappings = config.getPartitions().stream()
                .flatMap(
                        p -> p.getOneIDs().stream()
                                .map(o -> new ItemPair<>(o, getApiUrl(deployments, p))))
                .collect(Collectors.toMap(ItemPair::getKey, ItemPair::getValue));
    }

    private static <K, V> Optional<V> getAsOptional(final Map<K, V> map, final K key) {
        return Optional.ofNullable(map.get(key));
    }

    private String getApiUrl(final PartitionDeploymentsConfig deployments, final PartitionsConfig.PartitionConfig partitionConfig) {
        final var attributeCollection = getAsOptional(deployments, partitionConfig.getKey())
                .orElseThrow(() -> new EdcConfigurationException("Missing entry in partition attributes file: " + partitionConfig.getKey()));
        return getAsOptional(attributeCollection, CONNECTOR_URL_ATTRIBUTE_KEY)
                .orElseThrow(() -> new EdcConfigurationException("Missing " + CONNECTOR_URL_ATTRIBUTE_KEY + " key in partition attributes file for " + partitionConfig.getKey()))
                .getValue();
    }

    /**
     * Retrieve the URL for the part ID.
     *
     * @param request the request containing part identifier to search.
     * @return Guaranteed to never return {@literal null} in the optional value.
     */
    public Optional<String> getUrl(final PartId request) {
        return getAsOptional(oneIdToUrlMappings, request.getOneIDManufacturer());
    }

    /**
     * Helper class to manage pairs of items.
     *
     * @param <K> type of the first item in the pair.
     * @param <V> type of the second item in the pair.
     */
    private static final class ItemPair<K, V> extends AbstractMap.SimpleEntry<K, V> {
        private ItemPair(final K key, final V value) {
            super(key, value);
        }
    }
}
