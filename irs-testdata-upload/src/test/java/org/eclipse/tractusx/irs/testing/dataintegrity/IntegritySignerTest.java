/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegritySignerTest {

    private IntegritySigner integritySigner;

    @BeforeEach
    void setUp() {
        final SecureRandom random = new SecureRandom();
        final BigInteger e = BigInteger.valueOf(0x11);
        final RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
        rsaKeyPairGenerator.init(new RSAKeyGenerationParameters(e, random, 1024, 100));

        final AsymmetricCipherKeyPair asymmetricCipherKeyPair = rsaKeyPairGenerator.generateKeyPair();
        final AsymmetricKeyParameter privateKey = asymmetricCipherKeyPair.getPrivate();
        final AsymmetricKeyParameter publicKey = asymmetricCipherKeyPair.getPublic();

        integritySigner = new IntegritySigner(privateKey, publicKey);
    }

    @Test
    void shouldHashWithSHA3_256() {
        // Arrange
        final String testdata = "testdata";

        // Act
        final byte[] bytes = integritySigner.hashString(testdata);

        // Assert
        assertThat(Hex.toHexString(bytes)).isEqualTo(
                "ebd25cfe070ab282250533a201e38c83249d489a3bf1c8f9718bad6369f59994");
    }

    @Test
    void shouldSign() throws CryptoException {
        // Arrange
        final String testdata = "testdata";
        final byte[] bytes = integritySigner.hashString(testdata);

        // Act
        final String signature = integritySigner.sign(bytes);

        // Assert
        assertThat(signature).isNotBlank();
    }

    @Test
    void shouldVerify() throws CryptoException {
        // Arrange
        final String testdata = "testdata";
        final byte[] bytes = integritySigner.hashString(testdata);

        // Act
        final String signature = integritySigner.sign(bytes);

        // Assert
        assertThat(integritySigner.verify(Hex.decode(signature), bytes)).isTrue();
    }
}