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
package org.eclipse.tractusx.irs.edc.client.testutil;

import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_CATENAX_POLICY;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DCAT;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DCT;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_DSPACE;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_EDC_PARTICIPANT_ID;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_ODRL;
import static org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration.NAMESPACE_TRACTUSX;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsonp.JSONPModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.edc.catalog.spi.Catalog;
import org.eclipse.edc.catalog.spi.DataService;
import org.eclipse.edc.catalog.spi.Dataset;
import org.eclipse.edc.catalog.spi.Distribution;
import org.eclipse.edc.core.transform.TypeTransformerRegistryImpl;
import org.eclipse.edc.jsonld.TitaniumJsonLd;
import org.eclipse.edc.policy.model.Action;
import org.eclipse.edc.policy.model.AndConstraint;
import org.eclipse.edc.policy.model.AtomicConstraint;
import org.eclipse.edc.policy.model.Constraint;
import org.eclipse.edc.policy.model.LiteralExpression;
import org.eclipse.edc.policy.model.Operator;
import org.eclipse.edc.policy.model.OrConstraint;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.XoneConstraint;
import org.eclipse.edc.spi.monitor.ConsoleMonitor;
import org.eclipse.edc.spi.types.domain.edr.EndpointDataReference;
import org.eclipse.tractusx.irs.data.StringMapper;
import org.eclipse.tractusx.irs.edc.client.configuration.JsonLdConfiguration;
import org.eclipse.tractusx.irs.edc.client.model.EDRAuthCode;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyType;
import org.eclipse.tractusx.irs.edc.client.transformer.EdcTransformer;
import org.jetbrains.annotations.NotNull;

public class TestMother {

    public static EdcTransformer createEdcTransformer() {
        final TitaniumJsonLd titaniumJsonLd = new TitaniumJsonLd(new ConsoleMonitor());
        titaniumJsonLd.registerNamespace("odrl", NAMESPACE_ODRL);
        titaniumJsonLd.registerNamespace("dct", NAMESPACE_DCT);
        titaniumJsonLd.registerNamespace("tx", NAMESPACE_TRACTUSX);
        titaniumJsonLd.registerNamespace("edc", NAMESPACE_EDC);
        titaniumJsonLd.registerNamespace("dcat", NAMESPACE_DCAT);
        titaniumJsonLd.registerNamespace("dspace", NAMESPACE_DSPACE);
        titaniumJsonLd.registerNamespace("cx-policy", NAMESPACE_CATENAX_POLICY);
        return new EdcTransformer(objectMapper(), titaniumJsonLd, new TypeTransformerRegistryImpl());
    }

    public static ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JSONPModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerSubtypes(AtomicConstraint.class, LiteralExpression.class);
        return objectMapper;
    }

    public static Catalog createCatalog(final String assetId, final int numberOfOffers) {
        final Policy policy = mock(Policy.class);
        final Distribution distribution = Distribution.Builder.newInstance()
                                                              .format("HttpProxy")
                                                              .dataService(new DataService())
                                                              .build();

        final List<Dataset> datasets = IntStream.range(0, numberOfOffers)
                                                .boxed()
                                                .map(i -> Dataset.Builder.newInstance()
                                                                         .id(assetId + i)
                                                                         .offer(getOfferId(assetId + i), policy)
                                                                         .distribution(distribution)
                                                                         .build())
                                                .toList();
        return Catalog.Builder.newInstance()
                              .datasets(datasets)
                              .properties(Map.of(NAMESPACE_EDC_PARTICIPANT_ID, "BPNTEST"))
                              .build();
    }

    public static Catalog createCatalog(final String assetId, final Policy policy, final String bpn,
            final String offerId) {
        final Distribution distribution = Distribution.Builder.newInstance()
                                                              .format("HttpProxy")
                                                              .dataService(new DataService())
                                                              .build();

        final Dataset dataset = Dataset.Builder.newInstance()
                                                .id(assetId)
                                                .offer(offerId, policy)
                                                .distribution(distribution)
                                                .build();
        return Catalog.Builder.newInstance()
                              .dataset(dataset)
                              .properties(Map.of(NAMESPACE_EDC_PARTICIPANT_ID, bpn))
                              .build();
    }

    @NotNull
    private static String getOfferId(final String assetId) {
        return UUID.randomUUID() + ":" + assetId + ":" + UUID.randomUUID();
    }

    private static Permission createUsePermission(final Constraint constraint) {
        return Permission.Builder.newInstance()
                                 .action(Action.Builder.newInstance().type(PolicyType.USE.getValue()).build())
                                 .constraint(constraint)
                                 .build();
    }

    public static AtomicConstraint createAtomicConstraint(final String leftExpr, final String rightExpr) {
        return AtomicConstraint.Builder.newInstance()
                                       .leftExpression(new LiteralExpression(leftExpr))
                                       .rightExpression(new LiteralExpression(rightExpr))
                                       .operator(Operator.EQ)
                                       .build();
    }

    public static Policy createAtomicConstraintPolicy(final String leftExpr, final String rightExpr) {
        final AtomicConstraint constraint = createAtomicConstraint(leftExpr, rightExpr);
        final Permission permission = createUsePermission(constraint);
        return Policy.Builder.newInstance().permission(permission).build();
    }

    public static Policy createAndConstraintPolicy(final List<Constraint> constraints) {
        final AndConstraint andConstraint = AndConstraint.Builder.newInstance().constraints(constraints).build();
        final Permission permission = createUsePermission(andConstraint);
        return Policy.Builder.newInstance().permission(permission).build();
    }

    public static Policy createOrConstraintPolicy(final List<Constraint> constraints) {
        final OrConstraint orConstraint = OrConstraint.Builder.newInstance().constraints(constraints).build();
        final Permission permission = createUsePermission(orConstraint);
        return Policy.Builder.newInstance().permission(permission).build();
    }

    public static Policy createXOneConstraintPolicy(final List<Constraint> constraints) {
        final XoneConstraint orConstraint = XoneConstraint.Builder.newInstance().constraints(constraints).build();
        final Permission permission = createUsePermission(orConstraint);
        return Policy.Builder.newInstance().permission(permission).build();
    }

    public static EndpointDataReference endpointDataReference(final String contractAgreementId) {
        return EndpointDataReference.Builder.newInstance()
                                            .authKey("testkey")
                                            .authCode(edrAuthCode(contractAgreementId))
                                            .properties(
                                                    Map.of(JsonLdConfiguration.NAMESPACE_EDC_CID, contractAgreementId))
                                            .endpoint("http://provider.dataplane/api/public")
                                            .id("testid")
                                            .contractId(contractAgreementId)
                                            .build();
    }

    public static String edrAuthCode(final String contractAgreementId) {
        final EDRAuthCode edrAuthCode = EDRAuthCode.builder()
                                                   .cid(contractAgreementId)
                                                   .dad("test")
                                                   .exp(9999999999L)
                                                   .build();
        final String b64EncodedAuthCode = Base64.getUrlEncoder()
                                                .encodeToString(StringMapper.mapToString(edrAuthCode)
                                                                            .getBytes(StandardCharsets.UTF_8));
        return "eyJhbGciOiJSUzI1NiJ9." + b64EncodedAuthCode + ".test";
    }
}
