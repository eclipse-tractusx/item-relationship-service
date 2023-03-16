package org.eclipse.tractusx.ess.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

interface RelatedInvestigationJobsCache {

    void store(String notificationId, RelatedInvestigationJobs relatedInvestigationJobs);
    Optional<RelatedInvestigationJobs> findByRecursiveRelatedJobId(UUID relatedJobId);
    void remove(String notificationId);

}

@Service
class InMemoryRelatedInvestigationJobsCache implements RelatedInvestigationJobsCache {

    private final ConcurrentHashMap<String, RelatedInvestigationJobs> inMemory = new ConcurrentHashMap<>();

    @Override
    public void store(final String notificationId, final RelatedInvestigationJobs relatedInvestigationJobs) {
        inMemory.put(notificationId, relatedInvestigationJobs);
    }

    @Override
    public Optional<RelatedInvestigationJobs> findByRecursiveRelatedJobId(final UUID relatedJobId) {
        return inMemory.values().stream()
                       .filter(relatedInvestigationJobs -> relatedInvestigationJobs.getRecursiveRelatedJobIds().contains(relatedJobId))
                .findFirst();

    }

    @Override
    public void remove(final String notificationId) {
        inMemory.remove(notificationId);
    }
}
