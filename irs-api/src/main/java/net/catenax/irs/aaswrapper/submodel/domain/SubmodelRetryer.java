//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.submodel.domain;

import java.util.Optional;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for retrying the retirement of submodel json's
 * from the remote aas wrapper service
 */
@Slf4j
@Service
@Getter
public class SubmodelRetryer {
    private final SubmodelClient client;
    private final RetryRegistry registry;

    public SubmodelRetryer(final SubmodelClient client, final RetryRegistry registry) {
        this.client = client;
        this.registry = registry;
    }

    /**
     * Retry the call getting a submodel from the remote
     * aas wrapper service
     *
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @param submodelClass           The Aspect Model for the given submodel
     *                                like AssemblyPartRelationship
     * @param <T>                     the generic Aspect Model
     * @return the requested submodel
     */
    @Retry(name = "submodelRetryer")
    public <T> T retrySubmodel(final String submodelEndpointAddress, final Class<T> submodelClass) {
        return this.client.getSubmodel(submodelEndpointAddress, submodelClass);
    }

    /**
     * Get the configured resilience4j Retry
     *
     * @return the object RetryConfig
     */
    public Optional<RetryConfig> getRetryConfig() {
        final RetryRegistry registry = this.getRegistry();

        if (registry != null) {
            return registry.getConfiguration("default");
        }
        return Optional.empty();
    }
}
