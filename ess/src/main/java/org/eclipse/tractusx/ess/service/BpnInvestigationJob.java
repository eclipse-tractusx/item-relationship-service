package org.eclipse.tractusx.ess.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Submodel;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BpnInvestigationJob {

    private Jobs jobSnapshot;
    private List<String> incidentBpns;
    private List<String> notifications;

    static BpnInvestigationJob create(final Jobs jobSnapshot, final List<String> incidentBpns) {
        return new BpnInvestigationJob(
                extendJobWithSupplyChainSubmodel(jobSnapshot, SupplyChainImpacted.UNKNOWN),
                incidentBpns,
                new ArrayList<>()
        );
    }

    BpnInvestigationJob update(final Jobs jobSnapshot, final SupplyChainImpacted newSupplyChainImpacted) {
//        TODO
//        Get state of jobSnapshot before update
//        Validate if SupplyChainImpacted should be changed - abandon update if not
//        YES	NO	UNKOWN	= YES	If any part is impacted then whole Supply is impactes
//        YES	NO	NO	YES	= Yes if any BPN is impacted even if all are not impacted.
//        NO	UNKNOW	NO	= UNKNOW	Unknown if no Yes and at leat one bpn is unknown state.
//        NO	NO	NO	NO	= No if complete SupplyChain is not impacted
//        this.getJobSnapshot().getSubmodels().get()

        this.jobSnapshot = extendJobWithSupplyChainSubmodel(jobSnapshot, newSupplyChainImpacted);
        return this;
    }

    BpnInvestigationJob withNotifications(final List<String> notifications) {
        this.notifications.addAll(notifications);
        return this;
    }

    private static Jobs extendJobWithSupplyChainSubmodel(final Jobs irsJob, final SupplyChainImpacted supplyChainImpacted) {
        final Submodel supplyChainImpactedSubmodel = Submodel.builder()
                                                             .aspectType("supply_chain_impacted")
                                                             .payload(Map.of("supplyChainImpacted", supplyChainImpacted.getDescription()))
                                                             .build();

        return irsJob.toBuilder().submodels(Collections.singletonList(supplyChainImpactedSubmodel)).build();
    }
}
