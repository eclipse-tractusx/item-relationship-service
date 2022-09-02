package org.eclipse.tractusx.irs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.Value;
import org.eclipse.tractusx.irs.persistence.BlobPersistence;

@Value
public class InMemoryBlobStore implements BlobPersistence {

    Map<String, byte[]> store = new ConcurrentHashMap<>();

    @Override
    public void putBlob(final String targetBlobName, final byte[] blob) {
        store.put(targetBlobName, blob);
    }

    @Override
    public Optional<byte[]> getBlob(final String sourceBlobName) {
        return Optional.ofNullable(store.get(sourceBlobName));
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
    public boolean delete(final String jobId, final List<String> processIds) {
        processIds.forEach(store::remove);
        return store.remove(jobId) != null;
    }
}
