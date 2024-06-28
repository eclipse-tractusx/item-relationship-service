/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.ess.service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.Job;
import org.eclipse.tractusx.irs.component.Jobs;
import org.eclipse.tractusx.irs.component.Notification;
import org.eclipse.tractusx.irs.component.Submodel;
import org.eclipse.tractusx.irs.component.Summary;
import org.eclipse.tractusx.irs.component.enums.JobState;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.ResponseNotificationContent;
import org.eclipse.tractusx.irs.util.JsonUtil;

/**
 * Object to store in cache
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class BpnInvestigationJob {

    private static final String SUPPLY_CHAIN_ASPECT_TYPE = "supply_chain_impacted";

    private Jobs jobSnapshot;
    private List<String> incidentBpns;
    private List<Notification> unansweredNotifications;
    private List<EdcNotification<ResponseNotificationContent>> answeredNotifications;
    private JobState state;

    public BpnInvestigationJob(final Jobs jobSnapshot, final List<String> incidentBpns) {
        this(jobSnapshot, incidentBpns, new ArrayList<>(), new ArrayList<>(), JobState.RUNNING);
    }

    public BpnInvestigationJob update(final Jobs jobSnapshot, final SupplyChainImpacted newSupplyChain) {
        final Optional<SupplyChainImpacted> previousSupplyChain = getSupplyChainImpacted();

        final SupplyChainImpacted supplyChainImpacted = previousSupplyChain.map(
                prevSupplyChain -> prevSupplyChain.or(newSupplyChain)).orElse(newSupplyChain);

        this.jobSnapshot = extendJobWithSupplyChainSubmodel(jobSnapshot, supplyChainImpacted);
        this.jobSnapshot = extendSummary(this.jobSnapshot);
        this.jobSnapshot = updateLastModified(this.jobSnapshot, ZonedDateTime.now(ZoneOffset.UTC));
        return this;
    }

    public BpnInvestigationJob withUnansweredNotifications(final List<Notification> notifications) {
        this.unansweredNotifications.addAll(notifications);
        return this;
    }

    public BpnInvestigationJob withAnsweredNotification(
            final EdcNotification<ResponseNotificationContent> notification) {
        final Optional<String> bpn = getChildBpn(notification);
        removeFromUnansweredNotification(notification);
        notification.getContent().setBpn(bpn.orElse(null));
        notification.getContent().incrementHops();
        this.answeredNotifications.add(notification);

        return this;
    }

    private Optional<String> getChildBpn(final EdcNotification<ResponseNotificationContent> notification) {
        return this.unansweredNotifications.stream()
                                           .filter(unansweredNotification -> unansweredNotification.notificationId()
                                                                                                   .equals(notification.getHeader()
                                                                                                                       .getOriginalNotificationId()))
                                           .map(Notification::childBpn)
                                           .findAny();
    }

    private void removeFromUnansweredNotification(final EdcNotification<ResponseNotificationContent> notification) {
        this.unansweredNotifications.removeIf(unansweredNotification -> unansweredNotification.notificationId()
                                                                                              .equals(notification.getHeader()
                                                                                                                  .getOriginalNotificationId()));
    }

    public BpnInvestigationJob complete() {
        this.state = JobState.COMPLETED;
        return this;
    }

    /* package */ Optional<SupplyChainImpacted> getSupplyChainImpacted() {
        return this.getJobSnapshot()
                   .getSubmodels()
                   .stream()
                   .filter(sub -> SUPPLY_CHAIN_ASPECT_TYPE.equals(sub.getAspectType()))
                   .map(sub -> sub.getPayload().get("supplyChainImpacted"))
                   .map(Object::toString)
                   .map(SupplyChainImpacted::fromString)
                   .findFirst();
    }

    private Jobs extendJobWithSupplyChainSubmodel(final Jobs irsJob, final SupplyChainImpacted supplyChainImpacted) {
        final SupplyChainImpactedAspect.SupplyChainImpactedAspectBuilder supplyChainImpactedAspectBuilder = SupplyChainImpactedAspect.builder()
                                                                                                                                     .supplyChainImpacted(
                                                                                                                                             supplyChainImpacted);

        if (getUnansweredNotifications().isEmpty()) {
            final Optional<SupplyChainImpactedAspect.ImpactedSupplierFirstLevel> impactedSupplierWithLowestHopsNumber = getImpactedSupplierWithLowestHopsNumber();
            supplyChainImpactedAspectBuilder.impactedSuppliersOnFirstTier(
                    impactedSupplierWithLowestHopsNumber.orElse(null));
        }

        return irsJob.toBuilder()
                     .clearSubmodels()
                     .submodels(Collections.singletonList(
                             createSupplyChainImpactedSubmodel(supplyChainImpactedAspectBuilder)))
                     .build();
    }

    private Optional<SupplyChainImpactedAspect.ImpactedSupplierFirstLevel> getImpactedSupplierWithLowestHopsNumber() {
        return getAnsweredNotifications().stream()
                                         .map(EdcNotification::getContent)
                                         .filter(ResponseNotificationContent::thereIsIncident)
                                         .min(Comparator.comparing(ResponseNotificationContent::getHops))
                                         .map(impacted -> new SupplyChainImpactedAspect.ImpactedSupplierFirstLevel(
                                                 impacted.getBpn(), impacted.getHops()));
    }

    private static Submodel createSupplyChainImpactedSubmodel(
            final SupplyChainImpactedAspect.SupplyChainImpactedAspectBuilder supplyChainImpactedAspectBuilder) {
        return Submodel.builder()
                       .aspectType(SUPPLY_CHAIN_ASPECT_TYPE)
                       .payload(new JsonUtil().asMap(supplyChainImpactedAspectBuilder.build()))
                       .build();
    }

    private Jobs updateLastModified(final Jobs irsJob, final ZonedDateTime lastModifiedOn) {
        final Job job = irsJob.getJob().toBuilder().completedOn(lastModifiedOn).lastModifiedOn(lastModifiedOn).build();
        return irsJob.toBuilder().job(job).build();
    }

    private Jobs extendSummary(final Jobs irsJob) {
        final Summary oldSummary = Optional.ofNullable(irsJob.getJob().getSummary()).orElse(Summary.builder().build());
        final NotificationSummary newSummary = new NotificationSummary(oldSummary.getAsyncFetchedItems(),
                new NotificationItems(unansweredNotifications.size() + answeredNotifications.size(),
                        answeredNotifications.size()));
        final Job job = irsJob.getJob().toBuilder().summary(newSummary).build();
        return irsJob.toBuilder().job(job).build();
    }

}
