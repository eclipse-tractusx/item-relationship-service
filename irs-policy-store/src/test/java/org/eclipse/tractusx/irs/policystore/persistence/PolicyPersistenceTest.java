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
package org.eclipse.tractusx.irs.policystore.persistence;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.assertj.core.api.ThrowableAssert;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyPersistenceTest {

    private PolicyPersistence testee;
    private ObjectMapper mapper;

    @Mock
    private BlobPersistence mockPersistence;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        testee = new PolicyPersistence(mockPersistence, mapper);
    }

    @Nested
    class SavePolicyTests {
        @Test
        void save() throws BlobPersistenceException {
            // ARRANGE
            final var policy = policyBuilder(randomPolicyId()).build();

            // ACT
            testee.save("testBpn", policy);

            // ASSERT
            verify(mockPersistence).putBlob(anyString(), any());
        }

        @Test
        void saveWithoutBpn() throws BlobPersistenceException {
            // ARRANGE
            final var policy = policyBuilder(randomPolicyId()).build();

            // ACT
            testee.save("default", policy);

            // ASSERT
            verify(mockPersistence).putBlob(anyString(), any());
        }

        @Test
        void saveDuplicate() throws BlobPersistenceException, JsonProcessingException {
            // ARRANGE
            final String policyId = randomPolicyId();
            final var policy = policyBuilder(policyId).build();
            when(mockPersistence.getBlob(anyString())).thenReturn(
                    Optional.of(mapper.writeValueAsBytes(List.of(policy))));

            // ACT & ASSERT
            final ThrowableAssert.ThrowingCallable call = () -> testee.save("testBpn", policy);
            assertThatThrownBy(call).isInstanceOf(PolicyStoreException.class)
                                    .hasMessageContaining("'" + policyId + "'")
                                    .hasMessageContaining("already exists");
        }

        @Test
        void saveWithReadError() throws BlobPersistenceException {
            // ARRANGE
            when(mockPersistence.getBlob(any())).thenThrow(
                    new BlobPersistenceException("test exception", new IllegalStateException()));

            // ACT & ASSERT
            final ThrowableAssert.ThrowingCallable call = () -> testee.save("testBpn",
                    policyBuilder(randomPolicyId()).build());
            assertThatThrownBy(call).isInstanceOf(PolicyStoreException.class)
                                    .hasMessageContaining("Unable to read policy data");
        }

        @Test
        void saveWithWriteError() throws BlobPersistenceException {
            // ARRANGE
            final String policyId = randomPolicyId();
            doThrow(new BlobPersistenceException(policyId, new IllegalStateException())).when(mockPersistence)
                                                                                        .putBlob(any(), any());

            // ACT & ASSERT
            final ThrowableAssert.ThrowingCallable call = () -> testee.save("testBpn", policyBuilder(policyId).build());
            assertThatThrownBy(call).isInstanceOf(PolicyStoreException.class)
                                    .hasMessageContaining("Unable to store policy data");
        }
    }

    @Nested
    class DeletePolicyTests {
        @Test
        void delete_success() throws BlobPersistenceException, JsonProcessingException {
            // ARRANGE
            final String policyId =  randomPolicyId();
            final String policyId2 =  randomPolicyId();
            final var policy = policyBuilder(policyId).build();
            final var policy2 = policyBuilder(policyId2).build();
            when(mockPersistence.getBlob(anyString())).thenReturn(Optional.of(mapper.writeValueAsBytes(List.of(policy, policy2))));

            // ACT
            testee.delete("testBpn", policyId);

            // ASSERT
            verify(mockPersistence).putBlob(anyString(), any());
        }

        @Test
        void delete_whenNoOtherPolicyLeftForBpn() throws BlobPersistenceException, JsonProcessingException {
            // ARRANGE
            final String policyId = "policy1";
            final var policy = new Policy(policyId, OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
            when(mockPersistence.getBlob(anyString())).thenReturn(
                    Optional.of(mapper.writeValueAsBytes(List.of(policy))));

            // ACT
            testee.delete("testBpn", policyId);

            // ASSERT
            verify(mockPersistence).delete(anyString(), any());
        }

        @Test
        void delete_shouldThrowExceptionIfPolicyWithIdDoesntExists()
                throws BlobPersistenceException, JsonProcessingException {

            // ARRANGE

            final String policyId = randomPolicyId();
            final String notExistingPolicyId = randomPolicyId();

            when(mockPersistence.getBlob(anyString())).thenReturn(
                    Optional.of(mapper.writeValueAsBytes(List.of(policyBuilder(policyId).build()))));

            // ACT
            assertThrows(PolicyStoreException.class, () -> testee.delete("testBpn", notExistingPolicyId));
        }
    }

    @Nested
    class ReadPoliciesTests {

        @Test
        void readAll_withBpn() throws BlobPersistenceException, JsonProcessingException {

            // ARRANGE
            final String policyId = randomPolicyId();
            when(mockPersistence.getBlob(anyString())).thenReturn(
                    Optional.of(mapper.writeValueAsBytes(List.of(policyBuilder(policyId).build()))));

            // ACT
            final var readPolicies = testee.readAll("testBpn");

            // ASSERT
            assertThat(readPolicies).hasSize(1);
        }

        @Test
        void readAll_shouldReturnCorrect() throws BlobPersistenceException, JsonProcessingException {

            // ARRANGE
            final String policyId = randomPolicyId();
            final Map<String, byte[]> blobMap = new HashMap<>();
            blobMap.put("bpn1", mapper.writeValueAsBytes(List.of(policyBuilder(policyId).build())));
            blobMap.put("bpn2", mapper.writeValueAsBytes(emptyList()));
            when(mockPersistence.getAllBlobs()).thenReturn(blobMap);

            // ACT
            final var readPolicies = testee.readAll();

            // ASSERT
            assertThat(readPolicies).hasSize(2);
            assertThat(readPolicies.keySet()).containsExactlyInAnyOrder("bpn1", "bpn2");
            assertThat(readPolicies.get("bpn1")).isNotEmpty();
            assertThat(readPolicies.get("bpn2")).isEmpty();
        }

        @Test
        void readAll_withBpn_withError() throws BlobPersistenceException, JsonProcessingException {

            // ARRANGE
            final String policyId = randomPolicyId();
            final var mapperMock = mock(ObjectMapper.class);
            when(mockPersistence.getBlob(anyString())).thenReturn(
                    Optional.of(mapper.writeValueAsBytes(List.of(policyBuilder(policyId).build()))));
            when(mapperMock.readerForListOf(Policy.class)).thenThrow(new IllegalStateException());

            final var localTestee = new PolicyPersistence(mockPersistence, mapperMock);

            // ACT & ASSERT
            final ThrowableAssert.ThrowingCallable call = () -> localTestee.readAll("testBpn");
            assertThatThrownBy(call).isInstanceOf(PolicyStoreException.class)
                                    .hasMessageContaining("Could not read the policies from the store");
        }
    }

    private static Policy.PolicyBuilder policyBuilder(final String policyId) {
        return Policy.builder()
                     .policyId(policyId)
                     .createdOn(OffsetDateTime.now())
                     .validUntil(OffsetDateTime.now())
                     .permissions(emptyList());
    }

    private static String randomPolicyId() {
        return UUID.randomUUID().toString();
    }
}