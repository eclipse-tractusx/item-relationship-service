package org.eclipse.tractusx.irs.policystore.models;

import java.time.OffsetDateTime;

public record Policy(String policyId, OffsetDateTime createdOn, OffsetDateTime validUntil) {
}
