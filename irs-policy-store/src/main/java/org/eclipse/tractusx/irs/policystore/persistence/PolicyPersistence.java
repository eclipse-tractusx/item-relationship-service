package org.eclipse.tractusx.irs.policystore.persistence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.eclipse.tractusx.irs.policystore.models.Policy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyPersistence {
    private final BlobPersistence persistence;

    private final ObjectMapper mapper;

    /**
     * The timeout in milliseconds to try to acquire locks.
     */
    private static final int TIMEOUT = 30_000;
    /**
     * A lock to synchronize access to the collection of stored jobs.
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void save(final String bpn, final Policy policy) {
        final var policies = readAll(bpn);
        policies.add(policy);
        save(bpn, policies);
    }

    public void delete(final String bpn, final String policyId) {
        final var policies = readAll(bpn);
        final var modifiedPolicies = policies.stream().filter(p -> !p.policyId().equals(policyId)).toList();
        save(bpn, modifiedPolicies);
    }

    private void save(final String bpn, final List<Policy> modifiedPolicies) {
        writeLock(() -> {
            try {
                persistence.putBlob(bpn, mapper.writeValueAsBytes(modifiedPolicies));
            } catch (BlobPersistenceException | JsonProcessingException e) {
                throw new PolicyStoreException("TODO", e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<Policy> readAll(final String bpn) {
        try {
            return persistence.getBlob(bpn).map(blob -> {
                try {
                    return mapper.readValue(blob, List.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).map(policies -> new ArrayList<Policy>(policies)).orElseGet(ArrayList::new);

        } catch (BlobPersistenceException e) {
            throw new RuntimeException(e);
        }

    }

    private void writeLock(final Runnable work) {
        try {
            if (!lock.writeLock().tryLock(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new PolicyStoreException("Timeout acquiring write lock");
            }
            try {
                work.run();
            } finally {
                lock.writeLock().unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PolicyStoreException("Interrupted while storing policy data", e);
        }
    }
}
