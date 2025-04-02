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
package org.eclipse.tractusx.irs.policystore.config;

import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.tractusx.irs.common.persistence.AzureBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.config.AzureBlobstoreConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreContainerConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.MinioBlobstoreConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Config values for the policy store.
 */
@Configuration
public class PolicyConfiguration {

    public static final String POLICY_BLOB_PERSISTENCE = "policyStorePersistence";

    @Profile("!test")
    @Bean(POLICY_BLOB_PERSISTENCE)
    @ConditionalOnProperty(name = "blobstore.persistence.storeType", havingValue = "MINIO")
    public BlobPersistence minioBlobStore(final BlobStoreConfiguration config) throws BlobPersistenceException {
        final MinioBlobstoreConfiguration minioConfig = config.getPersistence().getMinio();
        final BlobStoreContainerConfiguration policyConfig = config.getPolicies();

        if (minioConfig == null || policyConfig == null) {
            throw new IllegalArgumentException("Missing blob storage configuration");
        }

        return new MinioBlobPersistence(minioConfig.getEndpoint(), minioConfig.getAccessKey(), minioConfig.getSecretKey(),
                policyConfig.getContainerName(), policyConfig.getDaysToLive());
    }

    @Profile("!test")
    @Bean(POLICY_BLOB_PERSISTENCE)
    @ConditionalOnProperty(name = "blobstore.persistence.storeType", havingValue = "AZURE")
    public BlobPersistence azureBlobStore(final BlobStoreConfiguration config) {
        final AzureBlobstoreConfiguration azureConfig = config.getPersistence().getAzure();
        final BlobStoreContainerConfiguration policyConfig = config.getPolicies();

        if (azureConfig == null || policyConfig == null) {
            throw new IllegalArgumentException("Missing blob storage configuration");
        }

        if (azureConfig.isUseConnectionString()) {
            return new AzureBlobPersistence(azureConfig.getConnectionString(), policyConfig.getContainerName());
        } else {
            return new AzureBlobPersistence(azureConfig.getBaseUrl(), azureConfig.getClientId(), azureConfig.getClientSecret(),
                    azureConfig.getTenantId(), policyConfig.getContainerName());
        }
    }

    @Bean
    public TypeTransformerRegistry typeTransformerRegistry() {
        return new TypeTransformerRegistryImpl();
    }
}
