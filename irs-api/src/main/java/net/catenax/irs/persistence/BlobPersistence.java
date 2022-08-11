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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Interface for storing data blobs.
 */
public interface BlobPersistence {

    void putBlob(String targetBlobName, byte[] blob) throws BlobPersistenceException;

    Optional<byte[]> getBlob(String sourceBlobName) throws BlobPersistenceException;

    Collection<byte[]> findBlobByPrefix(String prefix) throws BlobPersistenceException;

    boolean delete(String jobId, List<String> processIds) throws BlobPersistenceException;
}
