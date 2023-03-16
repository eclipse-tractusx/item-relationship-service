package org.eclipse.tractusx.ess.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EssRecursiveNotificationHandler {

    private final RelatedInvestigationJobsCache relatedInvestigationJobsCache;
    private final BpnInvestigationJobCache bpnInvestigationJobCache;

    private final EdcNotificationSender edcNotificationSender;

    /* package */ void handleNotification(UUID finishedJobId, SupplyChainImpacted supplyChainImpacted) {
        final Optional<RelatedInvestigationJobs> relatedJobsId = relatedInvestigationJobsCache.findByRecursiveRelatedJobId(
                finishedJobId);

        relatedJobsId.ifPresent(relatedJobs -> {
            if (SupplyChainImpacted.YES.equals(supplyChainImpacted)) {
                edcNotificationSender.sendEdcNotification(relatedJobs.originalNotification(), supplyChainImpacted);
                relatedInvestigationJobsCache.remove(relatedJobs.originalNotification().getHeader().getNotificationId());
            } else {
                sendNotificationAfterAllCompleted(relatedJobs);
            }
}
        );
    }

    private void sendNotificationAfterAllCompleted(RelatedInvestigationJobs relatedInvestigationJobs) {
        List<BpnInvestigationJob> allInvestigationJobs = relatedInvestigationJobs.getRecursiveRelatedJobIds()
                                                                                 .stream()
                                                                                 .map(bpnInvestigationJobCache::findByJobId)
                                                                                 .flatMap(Optional::stream)
                                                                                 .toList();
        if (checkAllFinished(allInvestigationJobs)) {
            final SupplyChainImpacted finalResult = allInvestigationJobs.stream()
                                                                        .map(BpnInvestigationJob::getSupplyChainImpacted)
                                                                        .flatMap(Optional::stream)
                                                                        .reduce(SupplyChainImpacted.NO,
                                                                                SupplyChainImpacted::or);
            edcNotificationSender.sendEdcNotification(relatedInvestigationJobs.originalNotification(),
                    finalResult);
        }

    }


    private boolean checkAllFinished(final List<BpnInvestigationJob> allInvestigationJobs) {
        return allInvestigationJobs.stream().allMatch(bpnInvestigationJob -> bpnInvestigationJob.getSupplyChainImpacted().isPresent());
    }
}
