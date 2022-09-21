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

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.esr.controller.model.EsrCertificateStatistics;
import org.eclipse.tractusx.esr.irs.IrsResponse;
import org.eclipse.tractusx.esr.irs.model.shell.Shell;
import org.eclipse.tractusx.esr.irs.model.shell.SubmodelDescriptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Aggregation of EsrCertificateStatistics
 */
@Service
@Slf4j
public class EsrCertificateAggregation {

    private final RestTemplate restTemplate;

    public EsrCertificateAggregation(final RestTemplate defaultRestTemplate) {
        this.restTemplate = defaultRestTemplate;
    }

    public EsrCertificateStatistics aggregateStatistics(final IrsResponse irs, final EsrCertificateStatistics esrStatistics) {

        irs.getShells()
                .stream()
                .map(Shell::getSubmodelDescriptors)
                .flatMap(Collection::stream)
                .filter(SubmodelDescriptor::isEsrCertificate)
                .map(SubmodelDescriptor::getEndpoints)
                .flatMap(Collection::stream)
                .map(endpoint -> endpoint.getProtocolInformation().getEndpointAddress())
                .forEach(endpointAddress -> incrementStatistics(esrStatistics, endpointAddress));

        return esrStatistics;
    }

    private void incrementStatistics(final EsrCertificateStatistics esrStatistics, final String url) {
        final EsrCertificateStatistics subStatistics = restTemplate.getForEntity(url,
                EsrCertificateStatistics.class).getBody();

        esrStatistics.incrementBy(subStatistics);
    }

}
