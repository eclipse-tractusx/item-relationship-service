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
package org.eclipse.tractusx.irs.recursive.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.recursive.util.RecursivePatternStore;

/**
 * Unified notification message for partner-to-partner communication.
 * A single notification endpoint handles both REQUEST and RESPONSE types,
 * aligned with the Industry-Core target architecture.
 *
 * <p>Header/content separation keeps technical routing metadata separate from
 * the business payload. PURIS-specific content, such as anonymized item stock,
 * can be carried without changing the envelope structure.</p>
 */
@Value
@Builder
@Jacksonized
@Schema(description = "Recursive notification envelope. REQUEST messages start the next recursive child hop. "
        + "RESPONSE messages return the correlated child result to the parent.")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecursiveNotificationMessage {

    public static final String HEADER_CONTEXT = "IndustryCore-RecursiveIrsNotificationApi-Receive:1.0.0";
    public static final String MESSAGE_HEADER_SEMANTIC_ID =
            "urn:samm:io.catenax.shared.message_header:3.0.0#MessageHeaderAspect";
    public static final String HEADER_VERSION = "3.0.0";

    private static final String HEADER_CONTEXT_PATTERN =
            "^IndustryCore-RecursiveIrsNotificationApi-Receive:1\\.0\\.0$";

    @Valid
    @NotNull
    private Header header;

    @Valid
    @NotNull
    private Content content;

    /**
     * Transport metadata used for routing, correlation, and access validation.
     */
    @Value
    @Builder
    @Jacksonized
    @Schema(description = "CX-0151 message header based on "
            + RecursiveNotificationMessage.MESSAGE_HEADER_SEMANTIC_ID
            + ". REQUEST messages require expectedResponseBy. RESPONSE messages require relatedMessageId "
            + "and must not contain expectedResponseBy.")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Header {

        @NotBlank
        @Pattern(regexp = RecursivePatternStore.MESSAGE_ID_STRING,
                 message = "messageId must be a UUID or URN UUID")
        private String messageId;

        /**
         * Links this message to a previous request (for responses).
         */
        @Pattern(regexp = RecursivePatternStore.MESSAGE_ID_STRING,
                 message = "relatedMessageId must be a UUID or a URN UUID")
        @Schema(description = "Correlation to the previous recursive notification. "
                + "REQUEST uses it to link to the parent job message. RESPONSE uses it to reference "
                + "the child request being answered.")
        private String relatedMessageId;

        @NotBlank
        @Pattern(regexp = HEADER_CONTEXT_PATTERN,
                 message = "context must identify version 1.0.0 of the recursive IRS notification API")
        @Schema(description = "Recursive IRS notification API context.")
        private String context;

        @NotBlank
        @Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
                 message = "sentDateTime must not contain control or line separator characters")
        private String sentDateTime;

        @NotBlank
        @Pattern(regexp = RecursivePatternStore.BPNL_STRING, message = "senderBpn must be a valid BPNL")
        @JsonProperty("senderBpn")
        @Schema(name = "senderBpn", description = "Sender BPNL.")
        private String senderBpnl;

        @NotBlank
        @Pattern(regexp = RecursivePatternStore.BPNL_STRING, message = "receiverBpn must be a valid BPNL")
        @JsonProperty("receiverBpn")
        @Schema(name = "receiverBpn", description = "Receiver BPNL.")
        private String receiverBpnl;

        /**
         * ISO 8601 timestamp. Partner should respond before this time.
         */
        @Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
                 message = "expectedResponseBy must not contain control or line separator characters")
        @Schema(description = "REQUEST only. ISO 8601 timestamp by which the child should respond. "
                + "Must be omitted for RESPONSE messages.")
        private String expectedResponseBy;

        @NotBlank
        @Pattern(regexp = "^3\\.0\\.0$", message = "version must identify MessageHeaderAspect 3.0.0")
        @Schema(description = "MessageHeaderAspect semantic model version.")
        private String version;
    }

    /**
     * Business payload carried by the recursive notification.
     */
    @Value
    @Builder
    @Jacksonized
    @Schema(description = "Recursive notification content. REQUEST requires type, openingId, useCase, globalAssetId, "
            + "bomLifecycle=asPlanned and a non-empty aspects list; status and result must be omitted. "
            + "RESPONSE requires type, openingId, useCase, bomLifecycle=asPlanned, the same non-empty aspects list, "
            + "status and result.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Content {

        @NotNull
        private RecursiveNotificationType type;

        @NotBlank
        @Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
                 message = "openingId must not contain control or line separator characters")
        private String openingId;

        @NotNull
        private RecursiveUseCase useCase;

        /**
         * The asset to traverse (mandatory for REQUEST).
         */
        @Pattern(regexp = RecursivePatternStore.GLOBAL_ASSET_ID_STRING,
                 message = "globalAssetId must be a valid UUID or URN UUID")
        private String globalAssetId;

        private BomLifecycle bomLifecycle;

        private List<@Pattern(regexp = RecursivePatternStore.SAFE_SINGLE_LINE_STRING,
                message = "aspects must not contain control or line separator characters") String> aspects;

        /**
         * For RESPONSE: status of the child partner's recursive processing.
         */
        private RecursiveResponseStatus status;

        /**
         * For RESPONSE: aggregated result payload.
         */
        @Valid
        private RecursiveJobResult result;
    }
}
