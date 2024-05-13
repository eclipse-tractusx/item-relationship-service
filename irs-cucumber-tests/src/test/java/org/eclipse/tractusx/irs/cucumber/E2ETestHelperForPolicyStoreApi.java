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
package org.eclipse.tractusx.irs.cucumber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelper.givenAuthentication;
import static org.eclipse.tractusx.irs.cucumber.E2ETestHelper.objectMapper;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.DataTableType;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.irs.cucumber.AuthenticationProperties.AuthenticationPropertiesBuilder;
import org.eclipse.tractusx.irs.cucumber.E2ETestHelperForPolicyStoreApi.CreatePolicyRequest.CreatePolicyRequestBuilder;
import org.springframework.http.HttpStatus;

/**
 * Helper class for Policy Store API tests.
 */
public class E2ETestHelperForPolicyStoreApi {

    public static final String URL_IRS_POLICIES = "/irs/policies";

    public static final String QUERYPARAM_BUSINESS_PARTNER_NUMBERS = "businessPartnerNumbers";

    public static String getPolicyTemplate() throws IOException {
        return E2ETestHelper.getTemplateFileContent("policy-for-e2e-tests.json");
    }

    public static String getPolicyTemplateWithoutDefinition() throws IOException {
        return E2ETestHelper.getTemplateFileContent("policy-for-e2e-tests-without-definition.json");
    }

    public static String getPolicyTemplateWithEmptyDefinition() throws IOException {
        return E2ETestHelper.getTemplateFileContent("policy-for-e2e-tests-with-empty-definition.json");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ArrayList<LinkedHashMap<String, ?>>> fetchPoliciesForBpn(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder, final String bpn) {

        return givenAuthentication(authenticationPropertiesBuilder).queryParam(QUERYPARAM_BUSINESS_PARTNER_NUMBERS, bpn)
                                                                   .when()
                                                                   .get(URL_IRS_POLICIES)
                                                                   .then()
                                                                   .statusCode(HttpStatus.OK.value())
                                                                   .extract()
                                                                   .body()
                                                                   .as(Map.class);
    }

    public static ValidatableResponse fetchAllPolicies(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder) {
        return givenAuthentication(authenticationPropertiesBuilder).when().get(URL_IRS_POLICIES).then();
    }

    @Builder
    public record CreatePolicyRequest(OffsetDateTime validUntil, String businessPartnerNumber, JsonNode payload) {
    }

    @Builder
    public record UpdatePolicyRequest(OffsetDateTime validUntil, List<String> businessPartnerNumbers,
                                      List<String> policyIds) {
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BpnToPolicyId {
        private String bpn;
        private String policyId;
    }

    @DataTableType
    public BpnToPolicyId bpnToPolicyEntryTransformer(final Map<String, String> row) {
        return new BpnToPolicyId(row.get("BPN"), row.get("policyId"));
    }

    public static HashMap<String, HashSet<String>> getExpectedBpnToPolicyIdsMapping(
            final List<BpnToPolicyId> bpnToPolicyIdTable) {

        final HashMap<String, HashSet<String>> expectedBpnToPolicyIdsMapping = new HashMap<>();
        bpnToPolicyIdTable.forEach(entry -> {

            HashSet<String> policyIds = expectedBpnToPolicyIdsMapping.get(entry.getBpn());
            if (policyIds == null) {
                policyIds = new HashSet<>();
            }

            policyIds.add(entry.getPolicyId());

            expectedBpnToPolicyIdsMapping.put(entry.getBpn(), policyIds);
        });
        return expectedBpnToPolicyIdsMapping;
    }

    public static Stream<String> extractPolicyIdsForBpn(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String bpn) {
        List<LinkedHashMap<String, ?>> policyIds = bpnToPoliciesMap.get(bpn);
        if (policyIds == null) {
            policyIds = new ArrayList<>();
        }
        return extractPolicyIds(policyIds.stream());
    }

    @SuppressWarnings("rawtypes")
    public static Stream<LinkedHashMap> extractPoliciesForBpn(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String bpn) {
        return extractPolicy(bpnToPoliciesMap.get(bpn).stream());
    }

    public static Stream<String> extractPolicyIdsStartingWith(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap, final String policyIdPrefix) {
        return extractPolicyIds(bpnToPoliciesMap).filter(policyId -> StringUtils.startsWith(policyId, policyIdPrefix));
    }

    public static Stream<String> extractPolicyIds(
            final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap) {
        return extractPolicyIds(bpnToPoliciesMap.values().stream().flatMap(Collection::stream));
    }

    private static Stream<String> extractPolicyIds(final Stream<LinkedHashMap<String, ?>> linkedHashMapStream) {
        return extractPolicy(linkedHashMapStream).map(v -> (String) v.get("policyId"));
    }

    @SuppressWarnings("rawtypes")
    private static Stream<LinkedHashMap> extractPolicy(final Stream<LinkedHashMap<String, ?>> linkedHashMapStream) {
        return extractPolicyPayloads(linkedHashMapStream).map(v -> (LinkedHashMap) v.get("policy"));
    }

    @SuppressWarnings("rawtypes")
    private static Stream<LinkedHashMap> extractPolicyPayloads(
            final Stream<LinkedHashMap<String, ?>> linkedHashMapStream) {
        return linkedHashMapStream.map(v -> (LinkedHashMap) v.get("payload"));
    }

    public static JsonNode jsonFromString(final ObjectMapper objectMapper, final String jsonObjectStr)
            throws JsonProcessingException {
        return objectMapper.readTree(jsonObjectStr);
    }

    public static ValidatableResponse updatePolicies(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder, final List<String> policyIds,
            final List<String> businessPartnerNumbers, final String validUntil) {

        final var updatePolicyRequest = UpdatePolicyRequest.builder()
                                                           .policyIds(policyIds)
                                                           .businessPartnerNumbers(businessPartnerNumbers)
                                                           .validUntil(OffsetDateTime.parse(validUntil))
                                                           .build();
        return givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                   .body(updatePolicyRequest)
                                                                   .when()
                                                                   .put(URL_IRS_POLICIES)
                                                                   .then();
    }

    public static ValidatableResponse registerPolicyForBpn(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder, final String policyJson,
            final String bpn, final String validUntil) {

        final CreatePolicyRequest createPolicyRequest;
        try {

            CreatePolicyRequestBuilder builder = CreatePolicyRequest.builder();

            if (validUntil != null) {
                builder = builder.validUntil(OffsetDateTime.parse(validUntil));
            }

            builder = builder.businessPartnerNumber(bpn);

            if (policyJson != null) {
                builder = builder.payload(E2ETestHelperForPolicyStoreApi.jsonFromString(objectMapper, policyJson));
            }

            createPolicyRequest = builder.build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return givenAuthentication(authenticationPropertiesBuilder).contentType(ContentType.JSON)
                                                                   .body(createPolicyRequest)
                                                                   .when()
                                                                   .post(URL_IRS_POLICIES)
                                                                   .then();

    }

    public static ValidatableResponse fetchPoliciesForBusinessPartnerNumbers(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder,
            final List<String> businessPartnerNumbers) {

        return givenAuthentication(authenticationPropertiesBuilder).queryParam(QUERYPARAM_BUSINESS_PARTNER_NUMBERS,
                businessPartnerNumbers).when().get(URL_IRS_POLICIES).then();
    }

    public static void cleanupPolicyIdsByPrefix(final AuthenticationPropertiesBuilder authenticationPropertiesBuilder,
            final String policyIdPrefix) {
        fetchPolicyIdsByPrefixSuccessfully(authenticationPropertiesBuilder, policyIdPrefix).forEach(
                policyId -> cleanupPolicy(authenticationPropertiesBuilder, policyId));
    }

    public static List<String> fetchPolicyIdsByPrefixSuccessfully(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder, final String policyIdPrefix) {
        final Map<String, ArrayList<LinkedHashMap<String, ?>>> bpnToPoliciesMap = E2ETestHelperForPolicyStoreApi.fetchAllPoliciesSuccessfully(
                authenticationPropertiesBuilder);
        return E2ETestHelperForPolicyStoreApi.extractPolicyIdsStartingWith(bpnToPoliciesMap, policyIdPrefix).toList();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, ArrayList<LinkedHashMap<String, ?>>> fetchAllPoliciesSuccessfully(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder) {
        final ValidatableResponse getAllPoliciesResponse = fetchAllPolicies(authenticationPropertiesBuilder);
        return getAllPoliciesResponse.statusCode(HttpStatus.OK.value()).extract().body().as(Map.class);
    }

    public static void cleanupPolicy(final AuthenticationPropertiesBuilder authenticationPropertiesBuilder,
            final String policyId) {

        final ValidatableResponse deleteResponse = deletePolicy(authenticationPropertiesBuilder, policyId);
        final int status = deleteResponse.extract().statusCode();

        assertThat(List.of(HttpStatus.OK.value(), HttpStatus.NOT_FOUND.value())).describedAs(
                "Should either return status 200 OK or 404 NOT_FOUND").contains(status);
    }

    public static ValidatableResponse deletePolicy(
            final AuthenticationPropertiesBuilder authenticationPropertiesBuilder, final String policyId) {
        return givenAuthentication(authenticationPropertiesBuilder).pathParam("policyId", policyId)
                                                                   .when()
                                                                   .delete(URL_IRS_POLICIES + "/{policyId}")
                                                                   .then();
    }

    @Data
    @NoArgsConstructor
    public static final class PolicyAttributes {
        private String policyId;
        private List<String> bpnls;
        private String validUntil;
    }
}
