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

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

/**
 * Enrich a Testdata file
 */
public class TestdataTransformer {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        final String inputFilePath = args[0];
        final String outputFilePath = args[1];
        final String keyFilePath = args[2];

        final String testdata = Files.readString(Paths.get(inputFilePath));

        final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        final PrivateKey privateKey = readPrivateKey(keyFilePath, keyFactory);

        final ObjectMapper objectMapper = new ObjectMapper();
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA3-256");
        final Signature signer = Signature.getInstance("SHA256withRSA");
        Security.addProvider(new BouncyCastleProvider());

        final IntegrityAspectCreator integrityAspectCreator = new IntegrityAspectCreator(messageDigest, signer,
                objectMapper, privateKey);

        final String enrichedTestdata = integrityAspectCreator.enrichTestdata(testdata);

        writeResultToFile(outputFilePath, enrichedTestdata);
    }

    private static void writeResultToFile(final String outputFilePath, final String enrichedTestdata) throws IOException {
        final Path path = Paths.get(outputFilePath);
        System.out.printf("Writing enriched testdata file to '%s'%n", path.toAbsolutePath());
        Files.writeString(path, enrichedTestdata);
    }

    private static PrivateKey readPrivateKey(final String file, final KeyFactory keyFactory)
            throws IOException, InvalidKeySpecException {
        try (FileReader keyReader = new FileReader(file); PemReader pemReader = new PemReader(keyReader)) {
            final PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            final PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
            return keyFactory.generatePrivate(privKeySpec);
        }
    }
}

