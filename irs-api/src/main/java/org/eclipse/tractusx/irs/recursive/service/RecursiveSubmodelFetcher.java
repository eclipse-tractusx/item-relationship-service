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

import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.DSP_ENDPOINT;
import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.extractAssetId;
import static org.eclipse.tractusx.irs.aaswrapper.job.ExtractDataFromProtocolInformation.extractDspEndpoint;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.recursive.model.RecursiveTombstoneReason;
import org.eclipse.tractusx.irs.recursive.util.RecursiveLogValue;

/**
 * Loads one local submodel payload through the EDC submodel facade.
 *
 * <p>The endpoints of a submodel descriptor are tried in order and the first payload that can be
 * retrieved and parsed as a JSON object is used. If every endpoint fails, the last failure is
 * classified into a tombstone reason with an anonymized detail.</p>
 */
@Slf4j
@RequiredArgsConstructor
class RecursiveSubmodelFetcher {

    private final EdcSubmodelFacade submodelFacade;
    private final ObjectMapper objectMapper;

    /**
     * Loads the payload from the first endpoint that can be read.
     *
     * @param aspect semantic id of the requested aspect, used for logging
     * @param endpoints endpoints of one submodel descriptor, at least one
     * @param localBpnl BPNL of this IRS instance
     * @param globalAssetId global asset id of the local material, used for logging
     * @return the payload, or the reason and detail of the last failure if no endpoint could be read
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    /* package */ SubmodelCollectionResult collectFromEndpoints(final String aspect, final List<Endpoint> endpoints,
            final String localBpnl, final String globalAssetId) {
        Exception lastFailure = null;
        for (final Endpoint endpoint : endpoints) {
            try {
                return new SubmodelCollectionResult(requestSubmodelPayload(endpoint, localBpnl), null, null);
            } catch (final Exception exception) {
                lastFailure = exception;
                log.warn("Could not collect recursive aspect '{}' from asset '{}' at bpn '{}' via one of {} "
                                + "endpoint(s): causeType={}",
                        RecursiveLogValue.of(aspect), RecursiveLogValue.of(globalAssetId),
                        RecursiveLogValue.of(localBpnl), endpoints.size(), exception.getClass().getName());
            }
        }
        return new SubmodelCollectionResult(Map.of(),
                RecursiveFailureReasonMapper.localAspectFailureReason(lastFailure),
                RecursiveFailureDetails.anonymizedDetail(lastFailure));
    }

    /* package */ static String aspectType(final SubmodelDescriptor descriptor) {
        final Reference semanticId = descriptor.getSemanticId();
        if (semanticId == null || semanticId.getKeys() == null || semanticId.getKeys().isEmpty()) {
            return null;
        }
        final SemanticId firstKey = semanticId.getKeys().get(0);
        return firstKey == null ? null : firstKey.getValue();
    }

    @SuppressWarnings("PMD.UseConcurrentHashMap")
    /* package */ static Optional<Map<String, Object>> payloadMap(final Object payload) {
        if (!(payload instanceof Map<?, ?> rawMap)) {
            return Optional.empty();
        }
        final Map<String, Object> result = new LinkedHashMap<>();
        rawMap.forEach((key, value) -> {
            if (key != null) {
                result.put(key.toString(), value);
            }
        });
        return Optional.of(Collections.unmodifiableMap(result));
    }

    private Map<String, Object> readPayload(final String payload) throws JsonProcessingException {
        final Object parsedPayload = objectMapper.readValue(payload, Object.class);
        return payloadMap(parsedPayload)
                .orElseThrow(() -> new IllegalArgumentException("Submodel payload must be a JSON object."));
    }

    private Map<String, Object> requestSubmodelPayload(final Endpoint endpoint, final String localBpnl)
            throws EdcClientException, JsonProcessingException {
        final String body = endpoint.getProtocolInformation().getSubprotocolBody();
        final Optional<String> dsp = extractDspEndpoint(body);
        if (dsp.isEmpty()) {
            throw new EdcClientException("Submodel descriptor endpoint does not contain " + DSP_ENDPOINT + ".");
        }
        return readPayload(submodelFacade.getSubmodelPayload(
                dsp.get(), endpoint.getProtocolInformation().getHref(), extractAssetId(body), localBpnl).getPayload());
    }

    /**
     * Payload of one submodel. A failed request carries an empty payload together with the failure
     * reason and its anonymized detail, a successful one leaves both {@code null}.
     */
    record SubmodelCollectionResult(Map<String, Object> payload, RecursiveTombstoneReason failureReason,
            String failureDetail) {
    }
}
