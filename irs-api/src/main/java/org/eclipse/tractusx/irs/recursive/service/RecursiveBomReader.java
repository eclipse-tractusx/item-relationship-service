/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.recursive.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Quantity;
import org.eclipse.tractusx.irs.component.Relationship;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.RelationshipSubmodel;
import org.eclipse.tractusx.irs.edc.client.relationships.RelationshipAspect;
import org.eclipse.tractusx.irs.recursive.model.ItemUnitEnumeration;
import org.eclipse.tractusx.irs.recursive.model.RecursiveBomChild;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;
import org.eclipse.tractusx.irs.util.JsonUtil;

/** Reads BOM relationships independently so malformed entries do not discard valid siblings. */
@Slf4j
final class RecursiveBomReader {

    private static final String CHILD_ITEMS = "childItems";
    private static final String CATENA_X_ID = "catenaXId";
    private static final String QUANTITY = "quantity";

    private final JsonUtil jsonUtil;
    private final Class<? extends RelationshipSubmodel> submodelType;
    private final UUID jobId;
    private final String globalAssetId;

    /* package */ RecursiveBomReader(final JsonUtil jsonUtil,
            final BomLifecycle lifecycle, final UUID jobId, final String globalAssetId) {
        this.jsonUtil = jsonUtil;
        this.submodelType = RelationshipAspect.from(lifecycle, Direction.DOWNWARD).getSubmodelClazz();
        this.jobId = jobId;
        this.globalAssetId = globalAssetId;
    }

    /* package */ List<RecursiveBomChild> read(final String payload) {
        final JsonNode bom = readRoot(payload);
        final JsonNode childItems = bom.get(CHILD_ITEMS);
        final List<RecursiveBomChild> children = new ArrayList<>();
        for (int index = 0; index < childItems.size(); index++) {
            children.add(readChild(childItems.get(index), index));
        }
        return children;
    }

    private JsonNode readRoot(final String payload) {
        if (payload == null) {
            throw invalidBom();
        }
        final JsonNode bom;
        try {
            bom = jsonUtil.fromString(payload, JsonNode.class);
        } catch (final JsonParseException exception) {
            throw invalidBom();
        }
        if (bom == null || !bom.path(CHILD_ITEMS).isArray()) {
            throw invalidBom();
        }
        return bom;
    }

    private static RecursiveExternalCallException invalidBom() {
        return new RecursiveExternalCallException(RecursiveTombstoneReason.BOM_SUBMODEL_INVALID.name(),
                RecursiveTombstones.BOM_SUBMODEL_INVALID_DETAIL, null);
    }

    private RecursiveBomChild readChild(final JsonNode child, final int index) {
        final JsonNode partner = child.path("businessPartner");
        if (!partner.isTextual() || !RecursivePatternStore.BPNL_PATTERN.matcher(partner.textValue()).matches()) {
            logRelationshipFailure(index, "No valid partner BPNL");
            return new RecursiveBomChild(null, null, null);
        }
        final ObjectNode relationshipData = child.deepCopy();
        // Exclude unused fields so their formats cannot block recursive traversal.
        relationshipData.retain(CATENA_X_ID, "businessPartner", QUANTITY);
        if (!relationshipData.path(CATENA_X_ID).isTextual()) {
            relationshipData.remove(CATENA_X_ID);
        }
        final ObjectNode singleChildBom = JsonNodeFactory.instance.objectNode();
        singleChildBom.put(CATENA_X_ID, globalAssetId);
        singleChildBom.putArray(CHILD_ITEMS).add(relationshipData);
        final Relationship relationship = readRelationship(singleChildBom, relationshipData, index);
        return new RecursiveBomChild(relationship.getLinkedItem().getChildCatenaXId().getGlobalAssetId(),
                partner.textValue(), quantityOf(relationship));
    }

    private void logRelationshipFailure(final int index, final String cause) {
        log.warn("BOM relationship issue: jobId={}, globalAssetId={}, entryIndex={}, cause={}",
                RecursiveLogValue.of(jobId.toString()), RecursiveLogValue.of(globalAssetId), index,
                RecursiveLogValue.of(cause));
    }

    private Relationship relationship(final ObjectNode singleChildBom) {
        return jsonUtil.fromString(singleChildBom.toString(), submodelType).asRelationships().get(0);
    }

    private Relationship readRelationship(final ObjectNode singleChildBom, final ObjectNode child, final int index) {
        try {
            return relationship(singleChildBom);
        } catch (final JsonParseException exception) {
            child.remove(QUANTITY);
            final Relationship relationship = relationship(singleChildBom);
            logRelationshipFailure(index, "Invalid quantity");
            return relationship;
        }
    }

    private static RecursiveQuantity quantityOf(final Relationship relationship) {
        if (relationship.getLinkedItem().getQuantity() == null) {
            return null;
        }
        final Quantity quantity = relationship.getLinkedItem().getQuantity();
        final ItemUnitEnumeration unit = quantity.getMeasurementUnit() == null
                ? null
                : ItemUnitEnumeration.findByValue(quantity.getMeasurementUnit().getLexicalValue()).orElse(null);
        return RecursiveResultTreeSanitizer.sanitizeQuantity(RecursiveQuantity.builder()
                .value(quantity.getQuantityNumber())
                .unit(unit)
                .build());
    }
}
