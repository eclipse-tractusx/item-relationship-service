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
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.edc.client.policy.Policy;
import org.eclipse.tractusx.irs.policystore.exceptions.PolicyStoreException;
import org.junit.jupiter.api.BeforeEach;
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

    @Test
    void save() throws BlobPersistenceException {
        // arrange
        final var policy = new Policy("test", OffsetDateTime.now(), OffsetDateTime.now(), emptyList());

        // act
        testee.save(List.of("testBpn"), policy);

        // assert
        verify(mockPersistence).putBlob(anyString(), any());
    }

    @Test
    void saveDuplicate() throws BlobPersistenceException, JsonProcessingException {
        // arrange
        final var policy = new Policy("test", OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
        final var policies = List.of(policy);
        when(mockPersistence.getBlob(anyString())).thenReturn(Optional.of(mapper.writeValueAsBytes(policies)));

        // act & assert
        assertThatThrownBy(() -> testee.save(List.of("testBpn"), policy)).isInstanceOf(PolicyStoreException.class);
    }

    @Test
    void saveWithError() throws BlobPersistenceException {
        // arrange
        final var policy = new Policy("test", OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
        when(mockPersistence.getBlob(any())).thenThrow(
                new BlobPersistenceException("test", new IllegalStateException()));

        // act & assert
        assertThatThrownBy(() -> testee.save(List.of("testBpn"), policy)).isInstanceOf(PolicyStoreException.class);
    }

    @Test
    void saveWithWriteError() throws BlobPersistenceException {
        // arrange
        final var policy = new Policy("test", OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
        doThrow(new BlobPersistenceException("test", new IllegalStateException())).when(mockPersistence)
                                                                                  .putBlob(any(), any());

        // act & assert
        assertThatThrownBy(() -> testee.save(List.of("testBpn"), policy)).isInstanceOf(PolicyStoreException.class);
    }

    @Test
    void delete() throws BlobPersistenceException, JsonProcessingException {
        // arrange
        final String policyId = "test";
        final var policy = new Policy(policyId, OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
        final var policies = List.of(policy);
        when(mockPersistence.getBlob(anyString())).thenReturn(Optional.of(mapper.writeValueAsBytes(policies)));

        // act
        testee.delete("testBpn", policyId);

        // assert
        verify(mockPersistence).putBlob(anyString(), any());
    }

    @Test
    void deleteShouldThrowExceptionIfPolicyWithIdDoesntExists() throws BlobPersistenceException, JsonProcessingException {
        // arrange
        final var policy = new Policy("policyId", OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
        when(mockPersistence.getBlob(anyString())).thenReturn(Optional.of(mapper.writeValueAsBytes(List.of(policy))));

        // act
        assertThrows(PolicyStoreException.class, () -> testee.delete("testBpn", "notExistingPolicyId"));
    }

    @Test
    void readAll() throws BlobPersistenceException, JsonProcessingException {
        // arrange
        final var policy = new Policy("test", OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
        final var policies = List.of(policy);
        when(mockPersistence.getBlob(anyString())).thenReturn(Optional.of(mapper.writeValueAsBytes(policies)));

        // act
        final var readPolicies = testee.readAll("testBpn");

        // assert
        assertThat(readPolicies).hasSize(1);
    }

    @Test
    void readAllWithError() throws BlobPersistenceException, JsonProcessingException {
        // arrange
        final var policy = new Policy("test", OffsetDateTime.now(), OffsetDateTime.now(), emptyList());
        final var policies = List.of(policy);
        final var mapperMock = mock(ObjectMapper.class);
        when(mockPersistence.getBlob(anyString())).thenReturn(Optional.of(mapper.writeValueAsBytes(policies)));
        when(mapperMock.readerForListOf(Policy.class)).thenThrow(new IllegalStateException());

        final var localTestee = new PolicyPersistence(mockPersistence, mapperMock);

        // act & assert
        assertThatThrownBy(() -> localTestee.readAll("testBpn")).isInstanceOf(PolicyStoreException.class);
    }
}