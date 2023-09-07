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
     * @param itemContainer data
     * @return flag indicates if chain is valid
     */
    public IntegrityState chainDataIntegrityIsValid(final ItemContainer itemContainer) {
        log.debug("Starting validation of Data Chain Integrity with {} integrity aspects.", itemContainer.getIntegrities().size());
        final long numberOfValidSubmodels = itemContainer.getSubmodels()
                                                         .stream()
                                                         .takeWhile(submodel -> submodelDataIntegrityIsValid(submodel, itemContainer.getIntegrities()))
                                                         .count();

        return IntegrityState.from(numberOfValidSubmodels, totalNumberOfSubmodels(itemContainer));
    }

    private boolean submodelDataIntegrityIsValid(final Submodel submodel, final Set<IntegrityAspect> integrities) {
        final IntegrityAspect.ChildData childData = integrities.stream()
                                                               .map(IntegrityAspect::getChildParts)
                                                               .flatMap(Set::stream)
                                                               .filter(findIntegrityChildPart(submodel.getCatenaXId()))
                                                               .findFirst()
                                                               .orElseThrow();

        final IntegrityAspect.Reference reference = childData.getReference()
                                                             .stream()
                                                             .filter(findReference(submodel.getAspectType()))
                                                             .findFirst()
                                                             .orElseThrow();

        log.debug("Calculating hash of Submodel id: {}", submodel.getIdentification());
        final String calculatedHash = calculateHashForRawSubmodelPayload(submodel.getPayload());

        log.debug("Comparing hashes and signatures Data integrity of Submodel id: {}", submodel.getIdentification());
        return hashesEquals(reference.getHash(), calculatedHash)
                && signaturesEquals(reference.getSignature(), bytesOf(submodel.getPayload()));
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

    private int totalNumberOfSubmodels(final ItemContainer itemContainer) {
        return itemContainer.getSubmodels().size();
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
            log.error("Cant retrieve bytes from payload, integrity of data chain cannot be determined.", exc);
            return new byte[0];
        }
    }

    private Predicate<? super IntegrityAspect.Reference> findReference(final String aspectType) {
        return reference -> reference.getSemanticModelUrn().equals(aspectType);
    }

    private Predicate<? super IntegrityAspect.ChildData> findIntegrityChildPart(final String catenaXId) {
        return integrityChildPart -> integrityChildPart.getChildCatenaXId().equals(catenaXId);
    }

}
