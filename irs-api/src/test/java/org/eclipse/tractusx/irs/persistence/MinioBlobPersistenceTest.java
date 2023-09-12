/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinioBlobPersistenceTest {

    private MinioBlobPersistence testee;

    @Mock
    private MinioClient client;

    @BeforeEach
    void setUp() throws BlobPersistenceException {
        testee = new MinioBlobPersistence("test-bucket", client, 1);
    }

    @Test
    void shouldStoreBlobWithClient() throws Exception {
        // act
        testee.putBlob("testBlobName", "testContent".getBytes(StandardCharsets.UTF_8));

        // assert
        verify(client).putObject(any());
    }

    @Test
    void shouldDeleteBlobWithClient() throws Exception {
        // act
        testee.delete("testBlobName", List.of());

        // assert
        verify(client).removeObject(any());
    }

    @Test
    void shouldThrowCorrectExceptionWhenDeleting() throws Exception {
        // arrange
        doThrow(new IOException("Test")).when(client).removeObject(any());

        // act+assert
        assertThatThrownBy(() -> testee.delete("testBlobName", List.of())).isInstanceOf(BlobPersistenceException.class);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentBlob() throws Exception {
        // arrange
        final ErrorResponse errorResponse = new ErrorResponse("NoSuchKey", "", "", "", "", "", "");
        doThrow(new ErrorResponseException(errorResponse, null, "")).when(client).removeObject(any());

        // act
        final boolean success = testee.delete("testBlobName", List.of());

        // assert
        verify(client).removeObject(any());
        assertThat(success).isFalse();
    }

    @Test
    void shouldDeleteBlobWithRelatedBlobByProcessId() throws Exception {
        final List<String> processIds = List.of("testBlobName_process_1", "testBlobName_process_2");
        testee.putBlob(processIds.get(0), "testContent".getBytes(StandardCharsets.UTF_8));
        testee.putBlob(processIds.get(1), "testContent".getBytes(StandardCharsets.UTF_8));

        // act
        testee.delete("testBlobName", processIds);

        // assert
        verify(client, times(3)).removeObject(any());
    }

    @Test
    void shouldRetrieveBlobWithClient() throws Exception {
        // arrange
        byte[] blob = "TestData".getBytes(StandardCharsets.UTF_8);
        final GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.readAllBytes()).thenReturn(blob);
        when(client.getObject(any())).thenReturn(response);

        // act
        final Optional<byte[]> result = testee.getBlob("testBlobName");

        // assert
        assertThat(result).isPresent().get().isEqualTo(blob);
    }

    @Test
    void shouldThrowCorrectExceptionWhenRetrievingBlob() throws Exception {
        // arrange
        when(client.getObject(any())).thenThrow(new IOException("Test"));

        // act + assert
        assertThatThrownBy(() -> testee.getBlob("testBlobName")).isInstanceOf(BlobPersistenceException.class);
    }

    @Test
    void shouldThrowCorrectExceptionWhenWritingBlob() throws Exception {
        // arrange
        when(client.putObject(any())).thenThrow(new IOException("Test"));

        // act + assert
        assertThatThrownBy(() -> testee.putBlob("testBlobName", "test".getBytes(StandardCharsets.UTF_8))).isInstanceOf(
                BlobPersistenceException.class);
    }

}