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
package org.eclipse.tractusx.irs.common.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.SetBucketLifecycleArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Expiration;
import io.minio.messages.Item;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.ResponseDate;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * BlobPersistence implementation using the min.io library
 */
@Slf4j
@SuppressWarnings({ "PMD.ExcessiveImports",
                    "PMD.PreserveStackTrace",
                    "PMD.TooManyMethods"
})
public class MinioBlobPersistence implements BlobPersistence {

    private final MinioClient minioClient;
    private final String bucketName;
    private final int daysToLive;

    public MinioBlobPersistence(final String endpoint, final String accessKey, final String secretKey,
            final String bucketName, final int daysToLive) throws BlobPersistenceException {
        this(bucketName, createClient(endpoint, accessKey, secretKey), daysToLive);
    }

    public MinioBlobPersistence(final String bucketName, final MinioClient client, final int daysToLive) throws BlobPersistenceException {
        this.bucketName = bucketName;
        this.minioClient = client;
        this.daysToLive = daysToLive;

        try {
            createBucketIfNotExists(bucketName);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new BlobPersistenceException("Encountered error while trying to create min.io client", e);
        }
    }

    public final void createBucketIfNotExists(final String bucketName)
            throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException,
            InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {

        if (!this.bucketExists(bucketName)) {
            this.minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            if (this.daysToLive > 0) {
                this.setExpirationLifecycle(bucketName);
            }
        }
    }

    public final boolean bucketExists(final String bucketName)
            throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException,
            InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {

        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    @NotNull
    private static MinioClient createClient(final String endpoint, final String accessKey, final String secretKey) {
        log.info("Building Minio client with url '{}'", endpoint);
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }

    private void setExpirationLifecycle(final String bucketName)
            throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException,
            InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {

        final Expiration expiration = new Expiration((ResponseDate) null, daysToLive, null);
        final LifecycleRule rule = createExpirationRule(expiration);
        final LifecycleConfiguration lifecycleConfig = new LifecycleConfiguration(List.of(rule));
        minioClient.setBucketLifecycle(
                SetBucketLifecycleArgs.builder().bucket(bucketName).config(lifecycleConfig).build());
    }

    private LifecycleRule createExpirationRule(final Expiration expiration) {
        return new LifecycleRule(Status.ENABLED, null, expiration, new RuleFilter(""), null, null, null, null);
    }

    @Override
    public void putBlob(final String targetBlobName, final byte[] blob) throws BlobPersistenceException {
        try {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(blob);
            minioClient.putObject(PutObjectArgs.builder()
                                               .bucket(bucketName)
                                               .object(targetBlobName)
                                               .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                                               .build());
            log.debug("Saving to bucket name {} with object name {}", bucketName, targetBlobName);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new BlobPersistenceException("Encountered error while trying to store blob", e);
        }
    }

    @Override
    public Optional<byte[]> getBlob(final String sourceBlobName) throws BlobPersistenceException {
        final GetObjectResponse response;
        try {
            response = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(sourceBlobName).build());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return Optional.empty();
            }
            throw createLoadFailedException(e);
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw createLoadFailedException(e);
        }
        try (response) {
            return Optional.ofNullable(response.readAllBytes());
        } catch (IOException e) {
            throw createLoadFailedException(e);
        }

    }

    private BlobPersistenceException createLoadFailedException(final Throwable cause) {
        return new BlobPersistenceException("Encountered error while trying to load blob", cause);
    }

    @Override
    public Collection<byte[]> findBlobByPrefix(final String prefix) {
        final Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().prefix(prefix).bucket(bucketName).build());

        return StreamSupport.stream(results.spliterator(), false)
                            .flatMap(this::getItem)
                            .map(Item::objectName)
                            .flatMap(this::getBlobIfPresent)
                            .toList();
    }

    @Override
    public boolean delete(final String sourceBlobName, final List<String> processIds) throws BlobPersistenceException {
        try {
            deleteConnectedProcessesBlobs(processIds);
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(sourceBlobName).build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            } else {
                throw new BlobPersistenceException("Encountered error while trying to delete blob", e);
            }
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new BlobPersistenceException("Encountered error while trying to delete blob", e);
        }
    }

    private void deleteConnectedProcessesBlobs(final List<String> processIds) {
        processIds.forEach(processId -> {
            try {
                minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(processId).build());
            } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException | ErrorResponseException e) {
                log.info("No object data with process Id {} found", processId);
            }
        });
    }

    private Stream<byte[]> getBlobIfPresent(final String sourceBlobName) {
        try {
            return getBlob(sourceBlobName).stream();
        } catch (BlobPersistenceException e) {
            log.error("Cannot find content for blob id {}", sourceBlobName);
            return Stream.empty();
        }
    }

    private Stream<Item> getItem(final Result<Item> result) {
        try {
            return Stream.of(result.get());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            log.error("Encountered error while trying to retrieve result content", e);
            return Stream.empty();
        }
    }

}
