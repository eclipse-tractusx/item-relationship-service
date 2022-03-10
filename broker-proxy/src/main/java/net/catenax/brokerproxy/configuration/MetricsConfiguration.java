//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerproxy.configuration;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Beans for capturing application custom metrics.
 */
@Configuration
@RequiredArgsConstructor
public class MetricsConfiguration {
    /**
     * {@link Qualifier} value to inject {@link #uploadedBomSize()} meter.
     */
    public static final String UPLOADED_BOM_SIZE = "uploaded_bom_size";

    /**
     * Registry for publishing custom metrics.
     */
    private final MeterRegistry registry;

    /**
     * A custom meter recording the upload BOM size, i.e.
     * number of items in uploaded {@link PartRelationshipsUpdateRequest}.
     *
     * @return Guaranteed to never return {@literal null}.
     */
    @Bean
    @Qualifier(UPLOADED_BOM_SIZE)
    public DistributionSummary uploadedBomSize() {
        return DistributionSummary
                .builder("uploaded_bom_size")
                .description("Number of items in uploaded PartRelationshipUpdateList")
                .register(registry);
    }
}
