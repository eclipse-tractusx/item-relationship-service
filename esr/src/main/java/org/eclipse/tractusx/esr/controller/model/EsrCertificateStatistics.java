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
package org.eclipse.tractusx.esr.controller.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.eclipse.tractusx.esr.supplyon.CertificateState;

/**
 * ESR certificate statistics data model
 */
@Value
@Builder
public class EsrCertificateStatistics {

    private UUID jobId;
    private CertificateType certificateName;
    private CertificateStatistics certificateStateStatistic;

    /**
     * @return Initial {@link EsrCertificateStatistics} object with zero statistics states
     */
    public static EsrCertificateStatistics initial() {
        return EsrCertificateStatistics.builder()
                                       .jobId(UUID.randomUUID())
                                       .certificateName(CertificateType.ISO14001)
                                       .certificateStateStatistic(EsrCertificateStatistics.CertificateStatistics.builder()
                                             .certificatesWithStateValid(0)
                                             .certificatesWithStateInvalid(0)
                                             .certificatesWithStateUnknown(0)
                                             .certificatesWithStateExceptional(0)
                                             .build())
                                       .build();
    }

    /**
     * Method to merge statistics from suppliers
     * @param certificateState state of certificate
     * @return This with statistic incremented
     */
    public EsrCertificateStatistics incrementBy(final CertificateState certificateState) {
        this.certificateStateStatistic.incrementBy(certificateState);
        return this;
    }

    /**
     * @return This with state exceptional statistic incremented
     */
    public EsrCertificateStatistics incrementExceptional() {
        this.certificateStateStatistic.incrementExceptional();
        return this;
    }

    /**
     * Certificate statistics
     */
    @Getter
    @Builder
    public static class CertificateStatistics {
        private int certificatesWithStateValid;
        private int certificatesWithStateInvalid;
        private int certificatesWithStateUnknown;
        private int certificatesWithStateExceptional;

        private void incrementBy(final CertificateState certificateState) {
            switch (certificateState) {
                case VALID:
                    this.certificatesWithStateValid += 1;
                    break;
                case INVALID:
                    this.certificatesWithStateInvalid += 1;
                    break;
                case UNKNOWN:
                    this.certificatesWithStateUnknown += 1;
                    break;
                default:
                    break;
            }
        }

        private void incrementExceptional() {
            this.certificatesWithStateExceptional += 1;
        }
    }
}
