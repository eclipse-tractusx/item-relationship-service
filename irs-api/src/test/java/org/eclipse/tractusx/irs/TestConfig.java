/********************************************************************************
 * Copyright (c) 2022 ZF Friedrichshafen AG
 * Copyright (c) 2022 ISTOS GmbH
 * Copyright (c) 2022 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2022 BOSCH AG
 * Copyright (c) 2026 Volkswagen AG
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs;

import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.configuration.JobConfiguration;
import org.eclipse.tractusx.irs.policystore.config.PolicyConfiguration;
import org.eclipse.tractusx.irs.recursive.config.RecursiveChainOpeningGrantConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Primary
    @Bean(JobConfiguration.JOB_BLOB_PERSISTENCE)
    public BlobPersistence inMemoryBlobStore() {
        return new InMemoryBlobStore();
    }

    @Bean(PolicyConfiguration.POLICY_BLOB_PERSISTENCE)
    public BlobPersistence inMemoryBlobStore2() {
        return new InMemoryBlobStore();
    }

    @Bean(RecursiveChainOpeningGrantConfiguration.CHAIN_OPENING_GRANT_BLOB_PERSISTENCE)
    public BlobPersistence inMemoryRecursiveChainOpeningGrantBlobStore() {
        return new InMemoryBlobStore();
    }
}
