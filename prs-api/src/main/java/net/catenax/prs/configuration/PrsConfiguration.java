//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.configuration;

import lombok.Data;
import net.catenax.prs.entities.PartAttributeEntity;
import net.catenax.prs.entities.PartIdEntityPart;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.net.URL;

/**
 * PRS configuration settings. Automatically populated by Spring from application.yml
 * and other configuration sources.
 */
@Component
@ConfigurationProperties(prefix = "prs")
@Data
public class PrsConfiguration {
    /**
     * The name of the {@link PartAttributeEntity} containing the part type name in the value.
     */
    public static final String PART_TYPE_NAME_ATTRIBUTE = "partTypeName";

    /**
     * The value of the {@link PartAttributeEntity} with the name {@link #PART_TYPE_NAME_ATTRIBUTE},
     * which indicates that the {@link PartIdEntityPart#getObjectIDManufacturer()} value is a VIN
     * (Vehicle Identification Number). This is used to query the part tree by VIN.
     */
    public static final String VEHICLE_ATTRIBUTE_VALUE = "vehicle";

    /**
     * The Base URL at which the API is externally accessible. Used in generated OpenAPI definition.
     */
    private URL apiUrl;

    /**
     * The maximum depth at which parts tree are recursively retrieved.
     */
    private int partsTreeMaxDepth = Integer.MAX_VALUE;

    /**
     * Kafka topic for prs data update events.
     */
    private String kafkaTopic;

    /**
     * Nested configuration settings for retrying incoming event processing.
     */
    @NestedConfigurationProperty
    private EventProcessingRetryConfiguration processingRetry = new EventProcessingRetryConfiguration();
}
