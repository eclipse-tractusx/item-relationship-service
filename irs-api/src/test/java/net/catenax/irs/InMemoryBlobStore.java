package net.catenax.irs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Value;
import net.catenax.irs.persistence.BlobPersistence;
import net.catenax.irs.persistence.BlobPersistenceException;

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

    @Override
    public Collection<byte[]> findBlobByPrefix(final String prefix) {
        return store.entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().startsWith(prefix))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
    }

    @Override
    public boolean delete(final String jobId) {
        return store.remove(jobId) != null;
    }
}
