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

import static io.github.resilience4j.core.IntervalFunction.ofExponentialBackoff;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.catenax.irs.dto.AssemblyPartRelationshipDTO;
import net.catenax.irs.dto.ChildDataDTO;
import net.catenax.irs.dto.JobParameter;
import net.catenax.irs.dto.QuantityDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Public API Facade for submodel domain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmodelFacade {

    private final SubmodelClient submodelClient;

    /**
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @param jobData                 relevant job data values
     * @return The Aspect Model for the given submodel
     */
    public AssemblyPartRelationshipDTO getSubmodel(final String submodelEndpointAddress, final JobParameter jobData) {
        final AssemblyPartRelationship submodel = this.retryGetSubmodel(submodelEndpointAddress);

        log.info("Submodel: {}, childParts {}", submodel.getCatenaXId(), submodel.getChildParts());

        final Set<ChildData> submodelParts = thereAreChildParts(submodel) ? new HashSet<>(submodel.getChildParts()) : Collections.emptySet();

        final String lifecycleContext = jobData.getBomLifecycle();
        if (shouldFilterByLifecycleContext(lifecycleContext)) {
            filterSubmodelPartsByLifecycleContext(submodelParts, lifecycleContext);
        }

        return buildAssemblyPartRelationshipResponse(submodelParts, submodel.getCatenaXId());
    }

    private boolean thereAreChildParts(final AssemblyPartRelationship submodel) {
        return submodel.getChildParts() != null;
    }

    @SuppressWarnings("PMD.NullAssignment")
    private AssemblyPartRelationshipDTO buildAssemblyPartRelationshipResponse(final Set<ChildData> submodelParts,
            final String catenaXId) {
        final Set<ChildDataDTO> childParts = new HashSet<>();
        submodelParts.forEach(childData -> childParts.add(ChildDataDTO.builder()
                                                  .childCatenaXId(childData.getChildCatenaXId())
                                                  .lifecycleContext(childData.getLifecycleContext().getValue())
                                                  .assembledOn(childData.getAssembledOn())
                                                  .lastModifiedOn(childData.getLastModifiedOn())
                                                  .quantity(QuantityDTO.builder()
                                                          .quantityNumber(thereIsQuantity(childData) ? childData.getQuantity().getQuantityNumber() : null)
                                                          .measurementUnit(QuantityDTO.MeasurementUnitDTO.builder()
                                                                        .datatypeURI(thereIsMeasurementUnit(childData)
                                                                                ? childData.getQuantity().getMeasurementUnit().getDatatypeURI() : null)
                                                                        .lexicalValue(thereIsMeasurementUnit(childData)
                                                                                ? childData.getQuantity().getMeasurementUnit().getLexicalValue() : null)
                                                                        .build())
                                                            .build())
                                                  .build()));

        return AssemblyPartRelationshipDTO.builder().catenaXId(catenaXId).childParts(childParts).build();
    }

    private boolean thereIsMeasurementUnit(final ChildData childData) {
        return childData.getQuantity() != null && childData.getQuantity().getMeasurementUnit() != null;
    }

    private boolean thereIsQuantity(final ChildData childData) {
        return childData.getQuantity() != null;
    }

    private void filterSubmodelPartsByLifecycleContext(final Set<ChildData> submodelParts,
            final String lifecycleContext) {
        submodelParts.removeIf(isNotLifecycleContext(lifecycleContext));
    }

    private boolean shouldFilterByLifecycleContext(final String lifecycleContext) {
        return StringUtils.isNotBlank(lifecycleContext);
    }

    private Predicate<ChildData> isNotLifecycleContext(final String lifecycleContext) {
        return childData -> !childData.getLifecycleContext().getValue().equals(lifecycleContext);
    }

    /**
     * Apply the customized retry component on retrieving the given submodel
     * @param submodelEndpointAddress The URL to the submodel endpoint
     * @return The Aspect Model for the given submodel
     */
    private AssemblyPartRelationship retryGetSubmodel(final String submodelEndpointAddress) {
        final Function<String, AssemblyPartRelationship> decorated =
                Retry.decorateFunction(RetryConfiguration.retryer(),
                        (String s) -> this.submodelClient.getSubmodel(submodelEndpointAddress, AssemblyPartRelationship.class));

        return decorated.apply(submodelEndpointAddress);
    }

    /**
     * Configure the resilience4j retry procedure
     */
    public static class RetryConfiguration {
        private static final double MULTIPLIER = 2.0D;
        private static final long INITIAL_INTERVAL = 10L;
        private static final int MAX_RETRIES = 3;

        /**
         * Customize the resilience4j retry method
         * @return configured retry mechanism
         */
        public static Retry retryer() {
            final RetryConfig retryConfig = RetryConfig.custom()
                                                       .maxAttempts(MAX_RETRIES)
                                                       .intervalFunction(ofExponentialBackoff(INITIAL_INTERVAL, MULTIPLIER))
                                                       .retryExceptions(IOException.class, TimeoutException.class, HttpServerErrorException.class)
                                                       .failAfterMaxAttempts(true)
                                                       .build();

            final RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);

            return retryRegistry.retry("submodelRetryer");
        }
    }

}
