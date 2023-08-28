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
package org.eclipse.tractusx.irs.testing.dataintegrity;

import static org.eclipse.tractusx.irs.testing.dataintegrity.KeyUtils.loadPrivateKey;
import static org.eclipse.tractusx.irs.testing.dataintegrity.KeyUtils.loadPublicKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;

/**
 * Enrich a Testdata file
 */
@Slf4j
public final class TestdataTransformer {

    public static final int INDEX_INPUT_PATH = 0;
    public static final int INDEX_OUTPUT_PATH = 1;
    public static final int INDEX_PRIVATE_KEY_PATH = 2;
    public static final int INDEX_PUBLIC_KEY_PATH = 3;

    private TestdataTransformer() {
    }

    public static void main(final String[] args) throws IOException {
        final String inputFilePath = args[INDEX_INPUT_PATH];
        final String outputFilePath = args[INDEX_OUTPUT_PATH];
        final String keyFilePath = args[INDEX_PRIVATE_KEY_PATH];
        final String certFilePath = args[INDEX_PUBLIC_KEY_PATH];

        final String testdata = Files.readString(Paths.get(inputFilePath));
        final AsymmetricKeyParameter privateKey = loadPrivateKey(Files.newInputStream(Paths.get(keyFilePath)));
        final AsymmetricKeyParameter publicKey = loadPublicKey(Files.newInputStream(Paths.get(certFilePath)));

        final IntegritySigner integritySigner = new IntegritySigner(privateKey, publicKey);
        final IntegrityAspectCreator integrityAspectCreator = new IntegrityAspectCreator(integritySigner);

        final String enrichedTestdata = integrityAspectCreator.enrichTestdata(testdata);
        writeResultToFile(outputFilePath, enrichedTestdata);
    }

    private static void writeResultToFile(final String outputFilePath, final String enrichedTestdata)
            throws IOException {
        final Path path = Paths.get(outputFilePath);
        log.info("Writing enriched testdata file to '{}'", path.toAbsolutePath());
        Files.writeString(path, enrichedTestdata);
    }
}
