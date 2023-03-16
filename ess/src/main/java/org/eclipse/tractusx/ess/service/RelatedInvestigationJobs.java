package org.eclipse.tractusx.ess.service;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;

/**
 * Object to store in cache
 */
@Getter
public record RelatedInvestigationJobs(EdcNotification originalNotification, List<UUID> recursiveRelatedJobIds) {

}
