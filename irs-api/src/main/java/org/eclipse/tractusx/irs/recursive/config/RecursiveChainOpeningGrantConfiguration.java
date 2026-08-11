/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.config;

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
 * Configures the dedicated blob persistence for recursive chain opening grants.
 */
@Configuration
public class RecursiveChainOpeningGrantConfiguration {

    public static final String CHAIN_OPENING_GRANT_BLOB_PERSISTENCE = "chainOpeningGrantPersistence";

    @Profile("!test")
    @Bean(CHAIN_OPENING_GRANT_BLOB_PERSISTENCE)
    @ConditionalOnProperty(name = "blobstore.persistence.storeType", havingValue = "MINIO")
    public BlobPersistence minioBlobStore(final BlobStoreConfiguration config) throws BlobPersistenceException {
        final MinioBlobstoreConfiguration minioConfig = config.getPersistence().getMinio();
        final BlobStoreContainerConfiguration chainOpeningGrantConfig = config.getChainOpeningGrants();

        if (minioConfig == null || chainOpeningGrantConfig == null) {
            throw new IllegalArgumentException("Missing chain opening grant blob storage configuration");
        }

        return new MinioBlobPersistence(minioConfig.getEndpoint(), minioConfig.getAccessKey(), minioConfig.getSecretKey(),
                chainOpeningGrantConfig.getContainerName(), chainOpeningGrantConfig.getDaysToLive());
    }

    @Profile("!test")
    @Bean(CHAIN_OPENING_GRANT_BLOB_PERSISTENCE)
    @ConditionalOnProperty(name = "blobstore.persistence.storeType", havingValue = "AZURE")
    public BlobPersistence azureBlobStore(final BlobStoreConfiguration config) {
        final AzureBlobstoreConfiguration azureConfig = config.getPersistence().getAzure();
        final BlobStoreContainerConfiguration chainOpeningGrantConfig = config.getChainOpeningGrants();

        if (azureConfig == null || chainOpeningGrantConfig == null) {
            throw new IllegalArgumentException("Missing chain opening grant blob storage configuration");
        }

        if (azureConfig.isUseConnectionString()) {
            return new AzureBlobPersistence(azureConfig.getConnectionString(),
                    chainOpeningGrantConfig.getContainerName());
        } else {
            return new AzureBlobPersistence(azureConfig.getBaseUrl(), azureConfig.getClientId(),
                    azureConfig.getClientSecret(), azureConfig.getTenantId(),
                    chainOpeningGrantConfig.getContainerName());
        }
    }
}
