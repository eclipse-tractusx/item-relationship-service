package org.eclipse.tractusx.ess.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.edc.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.component.JobHandle;
import org.eclipse.tractusx.irs.component.RegisterBpnInvestigationJob;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EssRecursiveService {

    private final EssService essService;
    private final RelatedInvestigationJobsCache relatedInvestigationJobsCache;
    private final String localBpn;
    private final EdcNotificationSender edcNotificationSender;

    /* package */ EssRecursiveService(final EssService essService,
            final RelatedInvestigationJobsCache relatedInvestigationJobsCache,
            @Value("${ess.localBpn}") final String localBpn,
            final EdcNotificationSender edcNotificationSender) {
        this.essService = essService;
        this.relatedInvestigationJobsCache = relatedInvestigationJobsCache;
        this.localBpn = localBpn;
        this.edcNotificationSender = edcNotificationSender;
    }

    public void handleNotification(final EdcNotification notification) {

        final Optional<String> incidentBpn = Optional.ofNullable(notification.getContent().get("incidentBpn"))
                                                     .map(Object::toString);

        Optional<Object> concernedCatenaXIdsNotification = Optional.ofNullable(notification.getContent().get("concernedCatenaXIds"));

        if (incidentBpn.isPresent() && localBpn.equals(incidentBpn.get())) {
            edcNotificationSender.sendEdcNotification(notification, SupplyChainImpacted.YES);
        } else if (concernedCatenaXIdsNotification.isPresent() && concernedCatenaXIdsNotification.get() instanceof List
                && incidentBpn.isPresent()) {
            final String bpn = incidentBpn.get();
                    final List<String> concernedCatenaXIds = getConcernedCatenaXIds(concernedCatenaXIdsNotification);

                    List<UUID> createdJobs = concernedCatenaXIds.stream()
                                                                .map(catenaXId -> essService.startIrsJob(
                                                                        RegisterBpnInvestigationJob.builder()
                                                                                                   .incidentBpns(
                                                                                                           List.of(bpn))
                                                                                                   .globalAssetId(
                                                                                                           catenaXId)
                                                                                                   .build()))
                                                                .map(JobHandle::getId)
                                                                .toList();
                    relatedInvestigationJobsCache.store(
                            notification.getHeader().getNotificationId(),
                            new RelatedInvestigationJobs(notification, createdJobs));
            }
        }


    @NotNull
    private static List<String> getConcernedCatenaXIds(final Optional<Object> concernedCatenaXIdsNotification) {
        return ((List<String>) concernedCatenaXIdsNotification.get()).stream().collect(Collectors.toList());

    }
}
