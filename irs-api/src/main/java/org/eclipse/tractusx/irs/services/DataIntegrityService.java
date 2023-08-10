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
import java.util.Base64;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.encoders.Hex;
import org.eclipse.tractusx.irs.aaswrapper.job.IntegrityAspect;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.Submodel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Data Integrity of a given data chain validator
 */
@Service
@Slf4j
public class DataIntegrityService {

    private MessageDigest messageDigest;
    private AsymmetricKeyParameter publicKey;

    public DataIntegrityService(@Value("${integrity.publicKeyCert:}") final String publicKeyCert) {
        try {
            messageDigest = MessageDigest.getInstance("SHA3-256");

            Security.addProvider(new BouncyCastleProvider());
            PEMParser reader = new PEMParser(new StringReader(publicKeyCert));
            SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo) reader.readObject();

            publicKey = PublicKeyFactory.createKey(subjectPublicKeyInfo);
        } catch (final Exception e) {
            log.error("Cannot create public key object based on injected publicKeyCert data");
        }
    }

    /**
     * @param itemContainer data
     * @return flag indicates if chain is valid
     */
    public boolean chainDataIntegrityIsValid(final ItemContainer itemContainer) {
        final long numberOfValidSubmodels = itemContainer.getSubmodels().stream().takeWhile(submodel -> {
            try {
                return submodelDataIntegrityIsValid(submodel, itemContainer.getIntegrities());
            } catch (NoSuchElementException exc) {
                log.error("Validation of data integrity not possible - DataIntegrity aspect is missing");
                return false;
            }
        }).count();

        return numberOfValidSubmodels == totalNumberOfSubmodels(itemContainer);
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

        final String calculatedHash = calculateHashForRawSubmodelPayload(submodel.getPayload());
        log.debug("Comparing hashes and signatures Data integrity of Submodel {}", submodel.getIdentification());

        return hashesAreEqual(reference.getHash(), calculatedHash) &&
                signaturesAreEqual(reference.getSignature(), bytesOf(submodel.getPayload()));
    }

    private static boolean hashesAreEqual(final String hashReference, final String calculatedHash) {
        return MessageDigest.isEqual(hashReference.getBytes(StandardCharsets.UTF_8),
                calculatedHash.getBytes(StandardCharsets.UTF_8));
    }

    public boolean signaturesAreEqual(final String signatureReference, final byte[] hashedMessage) {
        final RSADigestSigner verifier = new RSADigestSigner(new SHA256Digest());
        verifier.init(false, publicKey);
        verifier.update(hashedMessage, 0, hashedMessage.length);
        return verifier.verifySignature(signatureReference.getBytes(StandardCharsets.UTF_8));
    }

    private int totalNumberOfSubmodels(final ItemContainer itemContainer) {
        return itemContainer.getSubmodels().size();
    }

    private String calculateHashForRawSubmodelPayload(final Map<String, Object> payload) {
        log.debug("Calculating hash for payload '{}", payload);
        final byte[] digest = bytesOf(payload);
        log.debug("Returning hash '{}'", Hex.toHexString(digest));
        return Hex.toHexString(digest);
    }

    private byte[] bytesOf(final Map<String, Object> payload) {
        return messageDigest.digest(mapToString(payload).getBytes(StandardCharsets.UTF_8));
    }

    private Predicate<? super IntegrityAspect.Reference> findReference(final String aspectType) {
        return reference -> reference.getSemanticModelUrn().equals(aspectType);
    }

    private Predicate<? super IntegrityAspect.ChildData> findIntegrityChildPart(final String catenaXId) {
        return integrityChildPart -> integrityChildPart.getChildCatenaXId().equals(catenaXId);
    }

}
