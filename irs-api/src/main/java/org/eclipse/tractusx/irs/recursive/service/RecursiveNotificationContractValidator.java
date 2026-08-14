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

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import jakarta.validation.Validator;

import lombok.RequiredArgsConstructor;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspect;
import org.eclipse.tractusx.irs.recursive.model.RecursiveAspectItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveChildItem;
import org.eclipse.tractusx.irs.recursive.model.RecursiveJobResult;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationType;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResponseStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveResultStatus;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstone;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneType;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

/** Validates the shared notification envelope and the recursive operation contract. */
@RequiredArgsConstructor
public class RecursiveNotificationContractValidator {

    private final Validator validator;
    private final ObjectMapper objectMapper;

    public Optional<RoutingFields> readRoutingFields(final JsonNode payload) {
        if (payload == null || !payload.isObject()) {
            return Optional.empty();
        }
        final JsonNode header = payload.get("header");
        if (header == null || !header.isObject()) {
            return Optional.empty();
        }
        final JsonNode content = payload.get("content");
        return Optional.of(new RoutingFields(textValue(header, "senderBpn"), textValue(header, "messageId"),
                textValue(header, "relatedMessageId"), notificationType(content)));
    }

    public Optional<RecursiveNotificationMessage> decode(final JsonNode payload) {
        if (payload == null) {
            return Optional.empty();
        }
        final ObjectReader reader = objectMapper.readerFor(RecursiveNotificationMessage.class)
                                                .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        try (JsonParser parser = objectMapper.treeAsTokens(payload)) {
            return Optional.ofNullable(reader.readValue(parser));
        } catch (final IOException exception) {
            return Optional.empty();
        }
    }

    public boolean isValid(final RecursiveNotificationMessage message) {
        if (message == null || !validator.validate(message).isEmpty()) {
            return false;
        }
        final RecursiveNotificationMessage.Header header = message.getHeader();
        if (!isTimestamp(header.getSentDateTime())) {
            return false;
        }
        final RecursiveNotificationMessage.Content content = message.getContent();
        return content.getType() == RecursiveNotificationType.REQUEST
                ? isValidRequest(header, content)
                : isValidResponse(header, content);
    }

    private boolean isValidRequest(final RecursiveNotificationMessage.Header header,
            final RecursiveNotificationMessage.Content content) {
        return isTimestamp(header.getExpectedResponseBy())
                && content.getGlobalAssetId() != null
                && content.getBomLifecycle() == BomLifecycle.AS_PLANNED
                && content.getAspects() != null
                && !content.getAspects().isEmpty()
                && content.getUseCase() != null
                && content.getUseCase()
                        .selectAspectIds(content.getBomLifecycle(), content.getAspects())
                        .isPresent()
                && content.getStatus() == null
                && content.getResult() == null;
    }

    private boolean isValidResponse(final RecursiveNotificationMessage.Header header,
            final RecursiveNotificationMessage.Content content) {
        return header.getRelatedMessageId() != null
                && !header.getRelatedMessageId().isBlank()
                && header.getExpectedResponseBy() == null
                && content.getBomLifecycle() == BomLifecycle.AS_PLANNED
                && content.getStatus() != null
                && content.getResult() != null
                && isValidResult(content);
    }

    private boolean isValidResult(final RecursiveNotificationMessage.Content content) {
        final RecursiveJobResult result = content.getResult();
        if (content.getUseCase() == null) {
            return false;
        }
        final Set<String> selectedAspects = content.getUseCase()
                .selectAspectIds(content.getBomLifecycle(), content.getAspects())
                .orElse(Set.of());
        if (selectedAspects.isEmpty()
                || result.getResultStatus() == null
                || result.getChildItems() == null
                || result.getTombstones() == null
                || !Objects.equals(result.getUseCase(), content.getUseCase())
                || !Objects.equals(result.getBomLifecycle(), content.getBomLifecycle())
                || !containsSameAspects(result.getRequestedAspects(), selectedAspects)
                || !isValidTombstones(result.getTombstones(), selectedAspects)
                || content.getStatus() == RecursiveResponseStatus.FAILED
                        && result.getResultStatus() != RecursiveResultStatus.FAILED) {
            return false;
        }
        final boolean validRoot = result.getChildItems().size() == 1
                || content.getStatus() == RecursiveResponseStatus.FAILED
                        && result.getChildItems().isEmpty()
                        && !result.getTombstones().isEmpty();
        return validRoot && result.getChildItems().stream()
                .allMatch(node -> isValidNode(node, selectedAspects));
    }

    private boolean isValidNode(final RecursiveChildItem node, final Set<String> selectedAspects) {
        return node != null
                && node.getItems() != null
                && node.getTombstones() != null
                && node.getChildItems() != null
                && (node.getQuantity() == null
                        || RecursiveResultTreeSanitizer.sanitizeQuantity(node.getQuantity()) != null)
                && hasUniqueAspectItems(node.getItems(), selectedAspects)
                && isValidTombstones(node.getTombstones(), selectedAspects)
                && node.getChildItems().stream().allMatch(child -> isValidNode(child, selectedAspects));
    }

    private boolean hasUniqueAspectItems(final List<RecursiveAspectItem> items, final Set<String> selectedAspects) {
        final Set<String> seenAspects = new HashSet<>();
        for (final RecursiveAspectItem item : items) {
            final Optional<String> aspect = aspectId(item, selectedAspects);
            if (aspect.isEmpty() || !seenAspects.add(aspect.get())) {
                return false;
            }
        }
        return true;
    }

    private Optional<String> aspectId(final RecursiveAspectItem item, final Set<String> selectedAspects) {
        if (item == null || item.getItems() == null) {
            return Optional.empty();
        }
        return RecursiveAspect.fromSemanticId(item.getAspect())
                .map(RecursiveAspect::getSemanticId)
                .filter(selectedAspects::contains);
    }

    private boolean isValidTombstones(final List<RecursiveTombstone> tombstones,
            final Set<String> selectedAspects) {
        return tombstones.stream().allMatch(tombstone -> tombstone != null
                && tombstone.getType() == RecursiveTombstoneType.RECURSIVE_TOMBSTONE
                && tombstone.getScope() != null
                && tombstone.getReason() != null
                && Objects.equals(tombstone.getRetryable(), tombstone.getReason().isRetryable())
                && tombstone.getDetail() != null
                && !tombstone.getDetail().isBlank()
                && RecursivePatternStore.SAFE_SINGLE_LINE_PATTERN.matcher(tombstone.getDetail()).matches()
                && tombstone.getOccurrences() != null
                && tombstone.getOccurrences() > 0
                && tombstone.getAspects() != null
                && tombstone.getAspects().stream().allMatch(selectedAspects::contains)
                && tombstone.getErrorRefs() != null
                && !tombstone.getErrorRefs().isEmpty()
                && tombstone.getErrorRefs().stream().allMatch(this::isUuid));
    }

    private boolean isTimestamp(final String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            return true;
        } catch (final DateTimeParseException exception) {
            return false;
        }
    }

    private boolean containsSameAspects(final List<String> actual, final Set<String> expected) {
        return actual != null && actual.stream().noneMatch(Objects::isNull)
                && new HashSet<>(actual).equals(expected);
    }

    private RecursiveNotificationType notificationType(final JsonNode content) {
        final String type = textValue(content, "type");
        if (type == null) {
            return null;
        }
        try {
            return RecursiveNotificationType.valueOf(type);
        } catch (final IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isUuid(final String value) {
        if (value == null) {
            return false;
        }
        return RecursivePatternStore.UUID_PATTERN.matcher(value).matches();
    }

    private String textValue(final JsonNode parent, final String fieldName) {
        if (parent == null || !parent.isObject()) {
            return null;
        }
        final JsonNode value = parent.get(fieldName);
        return value == null || !value.isTextual() ? null : value.textValue();
    }

    /** Fields read before strict notification deserialization. */
    public record RoutingFields(String senderBpnl, String messageId, String relatedMessageId,
                                RecursiveNotificationType type) {
    }
}
