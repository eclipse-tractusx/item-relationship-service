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

/**
 * Interface for storing data blobs.
 */
public interface BlobPersistence {

    void putBlob(String targetBlobName, byte[] blob) throws BlobPersistenceException;

    byte[] getBlob(String sourceBlobName) throws BlobPersistenceException;

    Collection<byte[]> findBlobByPrefix(String prefix) throws BlobPersistenceException;

    boolean delete(String jobId) throws BlobPersistenceException;
}
