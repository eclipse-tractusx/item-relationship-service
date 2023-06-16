package org.eclipse.tractusx.irs.policystore.models;

import java.time.OffsetDateTime;

public record CreatePolicyRequest(String policyId, OffsetDateTime validUntil) {

}
