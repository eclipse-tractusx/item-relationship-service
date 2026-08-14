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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveQuantity;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;

final class RecursiveResultTreeSanitizer {

    private RecursiveResultTreeSanitizer() {
    }

    /* package */ static List<RecursiveChildItem> sanitizeNodes(final List<RecursiveChildItem> rawNodes,
            final List<String> selectedAspects) {
        if (rawNodes == null) {
            return List.of();
        }
        return rawNodes.stream()
                       .filter(Objects::nonNull)
                       .map(node -> sanitizeNode(node, selectedAspects, false))
                       .toList();
    }

    /* package */ static RecursiveChildItem sanitizeAndAggregateNode(final RecursiveChildItem rawNode,
            final List<String> selectedAspects) {
        return rawNode == null ? emptyNode() : sanitizeNode(rawNode, selectedAspects, true);
    }

    /* package */ static List<RecursiveTombstone> sanitizeTombstones(
            final List<RecursiveTombstone> rawTombstones, final List<String> selectedAspects) {
        if (rawTombstones == null) {
            return List.of();
        }
        return rawTombstones.stream()
                            .filter(Objects::nonNull)
                            .map(tombstone -> RecursiveTombstones.sanitized(tombstone, selectedAspects))
                            .toList();
    }

    /* package */ static List<RecursiveTombstone> aggregateTombstones(
            final List<RecursiveTombstone> tombstones) {
        final Map<String, RecursiveTombstone> byKey = new LinkedHashMap<>();
        for (final RecursiveTombstone tombstone : emptyIfNull(tombstones)) {
            if (tombstone == null) {
                continue;
            }
            final String key = tombstone.getScope() + "|" + tombstone.getReason() + "|"
                    + tombstone.getAspects() + "|" + tombstone.getDetail();
            final RecursiveTombstone existing = byKey.get(key);
            if (existing == null) {
                byKey.put(key, tombstone);
            } else {
                final List<String> errorRefs = new ArrayList<>(emptyIfNull(existing.getErrorRefs()));
                errorRefs.addAll(emptyIfNull(tombstone.getErrorRefs()));
                byKey.put(key, RecursiveTombstone.builder()
                        .type(existing.getType())
                        .scope(existing.getScope())
                        .aspects(emptyIfNull(existing.getAspects()))
                        .reason(existing.getReason())
                        .retryable(existing.getRetryable())
                        .detail(existing.getDetail())
                        .occurrences(occurrences(existing) + occurrences(tombstone))
                        .errorRefs(List.copyOf(errorRefs))
                        .build());
            }
        }
        return List.copyOf(byKey.values());
    }

    /* package */ static RecursiveQuantity sanitizeQuantity(final RecursiveQuantity quantity) {
        if (quantity == null || quantity.getValue() == null || quantity.getUnit() == null
                || !Double.isFinite(quantity.getValue()) || quantity.getValue() < 0) {
            return null;
        }
        return quantity;
    }

    /* package */ static boolean hasTombstones(final RecursiveChildItem node) {
        return !emptyIfNull(node.getTombstones()).isEmpty()
                || emptyIfNull(node.getChildItems()).stream().anyMatch(
                        RecursiveResultTreeSanitizer::hasTombstones);
    }

    /* package */ static boolean hasUsableData(final RecursiveChildItem node) {
        return node.getMaterialNumber() != null
                || node.getMaterialName() != null
                || !emptyIfNull(node.getItems()).isEmpty()
                || emptyIfNull(node.getChildItems()).stream().anyMatch(
                        RecursiveResultTreeSanitizer::hasUsableData);
    }

    /* package */ static RecursiveChildItem emptyNode() {
        return RecursiveChildItem.builder()
                                 .items(List.of())
                                 .tombstones(List.of())
                                 .childItems(List.of())
                                 .build();
    }

    private static RecursiveChildItem sanitizeNode(final RecursiveChildItem rawNode,
            final List<String> selectedAspects, final boolean aggregateNodeTombstones) {
        final List<RecursiveAspectItem> items = emptyIfNull(rawNode.getItems()).stream()
                .filter(Objects::nonNull)
                .map(item -> sanitizeAspectItem(item, selectedAspects))
                .flatMap(Optional::stream)
                .toList();
        final List<RecursiveTombstone> sanitizedTombstones = sanitizeTombstones(
                rawNode.getTombstones(), selectedAspects);
        final List<RecursiveTombstone> tombstones = aggregateNodeTombstones
                ? aggregateTombstones(sanitizedTombstones)
                : sanitizedTombstones;
        final List<RecursiveChildItem> children = emptyIfNull(rawNode.getChildItems()).stream()
                .filter(Objects::nonNull)
                .map(child -> sanitizeNode(child, selectedAspects, aggregateNodeTombstones))
                .toList();
        return RecursiveChildItem.builder()
                                 .materialNumber(rawNode.getMaterialNumber())
                                 .materialName(rawNode.getMaterialName())
                                 .quantity(sanitizeQuantity(rawNode.getQuantity()))
                                 .items(items)
                                 .tombstones(tombstones)
                                 .childItems(children)
                                 .build();
    }

    private static Optional<RecursiveAspectItem> sanitizeAspectItem(final RecursiveAspectItem rawItem,
            final List<String> selectedAspects) {
        return RecursiveAspect.fromSemanticId(rawItem.getAspect())
                .map(RecursiveAspect::getSemanticId)
                .filter(selectedAspects::contains)
                .filter(ignored -> rawItem.getItems() != null)
                .map(aspect -> RecursiveAspectItem.builder()
                                                  .aspect(aspect)
                                                  .items(Collections.unmodifiableMap(
                                                          new LinkedHashMap<>(rawItem.getItems())))
                                                  .build());
    }

    private static int occurrences(final RecursiveTombstone tombstone) {
        return tombstone.getOccurrences() == null || tombstone.getOccurrences() < 1
                ? 1
                : tombstone.getOccurrences();
    }

    private static <T> List<T> emptyIfNull(final List<T> values) {
        return values == null ? List.of() : values;
    }
}
