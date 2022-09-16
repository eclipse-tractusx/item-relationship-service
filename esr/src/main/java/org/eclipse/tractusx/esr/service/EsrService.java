/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.esr.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.esr.controller.model.BomLifecycle;
import org.eclipse.tractusx.esr.controller.model.CertificateType;
import org.eclipse.tractusx.esr.controller.model.EsrCertificateStatistics;
import org.eclipse.tractusx.esr.irs.IrsFacade;
import org.eclipse.tractusx.esr.irs.IrsResponse;
import org.eclipse.tractusx.esr.supplyon.EsrCertificate;
import org.eclipse.tractusx.esr.supplyon.SupplyOnFacade;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Business logic for building EsrCertificateStatistics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EsrService {

    private final IrsFacade irsFacade;
    private final SupplyOnFacade supplyOnFacade;

    /**
     * At first mapping between requestor-suppliers is being created.
     * For created mapping - retrieval of ESR Certificate statistics from SupplyOn service.
     *
     * @param globalAssetId for Job registration
     * @param bomLifecycle for Job registration
     * @param certificateName ISO14001
     * @return combined {@link EsrCertificateStatistics}
     */
    public EsrCertificateStatistics handle(final String globalAssetId, final BomLifecycle bomLifecycle,
            final CertificateType certificateName) {

        final EsrCertificateStatistics esrCertificateStatistics = EsrCertificateStatistics.initial();

        final IrsResponse irsResponse = irsFacade.getIrsResponse(globalAssetId, bomLifecycle.getName(), getAuthenticationToken());
        log.info("Retrieved completed IRS job with jobId: {}", irsResponse.getJob().getJobId());

        SupplyOnContainer.from(irsResponse).ifPresent(supplyOn -> {
            supplyOn.getSuppliers().forEach(supplier -> {
                try {
                    log.info("Calling SupplyOn service for ESR Certificate with req: {}, supp: {}", supplyOn.getRequestor().getBpn(), supplier.getBpn());
                    final EsrCertificate esrCertificate = supplyOnFacade.getESRCertificate(supplyOn.getRequestor().getBpn(),
                            supplier.getBpn());

                    log.info("Retrieved ESR Certificate state: {}", esrCertificate.getCertificateState());
                    esrCertificateStatistics.incrementBy(esrCertificate.getCertificateState());
                } catch (RestClientException e) {
                    log.warn("SupplyOn endpoint returned exceptional state. Increasing certificatesWithStateExceptional statistic", e);
                    esrCertificateStatistics.incrementExceptional();
                }
            });
        });

        return esrCertificateStatistics;
    }

    /**
     * @return TODO (me) - its not gonna work for long processing jobs
     */
    private String getAuthenticationToken() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getToken().getTokenValue();
        }

        return "";
    }

}
