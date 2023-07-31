/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.testing;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.testing.models.IntegrityAspect;
import org.eclipse.tractusx.irs.testing.models.IntegrityChildPart;
import org.eclipse.tractusx.irs.testing.models.IntegrityReference;
import org.eclipse.tractusx.irs.testing.models.SingleLevelBomAsBuiltPayload;
import org.eclipse.tractusx.irs.testing.models.TestdataContainer;

/**
 * Create and add a DataIntegrity AspectModels to a testdata set.
 */
/*protected*/ class IntegrityAspectCreator {
    public static final String SLBAB_IDENTIFIER = "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt";
    public static final String DIL_IDENTIFIER = "urn:bamm:io.catenax.data_integrity:1.0.0#DataIntegrity";
    public static final String CATENA_X_ID_IDENTIFIER = "catenaXId";
    public static final int HEX_255 = 0xff;
    private final MessageDigest sha256Digest;
    private final PrivateKey privateKey;
    private final ObjectMapper objectMapper;
    private final Signature signer;

    /*protected*/ IntegrityAspectCreator(final MessageDigest digest, final Signature signer,
            final ObjectMapper objectMapper, final PrivateKey privateKey) {
        sha256Digest = digest;
        this.signer = signer;
        this.privateKey = privateKey;
        this.objectMapper = objectMapper;
    }

    public String enrichTestdata(final String jsonData) throws IOException {
        final TestdataContainer testdataContainer = objectMapper.readValue(jsonData, TestdataContainer.class);
        final List<Map<String, Object>> testdata = testdataContainer.getContainer();

        for (Map<String, Object> digitalTwin : testdata) {
            System.out.printf("Building Integrity Aspect for '%s'%n", digitalTwin.get(CATENA_X_ID_IDENTIFIER));
            addIntegrityAspect(digitalTwin, testdata);
        }
        return objectMapper.writeValueAsString(testdataContainer);
    }

    private byte[] hashString(String data) {
        return sha256Digest.digest(data.getBytes(StandardCharsets.UTF_8));
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (final byte b : hash) {
            String hex = Integer.toHexString(HEX_255 & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String signWithPkcs115(PrivateKey key, byte[] hashedMessage)
            throws InvalidKeyException, SignatureException {
        signer.initSign(key);
        signer.update(hashedMessage);
        byte[] signature = signer.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    private String mapToString(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T readValue(String s, Class<T> type) {
        try {
            return objectMapper.readValue(s, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void addIntegrityAspect(Map<String, Object> digitalTwin, List<Map<String, Object>> testdata) {
        if (digitalTwin.containsKey(DIL_IDENTIFIER)) {
            System.out.println("Integrity Aspect already present");
            return;
        }

        if (digitalTwin.containsKey(SLBAB_IDENTIFIER)) {
            List<SingleLevelBomAsBuiltPayload> payloads = new ArrayList<>();
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
                                                 IntegrityChildPart integrityChildPart = createIntegrityReference(
                                                         stringObjectMap);
                                                 integrityChildParts.add(integrityChildPart);
                                             });
                final String cxId = (String) digitalTwin.get(CATENA_X_ID_IDENTIFIER);
                final IntegrityAspect integrityAspect = new IntegrityAspect(cxId, integrityChildParts);
                System.out.println("Adding integrity Aspect to Twin.");
                digitalTwin.put(DIL_IDENTIFIER, List.of(integrityAspect));
            }
        }
    }

    private IntegrityChildPart createIntegrityReference(Map<String, Object> childTwin) {
        List<IntegrityReference> references = new ArrayList<>();
        final String cxId = (String) childTwin.get(CATENA_X_ID_IDENTIFIER);
        System.out.println("Creating Integrity Part for ID: " + cxId);
        childTwin.entrySet()
                 .stream()
                 .filter(aspectModel -> !CATENA_X_ID_IDENTIFIER.equals(aspectModel.getKey()) && !"bpnl".equals(
                         aspectModel.getKey()))
                 .forEach(aspectModelEntry -> {
                     final String aspectModel = mapToString(aspectModelEntry.getValue());
                     final Object[] strings = readValue(aspectModel, Object[].class);
                     byte[] payloadHash = hashString(mapToString(strings[0]));
                     try {
                         String payloadSignature = signWithPkcs115(privateKey, payloadHash);
                         final IntegrityReference integrityReference = new IntegrityReference(aspectModelEntry.getKey(),
                                 bytesToHex(payloadHash), payloadSignature);
                         references.add(integrityReference);
                     } catch (InvalidKeyException | SignatureException e) {
                         e.printStackTrace();
                     }
                 });
        return new IntegrityChildPart(cxId, references);
    }

    private Map<String, Object> findDigitalTwinById(List<Map<String, Object>> testdata, String catenaXId) {
        for (Map<String, Object> digitalTwin : testdata) {
            final String twinCatenaXId = (String) digitalTwin.get(CATENA_X_ID_IDENTIFIER);
            if (catenaXId.equals(twinCatenaXId)) {
                return digitalTwin;
            }
        }
        return Map.of();
    }
}
