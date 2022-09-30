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
package org.eclipse.tractusx.esr.irs;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for IRS domain
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class IrsFacade {

    private static final int DEPTH = 1;
    private final IrsClient irsClient;

    public IrsResponse getIrsResponse(final String globalAssetId, final String bomLifecycle) {
        final StartJobResponse response = irsClient.startJob(IrsRequest.builder()
                                                                       .globalAssetId(globalAssetId)
                                                                       .bomLifecycle(bomLifecycle)
                                                                       .depth(DEPTH)
                                                                       .aspects(List.of("EsrCertificateStateStatistic"))
                                                                       .collectAspects(false)
                                                                       .build());
        log.info("Registered IRS job with jobId: {}", response.getJobId());
        return irsClient.getJobDetails(response.getJobId());
    }

}
