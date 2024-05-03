/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.edc.client.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.edc.policy.model.Duty;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.Prohibition;

/**
 * Represents a contract offer like it is used in a EDC {@link NegotiationRequest}.
 */
@Value
@Builder
@Jacksonized
public class ContractOffer {
    public static final String ID_PROPERTY = "@id";
    public static final String TYPE_PROPERTY = "@type";
    public static final String ODRL_NAMESPACE = "http://www.w3.org/ns/odrl/2/";
    public static final String ODRL_PERMISSION_ATTRIBUTE = ODRL_NAMESPACE + "permission";
    public static final String ODRL_PROHIBITION_ATTRIBUTE = ODRL_NAMESPACE + "prohibition";
    public static final String ODRL_OBLIGATION_ATTRIBUTE = ODRL_NAMESPACE + "obligation";
    public static final String ODRL_ASSIGNER_ATTRIBUTE = ODRL_NAMESPACE + "assigner";
    public static final String ODRL_TARGET_ATTRIBUTE = ODRL_NAMESPACE + "target";
    public static final String ODRL_ASSIGNEE_ATTRIBUTE = ODRL_NAMESPACE + "assignee";
    public static final String ODRL_POLICY_TYPE_OFFER = ODRL_NAMESPACE + "Offer";

    @JsonProperty(TYPE_PROPERTY)
    /* default */ String type = ODRL_POLICY_TYPE_OFFER;
    @JsonProperty(ID_PROPERTY)
    /* default */ String offerId;
    @Singular
    @JsonProperty(ODRL_PERMISSION_ATTRIBUTE)
    /* default */ List<Permission> permissions;
    @Singular
    @JsonProperty(ODRL_PROHIBITION_ATTRIBUTE)
    /* default */ List<Prohibition> prohibitions;
    @Singular
    @JsonProperty(ODRL_OBLIGATION_ATTRIBUTE)
    /* default */ List<Duty> obligations;
    @JsonProperty(ODRL_ASSIGNER_ATTRIBUTE)
    /* default */ String assigner;
    @JsonProperty(ODRL_ASSIGNEE_ATTRIBUTE)
    /* default */ String assignee;
    @JsonProperty(ODRL_TARGET_ATTRIBUTE)
    /* default */ String target;

    public static ContractOffer fromPolicy(final Policy policy, final String offerId, final String target,
            final String assigner) {
        return ContractOffer.builder()
                            .prohibitions(policy.getProhibitions())
                            .obligations(policy.getObligations())
                            .permissions(policy.getPermissions())
                            .offerId(offerId)
                            .target(target)
                            .assigner(assigner)
                            .build();
    }
}
