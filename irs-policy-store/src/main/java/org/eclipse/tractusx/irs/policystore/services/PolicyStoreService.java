package org.eclipse.tractusx.irs.policystore.services;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

import org.eclipse.tractusx.irs.policystore.models.CreatePolicyRequest;
import org.eclipse.tractusx.irs.policystore.models.Policy;
import org.eclipse.tractusx.irs.policystore.persistence.PolicyPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PolicyStoreService {

    private final String apiAllowedBpn;

    private final Clock clock;

    private final PolicyPersistence persistence;

    public PolicyStoreService(@Value("${apiAllowedBpn:}") final String apiAllowedBpn,
            final PolicyPersistence persistence, final Clock clock) {
        this.apiAllowedBpn = apiAllowedBpn;
        this.persistence = persistence;
        this.clock = clock;
    }

    public void registerPolicy(final CreatePolicyRequest request) {
        persistence.save(apiAllowedBpn,
                new Policy(request.policyId(), OffsetDateTime.now(clock), request.validUntil()));
    }

    public List<Policy> getStoredPolicies() {
        return persistence.readAll(apiAllowedBpn);
    }

    public void deletePolicy(final String policyId) {
        persistence.delete(apiAllowedBpn, policyId);
    }
}
