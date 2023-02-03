package org.eclipse.tractusx.ess.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Submodel;

@RequiredArgsConstructor
@Value
public class BpnInvestigationJob {

    private final Jobs jobSnapshot;
    private final List<String> incidentBpns;
    private final List<String> notifications;

    static BpnInvestigationJob create(final Jobs jobSnapshot, final List<String> incidentBpns) {
        return new BpnInvestigationJob(
                extendJobWithSupplyChainSubmodel(jobSnapshot, SupplyChainImpacted.UNKNOWN),
                incidentBpns,
                new ArrayList<>()
        );
    }

    BpnInvestigationJob update(final Jobs jobSnapshot, final SupplyChainImpacted supplyChainImpacted) {
        return new BpnInvestigationJob(
                extendJobWithSupplyChainSubmodel(jobSnapshot, supplyChainImpacted),
                this.incidentBpns,
                this.notifications
        );
    }

    BpnInvestigationJob withNotifications(final List<String> notifications) {
        this.notifications.addAll(notifications);
        return this;
    }

    private static Jobs extendJobWithSupplyChainSubmodel(final Jobs irsJob, final SupplyChainImpacted supplyChainImpacted) {
        final Submodel supplyChainImpactedSubmodel = Submodel.builder()
                                                             .payload(Map.of("supplyChainImpacted", supplyChainImpacted))
                                                             .build();

        return irsJob.toBuilder().submodels(Collections.singletonList(supplyChainImpactedSubmodel)).build();
    }
}
