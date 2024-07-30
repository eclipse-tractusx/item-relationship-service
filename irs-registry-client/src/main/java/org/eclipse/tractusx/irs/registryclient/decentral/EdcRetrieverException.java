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
package org.eclipse.tractusx.irs.registryclient.decentral;

import lombok.Getter;

/**
 * Thrown in case of error in EDC communication
 */
@Getter
public class EdcRetrieverException extends Exception {

    private String bpn;

    private String edcUrl;

    @Deprecated // TODO (mfischer) remove later, use the builder
    public EdcRetrieverException(final Throwable cause) {
        super(cause);
    }

    private EdcRetrieverException(final Builder builder) {
        super(builder.cause);
        this.bpn = builder.bpn;
        this.edcUrl = builder.edcUrl;
    }

    /**
     * Builder for {@link EdcRetrieverException}
     */
    public static class Builder {
        private final Throwable cause;
        private String bpn;
        private String edcUrl;

        public Builder(final Throwable cause) {
            this.cause = cause;
        }

        public Builder withBpn(final String bpn) {
            this.bpn = bpn;
            return this;
        }

        public Builder withEdcUrl(final String edcUrl) {
            this.edcUrl = edcUrl;
            return this;
        }

        public EdcRetrieverException build() {
            return new EdcRetrieverException(this);
        }
    }

}
