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
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/*** JSON deserialization classes for {@literal cd/dataspace-partitions.json} configuration file. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartitionsConfig {
    /**
     * The dataspace partitions.
     */
    private List<PartitionConfig> partitions = List.of();

    /**
     * Configuration for one dataspace partition.
     */
    @Data
    public static class PartitionConfig {
        /**
         * The partition code.
         */
        private String key;

        /**
         * The OneIDs for parts stored in the partition.
         */
        @JsonProperty("OneIDs")
        private List<String> oneIDs = List.of();
    }
}
