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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Externally visible recursive tombstone.
 *
 * <p>Identical failures (same scope, reason, aspects and detail) are aggregated within their
 * result-tree node: {@code occurrences} counts them and {@code errorRefs} keeps one correlation id
 * per occurrence. Placement in a {@link RecursiveChildItem} associates a failure with an anonymous
 * material node without exposing participant identities.</p>
 *
 * <p>This model is separate from the iterative IRS tombstone because recursive results must not carry
 * partner, endpoint, policy or asset identifiers across tiers.</p>
 */
@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecursiveTombstone {

    private String type;
    private RecursiveTombstoneScope scope;

    /**
     * The requested aspect semantic IDs this failure relates to.
     */
    private List<String> aspects;
    private RecursiveTombstoneReason reason;
    private Boolean retryable;
    private String detail;

    /**
     * Number of identical failures aggregated into this tombstone.
     */
    private Integer occurrences;

    /**
     * Identity-free correlation ids, one per occurrence. They carry no partner data themselves,
     * but let a support request (customer quotes an errorRef) be mapped to the detailed internal
     * diagnostics in the logs of the node that produced it.
     */
    private List<String> errorRefs;
}
