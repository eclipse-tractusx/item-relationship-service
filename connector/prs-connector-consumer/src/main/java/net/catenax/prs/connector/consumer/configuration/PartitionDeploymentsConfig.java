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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashMap;

/**
 * JSON deserialization classes for {@literal dataspace-deployments.json} file
 * generated from Terraform outputs in CD pipeline.
 */
public class PartitionDeploymentsConfig extends HashMap<String, PartitionDeploymentsConfig.PartitionAttributeCollection> {

    /**
     * Attributes for a given partition code, indexed by attribute name.
     */
    public static class PartitionAttributeCollection extends HashMap<String, PartitionAttribute> {
    }

    /**
     * One particular attribute entry.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PartitionAttribute {
        /**
         * Attribute value.
         */
        private String value;
    }
}
