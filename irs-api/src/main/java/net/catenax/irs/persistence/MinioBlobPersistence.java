//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package net.catenax.irs.persistence;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * BlobPersistence implementation using the min.io library
 */
@Slf4j
@SuppressWarnings("PMD.ExcessiveImports")
public class MinioBlobPersistence implements BlobPersistence {

    private static final Integer EXPIRE_AFTER_DAYS = 7;
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioBlobPersistence(final String endpoint, final String accessKey, final String secretKey,
                                final String bucketName) throws BlobPersistenceException {
        this(bucketName, createClient(endpoint, accessKey, secretKey));
    }

    @NotNull
    private static MinioClient createClient(final String endpoint, final String accessKey, final String secretKey) {
        log.info("Building Minio client with url '{}'", endpoint);
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }

    public MinioBlobPersistence(final String bucketName, final MinioClient client) throws BlobPersistenceException {
        this.bucketName = bucketName;
        this.minioClient = client;

        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            setExpirationLifecycle(bucketName);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new BlobPersistenceException("Encountered error while trying to create min.io client", e);
        }
    }

    private void setExpirationLifecycle(final String bucketName)
            throws ErrorResponseException, InsufficientDataException, InternalException, InvalidKeyException,
            InvalidResponseException, IOException, NoSuchAlgorithmException, ServerException, XmlParserException {
        final Expiration expiration = new Expiration((ResponseDate) null, EXPIRE_AFTER_DAYS, null);
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
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new BlobPersistenceException("Encountered error while trying to store blob", e);
        }
    }

    @Override
    public Optional<byte[]> getBlob(final String sourceBlobName) throws BlobPersistenceException {
        final GetObjectResponse response;
        try {
            response = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(sourceBlobName).build());
            return Optional.ofNullable(response.readAllBytes());
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return Optional.empty();
            }
            throw createLoadFailedException(e);
        } catch (ServerException | InsufficientDataException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
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
                .collect(Collectors.toList());
    }

    @Override
    public boolean delete(final String sourceBlobName) throws BlobPersistenceException {
        try {
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
