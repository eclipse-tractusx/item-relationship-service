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
package org.eclipse.tractusx.irs.services;

import static org.eclipse.tractusx.irs.data.StringMapper.mapToString;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.tractusx.irs.aaswrapper.job.IntegrityAspect;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.IntegrityState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Data Integrity of a given data chain validator
 */
@Service
@Slf4j
public class DataIntegrityService {

    private static final String SHA3_256 = "SHA3-256";
    private AsymmetricKeyParameter publicKey;

    public DataIntegrityService(@Value("${integrity.publicKeyCert:}") final String publicKeyCert) {
        Security.addProvider(new BouncyCastleProvider());
        try (PEMParser reader = new PEMParser(new StringReader(publicKeyCert))) {
            final SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) reader.readObject();

            publicKey = PublicKeyFactory.createKey(subjectPublicKeyInfo);
        } catch (final IOException | IllegalArgumentException e) {
            log.error("Cannot create public key object based on injected publicKeyCert data", e);
        }
    }

    /**
     * @param itemContainer data container
     * @param globalAssetId tier 0 submodels DataIntegrity aspect is missing
     * @return flag indicates if chain is valid
     */
    public IntegrityState chainDataIntegrityIsValid(final ItemContainer itemContainer, final String globalAssetId) {
        log.info("Starting validation of Data Chain Integrity with {} Integrity aspects and {} Submodels.", itemContainer.getIntegrities().size(), itemContainer.getSubmodels().size());
        final long numberOfValidSubmodels = itemContainer.getSubmodels()
                                                         .stream()
                                                         .filter(notTierZeroSubmodel(globalAssetId))
                                                         .takeWhile(submodel -> submodelDataIntegrityIsValid(submodel, itemContainer.getIntegrities()))
                                                         .count();
//        itemContainer.getTombstones().
        itemContainer.testAddTombstone();

        return IntegrityState.from(numberOfValidSubmodels, totalNumberOfSubmodels(itemContainer, globalAssetId));
    }

    private boolean submodelDataIntegrityIsValid(final Submodel submodel, final Set<IntegrityAspect> integrities) {
        log.debug("Validation data integrity of submodel: {}, {}, {}", submodel.getCatenaXId(), submodel.getAspectType(), submodel.getIdentification());
        final Optional<IntegrityAspect.Reference> reference = findIntegrityAspectReferenceForSubmodel(submodel, integrities);

        if (reference.isPresent()) {
            log.debug("Calculating hash of Submodel id: {}", submodel.getIdentification());
            final String calculatedHash = calculateHashForRawSubmodelPayload(submodel.getPayload());

            log.debug("Comparing hashes and signatures Data integrity of Submodel id: {}", submodel.getIdentification());
            return hashesEquals(reference.get().getHash(), calculatedHash)
                    && signaturesEquals(reference.get().getSignature(), bytesOf(submodel.getPayload()));
        } else {
            log.warn("Integrity of Data chain cannot be determined, as Data Integrity Aspect Reference was not found for Submodel: {}, {}", submodel.getIdentification(), submodel.getAspectType());
            return false;
        }
    }

    private Optional<IntegrityAspect.Reference> findIntegrityAspectReferenceForSubmodel(final Submodel submodel, final Set<IntegrityAspect> integrities) {
        return integrities.stream()
                          .map(IntegrityAspect::getChildParts)
                          .flatMap(Set::stream)
                          .filter(childData -> childData.catenaXIdMatches(submodel.getCatenaXId()))
                          .findFirst()
                          .flatMap(childData -> childData.getReferences()
                                                         .stream()
                                                         .filter(reference -> reference.semanticModelUrnMatches(submodel.getAspectType()))
                                                         .findFirst());
    }

    private static boolean hashesEquals(final String hashReference, final String calculatedHash) {
        return MessageDigest.isEqual(hashReference.getBytes(StandardCharsets.UTF_8),
                calculatedHash.getBytes(StandardCharsets.UTF_8));
    }

    private boolean signaturesEquals(final String signatureReference, final byte[] hashedMessage) {
        final RSADigestSigner verifier = new RSADigestSigner(new SHA256Digest());
        verifier.init(false, publicKey);
        verifier.update(hashedMessage, 0, hashedMessage.length);
        return verifier.verifySignature(Hex.decode(signatureReference));
    }

    private long totalNumberOfSubmodels(final ItemContainer itemContainer, final String globalAssetId) {
        return itemContainer.getSubmodels().stream().filter(notTierZeroSubmodel(globalAssetId)).count();
    }

    private String calculateHashForRawSubmodelPayload(final Map<String, Object> payload) {
        final byte[] digest = bytesOf(payload);
        log.debug("Returning hash '{}'", Hex.toHexString(digest));
        return Hex.toHexString(digest);
    }

    private byte[] bytesOf(final Map<String, Object> payload) {
        try {
            return MessageDigest.getInstance(SHA3_256).digest(mapToString(payload).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exc) {
            log.error("Integrity of Data chain cannot be determined, cant retrieve bytes from payload.", exc);
            return new byte[0];
        }
    }

    private static Predicate<Submodel> notTierZeroSubmodel(final String globalAssetId) {
        return submodel -> !submodel.getCatenaXId().equals(globalAssetId);
    }

}
