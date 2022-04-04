//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package net.catenax.irs.connector.consumer.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketLifecycleArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Expiration;
import io.minio.messages.LifecycleConfiguration;
import io.minio.messages.LifecycleRule;
import io.minio.messages.ResponseDate;
import io.minio.messages.RuleFilter;
import io.minio.messages.Status;
import org.jetbrains.annotations.NotNull;

/**
 * BlobPersistence implementation using the min.io library
 */
public class MinioBlobPersistence implements BlobPersistence {

    private static final Integer EXPIRE_AFTER_DAYS = 7;
    private final MinioClient minioClient;
    private final String bucketName;

    public MinioBlobPersistence(final String endpoint, final String accessKey, final String secretKey,
            final String bucketName) throws BlobPersistenceException {
        this(bucketName, MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build());
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
        final Expiration expiration = new Expiration((ResponseDate) null, EXPIRE_AFTER_DAYS, false);
        final LifecycleRule rule = createExpirationRule(expiration);
        final LifecycleConfiguration lifecycleConfig = new LifecycleConfiguration(List.of(rule));
        minioClient.setBucketLifecycle(
                SetBucketLifecycleArgs.builder().bucket(bucketName).config(lifecycleConfig).build());
    }

    @NotNull
    private LifecycleRule createExpirationRule(final Expiration expiration) {
        return new LifecycleRule(Status.ENABLED, null, expiration, new RuleFilter(""), null, null,
                null, null);
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
    public byte[] getBlob(final String sourceBlobName) throws BlobPersistenceException {
        final GetObjectResponse response;
        try {
            response = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(sourceBlobName).build());
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException | NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException | InternalException e) {
            throw new BlobPersistenceException("Encountered error while trying to load blob", e);
        }
        try (response) {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new BlobPersistenceException("Encountered error while trying to load blob", e);
        }
    }

}
