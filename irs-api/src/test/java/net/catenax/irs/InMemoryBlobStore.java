package net.catenax.irs;

import java.util.HashMap;
import java.util.Map;

import lombok.Value;
import net.catenax.irs.persistence.BlobPersistence;

@Value
public class InMemoryBlobStore implements BlobPersistence {

    Map<String, byte[]> store = new HashMap<>();

    @Override
    public void putBlob(final String targetBlobName, final byte[] blob) {
        store.put(targetBlobName, blob);
    }

    @Override
    public byte[] getBlob(final String sourceBlobName) {
        return store.get(sourceBlobName);
    }
}
