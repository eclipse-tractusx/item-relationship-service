///
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import net.catenax.irs.aaswrapper.registry.domain.AasShellTombstone;
import net.catenax.irs.aaswrapper.submodel.domain.ItemRelationshipAspectTombstone;
import net.catenax.irs.dto.NodeType;
import net.catenax.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;

class DeserializationTest {

    @Test
    void shouldDeserializeAspectTombstoneSuccessfully() {
        final JsonUtil jsonUtil = new JsonUtil();
        final ItemRelationshipAspectTombstone itemRelationshipAspectTombstone = jsonUtil.fromString(
                "{\"catenaXId\":\"id\",\"nodeType\":\"TOMBSTONE\",\"processingError\":{\"exception\":"
                        + "\"CustomException\",\"errorDetail\":\"detail\",\"lastAttempt\":"
                        + "\"2022-04-20T14:37:56.177195400Z\"},\"endpointURL\":\"url\"}",
                ItemRelationshipAspectTombstone.class);

        assertThat(itemRelationshipAspectTombstone).isNotNull();
        assertThat(itemRelationshipAspectTombstone.getCatenaXId()).isEqualTo("id");
        assertThat(itemRelationshipAspectTombstone.getEndpointURL()).isEqualTo("url");
        assertThat(itemRelationshipAspectTombstone.getProcessingError().getLastAttempt()).isEqualTo(
                Instant.parse("2022-04-20T14:37:56.177195400Z"));
    }

    @Test
    void shouldDeserializeShellTombstoneSuccessfully() {
        final JsonUtil jsonUtil = new JsonUtil();
        final AasShellTombstone test = jsonUtil.fromString(
                "{\"idShort\":\"test\",\"identification\":\"test\",\"nodeType\":\"TOMBSTONE\",\"processingError\":"
                        + "{\"exception\":\"CustomException\",\"errorDetail\":\"detail\",\"lastAttempt\":"
                        + "\"2022-04-21T14:20:42.179438900Z\"}}", AasShellTombstone.class);

        assertThat(test).isNotNull();
        assertThat(test.getNodeType()).isEqualTo(NodeType.TOMBSTONE);
        assertThat(test.getIdentification()).isEqualTo("test");
        assertThat(test.getIdShort()).isEqualTo("test");
        assertThat(test.getProcessingError()).isNotNull();
    }
}