/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.testing.dataintegrity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.IntegrityAspect;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.IntegrityChildPart;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.IntegrityReference;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.SingleLevelBomAsBuiltPayload;
import org.eclipse.tractusx.irs.testing.dataintegrity.models.TestdataContainer;

/**
 * Create and add a DataIntegrity AspectModels to a testdata set.
 */
@Slf4j
public class IntegrityAspectCreator {
    public static final String SLBAB_IDENTIFIER = "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt";
    public static final String DIL_IDENTIFIER = "urn:bamm:io.catenax.data_integrity:1.0.0#DataIntegrity";
    public static final String CATENA_X_ID_IDENTIFIER = "catenaXId";
    private final ObjectMapper objectMapper;
    private final IntegritySigner integritySigner;

    public IntegrityAspectCreator(final IntegritySigner integritySigner) {
        this.integritySigner = integritySigner;
        objectMapper = new ObjectMapper();
    }

    public String enrichTestdata(final String jsonData) throws IOException {
        final TestdataContainer testdataContainer = objectMapper.readValue(jsonData, TestdataContainer.class);
        final List<Map<String, Object>> testdata = testdataContainer.getContainer();

        for (final Map<String, Object> digitalTwin : testdata) {
            log.info("Building Integrity Aspect for '{}'", digitalTwin.get(CATENA_X_ID_IDENTIFIER));
            addIntegrityAspect(digitalTwin, testdata);
        }
        return objectMapper.writeValueAsString(testdataContainer);
    }

    private String mapToString(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IntegrityAspectException(e);
        }
    }

    private <T> T readValue(final String data, final Class<T> type) {
        try {
            return objectMapper.readValue(data, type);
        } catch (JsonProcessingException e) {
            throw new IntegrityAspectException(e);
        }
    }

    private void addIntegrityAspect(final Map<String, Object> digitalTwin, final List<Map<String, Object>> testdata) {
        if (digitalTwin.containsKey(DIL_IDENTIFIER)) {
            log.info("Integrity Aspect already present");
            return;
        }

        if (digitalTwin.containsKey(SLBAB_IDENTIFIER)) {
            final List<SingleLevelBomAsBuiltPayload> payloads = new ArrayList<>();
            digitalTwin.entrySet()
                       .stream()
                       .filter(stringObjectEntry -> SLBAB_IDENTIFIER.equals(stringObjectEntry.getKey()))
                       .map(Map.Entry::getValue)
                       .map(this::mapToString)
                       .map(o -> readValue(o, SingleLevelBomAsBuiltPayload[].class))
                       .map(Arrays::asList)
                       .forEach(payloads::addAll);

            if (!payloads.isEmpty()) {
                final SingleLevelBomAsBuiltPayload singleLevelBomAsBuiltPayloads = payloads.stream()
                                                                                           .findFirst()
                                                                                           .orElseThrow();
                singleLevelBomAsBuiltPayloads.getChildItems()
                                             .stream()
                                             .map(SingleLevelBomAsBuiltPayload.ChildData::getCatenaXId)
                                             .map(id -> findDigitalTwinById(testdata, id))
                                             .forEach(stringObjectMap -> addIntegrityAspect(stringObjectMap, testdata));

                final List<IntegrityChildPart> integrityChildParts = new ArrayList<>();
                singleLevelBomAsBuiltPayloads.getChildItems()
                                             .stream()
                                             .map(SingleLevelBomAsBuiltPayload.ChildData::getCatenaXId)
                                             .map(id -> findDigitalTwinById(testdata, id))
                                             .forEach(stringObjectMap -> {
                                                 final IntegrityChildPart integrityChildPart = createIntegrityChildPart(
                                                         stringObjectMap);
                                                 integrityChildParts.add(integrityChildPart);
                                             });
                final String cxId = (String) digitalTwin.get(CATENA_X_ID_IDENTIFIER);
                final IntegrityAspect integrityAspect = new IntegrityAspect(cxId, integrityChildParts);
                log.info("Adding integrity Aspect to Twin.");
                digitalTwin.put(DIL_IDENTIFIER, List.of(integrityAspect));
            }
        }
    }

    private IntegrityChildPart createIntegrityChildPart(final Map<String, Object> childTwin) {
        final List<IntegrityReference> references = new ArrayList<>();
        final String cxId = (String) childTwin.get(CATENA_X_ID_IDENTIFIER);
        log.info("Creating Integrity Part for ID: '{}'", cxId);
        childTwin.entrySet()
                 .stream()
                 .filter(aspectModel -> !CATENA_X_ID_IDENTIFIER.equals(aspectModel.getKey()) && !"bpnl".equals(
                         aspectModel.getKey()))
                 .forEach(aspectModelEntry -> references.add(createIntegrityReference(aspectModelEntry)));
        return new IntegrityChildPart(cxId, references);
    }

    protected IntegrityReference createIntegrityReference(final Map.Entry<String, Object> aspectModelEntry) {
        final String aspectModel = mapToString(aspectModelEntry.getValue());
        final Object[] strings = readValue(aspectModel, Object[].class);
        try {
            final byte[] payloadHash = integritySigner.hashString(mapToString(strings[0]));
            final String payloadSignature = integritySigner.sign(payloadHash);
            return new IntegrityReference(aspectModelEntry.getKey(), Hex.toHexString(payloadHash), payloadSignature);
        } catch (CryptoException e) {
            throw new IntegrityAspectException(e);
        }
    }

    private Map<String, Object> findDigitalTwinById(final List<Map<String, Object>> testdata, final String catenaXId) {
        for (final Map<String, Object> digitalTwin : testdata) {
            final String twinCatenaXId = (String) digitalTwin.get(CATENA_X_ID_IDENTIFIER);
            if (catenaXId.equals(twinCatenaXId)) {
                return digitalTwin;
            }
        }
        return Map.of();
    }
}
