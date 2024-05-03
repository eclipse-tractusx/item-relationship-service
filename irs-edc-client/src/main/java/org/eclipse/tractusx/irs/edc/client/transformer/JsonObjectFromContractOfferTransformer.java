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
package org.eclipse.tractusx.irs.edc.client.transformer;

import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.ID_PROPERTY;
import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.ODRL_ASSIGNEE_ATTRIBUTE;
import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.ODRL_ASSIGNER_ATTRIBUTE;
import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.ODRL_OBLIGATION_ATTRIBUTE;
import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.ODRL_PERMISSION_ATTRIBUTE;
import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.ODRL_PROHIBITION_ATTRIBUTE;
import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.ODRL_TARGET_ATTRIBUTE;
import static org.eclipse.tractusx.irs.edc.client.model.ContractOffer.TYPE;

import java.util.Optional;

import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import org.eclipse.edc.jsonld.spi.transformer.AbstractJsonLdTransformer;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.agent.ParticipantIdMapper;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.tractusx.irs.edc.client.model.ContractOffer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts from a {@link ContractOffer} to a DCAT catalog as a {@link JsonObject} in JSON-LD expanded form.
 */
@SuppressWarnings("PMD.TooManyStaticImports")
public class JsonObjectFromContractOfferTransformer extends AbstractJsonLdTransformer<ContractOffer, JsonObject> {

    private final ParticipantIdMapper participantIdMapper;
    private final JsonBuilderFactory jsonFactory;

    public JsonObjectFromContractOfferTransformer(final ParticipantIdMapper participantIdMapper,
            final JsonBuilderFactory jsonFactory) {
        super(ContractOffer.class, JsonObject.class);
        this.participantIdMapper = participantIdMapper;
        this.jsonFactory = jsonFactory;
    }

    @Override
    public @Nullable JsonObject transform(final @NotNull ContractOffer contractOffer,
            final @NotNull TransformerContext context) {

        final Policy policy = Policy.Builder.newInstance()
                                            .duties(contractOffer.getObligations())
                                            .prohibitions(contractOffer.getProhibitions())
                                            .permissions(contractOffer.getPermissions())
                                            .build();
        final JsonObject transform = context.transform(policy, JsonObject.class);
        final var builder = jsonFactory.createObjectBuilder()
                                       .add(ID_PROPERTY, contractOffer.getOfferId())
                                       .add(TYPE, contractOffer.getType());

        if (Optional.ofNullable(transform).isPresent()) {
            builder.add(ODRL_PERMISSION_ATTRIBUTE, transform.getJsonArray(ODRL_PERMISSION_ATTRIBUTE))
                   .add(ODRL_PROHIBITION_ATTRIBUTE, transform.getJsonArray(ODRL_PROHIBITION_ATTRIBUTE))
                   .add(ODRL_OBLIGATION_ATTRIBUTE, transform.getJsonArray(ODRL_OBLIGATION_ATTRIBUTE));
        }

        addIfPresent(Optional.ofNullable(contractOffer.getAssignee()).map(participantIdMapper::toIri), builder,
                ODRL_ASSIGNEE_ATTRIBUTE);
        addIfPresent(Optional.ofNullable(contractOffer.getAssigner()).map(participantIdMapper::toIri), builder,
                ODRL_ASSIGNER_ATTRIBUTE);
        addIfPresent(Optional.ofNullable(contractOffer.getTarget()), builder, ODRL_TARGET_ATTRIBUTE);

        return builder.build();
    }

    private void addIfPresent(final Optional<String> property, final JsonObjectBuilder builder,
            final String attribute) {
        property.ifPresent(target -> builder.add(attribute,
                jsonFactory.createArrayBuilder().add(jsonFactory.createObjectBuilder().add(ID_PROPERTY, target))));
    }

}
