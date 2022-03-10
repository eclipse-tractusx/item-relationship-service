//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.configuration;


import lombok.Builder;
import lombok.Value;

/**
 * Configuration data for the consumer connector.
 */
@Value
@Builder
public class ConsumerConfiguration {

    /**
     * Storage account name.
     */
    private final String storageAccountName;
}
