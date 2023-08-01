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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

public class KeyUtils {
    private KeyUtils() {
    }

    public static AsymmetricKeyParameter loadPublicKey(InputStream is) {
        SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo) readPemObject(is);
        try {
            return PublicKeyFactory.createKey(publicKeyInfo);
        } catch (IOException e) {
            throw new IntegrityAspectException("Cannot create public key object based on input data", e);
        }
    }

    public static AsymmetricKeyParameter loadPrivateKey(InputStream is) {
        PEMKeyPair keyPair = (PEMKeyPair) readPemObject(is);
        PrivateKeyInfo privateKeyInfo = keyPair.getPrivateKeyInfo();
        try {
            return PrivateKeyFactory.createKey(privateKeyInfo);
        } catch (IOException e) {
            throw new IntegrityAspectException("Cannot create private key object based on input data", e);
        }
    }
    private static Object readPemObject(InputStream is) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            PEMParser pemParser = new PEMParser(inputStreamReader);

            Object object = pemParser.readObject();
            if (object == null) {
                throw new IntegrityAspectException("No PEM object found");
            }
            return object;
        } catch (Exception e) {
            throw new IntegrityAspectException("Cannot read PEM object from input data", e);
        }
    }
}
