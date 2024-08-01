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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

/**
 * Utilities to hash, sign and verify data.
 */
public class IntegritySigner {
    private final AsymmetricKeyParameter privKey;
    private final AsymmetricKeyParameter publicKey;

    public IntegritySigner(final AsymmetricKeyParameter privKey, final AsymmetricKeyParameter publicKey) {
        Security.addProvider(new BouncyCastleProvider());
        this.privKey = privKey;
        this.publicKey = publicKey;
    }

    public byte[] hashString(final String data) {
        final MessageDigest sha256Digest;
        try {
            sha256Digest = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IntegrityAspectException(e);
        }
        return sha256Digest.digest(data.getBytes(StandardCharsets.UTF_8));
    }

    public String sign(final byte[] hashedMessage) throws CryptoException {
        final RSADigestSigner rsaDigestSigner = new RSADigestSigner(new SHA256Digest());
        rsaDigestSigner.init(true, privKey);
        rsaDigestSigner.update(hashedMessage, 0, hashedMessage.length);
        final byte[] signature = rsaDigestSigner.generateSignature();
        return Hex.toHexString(signature);
    }

    public boolean verify(final byte[] signature, final byte[] hashedMessage) {
        final RSADigestSigner verifier = new RSADigestSigner(new SHA256Digest());
        verifier.init(false, publicKey);
        verifier.update(hashedMessage, 0, hashedMessage.length);
        return verifier.verifySignature(signature);
    }

}
