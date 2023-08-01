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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.testing.KeyUtils.loadPrivateKey;
import static org.eclipse.tractusx.irs.testing.KeyUtils.loadPublicKey;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegritySignerTest {

    private IntegritySigner integritySigner;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {

        final Path pathPrivate = Paths.get(
                Objects.requireNonNull(getClass().getClassLoader().getResource("priv-key.pem")).toURI());
        AsymmetricKeyParameter privateKey = loadPrivateKey(new FileInputStream(pathPrivate.toFile()));

        final Path pathPub = Paths.get(
                Objects.requireNonNull(getClass().getClassLoader().getResource("pub-key.pem")).toURI());
        AsymmetricKeyParameter publicKey = loadPublicKey(new FileInputStream(pathPub.toFile()));

        integritySigner = new IntegritySigner(privateKey, publicKey);
    }

    @Test
    void shouldHashWithSHA3_256() {
        final String testdata = "testdata";
        final byte[] bytes = integritySigner.hashString(testdata);

        assertThat(Hex.toHexString(bytes)).isEqualTo(
                "ebd25cfe070ab282250533a201e38c83249d489a3bf1c8f9718bad6369f59994");
    }

    @Test
    void shouldSign() throws CryptoException {
        final String testdata = "testdata";
        final byte[] bytes = integritySigner.hashString(testdata);
        final String signature = integritySigner.sign(bytes);
        final String expected = "34f3acb2f1255bfcf549d73b29a9d9e765440e5a105c112416834ed29b3983d6d347060cb304b8fc7559168655ae8aff6d64e2d26a5c1996c66b57e56fdb7287f8901061da7cfff8f810b5d5930c463261526cc8721b44ac7f5f9f8442703fba28920f60dd0fe43767ca4587ff210525b13308f96813971c02515085f6a680ba6de86f0b699449cd6fb30fd53acc34dbf525651d3a2dcef00bfac3aee999f33b87b8fed6bc57f2ecf9317546a4e5c2891f27993f6325702d60bfcb30edfaba5b40277276bd1b3a60848831b41da220ce566de008c3179df0fb835d4e7fc7b82064f2a5cbb7531e91c0b09904a8c46583764865c40d161b5a32f2084a792571ad";
        assertThat(signature).isEqualTo(expected);
    }

    @Test
    void shouldVerify() throws CryptoException {
        final String testdata = "testdata";
        final byte[] bytes = integritySigner.hashString(testdata);

        final String signature = integritySigner.sign(bytes);

        assertThat(integritySigner.verify(Hex.decode(signature), bytes)).isTrue();
    }
}