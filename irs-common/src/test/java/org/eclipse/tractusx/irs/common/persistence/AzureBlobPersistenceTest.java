/********************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.common.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class AzureBlobPersistenceTest {
    private static final String CONTAINER_NAME = "test-container";

    @Mock private BlobServiceClient mockBlobServiceClient;
    @Mock private BlobContainerClient mockContainerClient;
    @Mock private BlobClient mockBlobClient;

    private AzureBlobPersistence azureBlobPersistence;

    @BeforeEach
    void setUp() {
        // Mock BlobServiceClient behavior
        when(mockBlobServiceClient.getBlobContainerClient(CONTAINER_NAME)).thenReturn(mockContainerClient);
        when(mockContainerClient.exists()).thenReturn(true);

        // Inject the mocked BlobServiceClient into AzureBlobPersistence
        azureBlobPersistence = new AzureBlobPersistence(mockBlobServiceClient, CONTAINER_NAME);
    }

    @Test
    void shouldStoreBlob() {
        try {
            when(mockContainerClient.getBlobClient("testBlob.txt")).thenReturn(mockBlobClient);
            doNothing().when(mockBlobClient).upload(any(ByteArrayInputStream.class), anyLong(), anyBoolean());

            azureBlobPersistence.putBlob("testBlob.txt", "Hello, Azure!".getBytes());

            verify(mockBlobClient, times(1)).upload(any(ByteArrayInputStream.class), anyLong(), anyBoolean());
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void shouldGetBlob() {
        try {
            when(mockContainerClient.getBlobClient("testBlob.txt")).thenReturn(mockBlobClient);
            when(mockBlobClient.exists()).thenReturn(true);

            doAnswer(invocation -> {
                ByteArrayOutputStream outputStream = (ByteArrayOutputStream) invocation.getArgument(0);
                outputStream.write("Hello, Azure!".getBytes());
                return null;
            }).when(mockBlobClient).downloadStream(any(ByteArrayOutputStream.class));

            Optional<byte[]> result = azureBlobPersistence.getBlob("testBlob.txt");

            assertTrue(result.isPresent());
            assertEquals("Hello, Azure!", new String(result.get()));
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void shouldGetBlobNotFound() {
        try {
            when(mockContainerClient.getBlobClient("nonexistentBlob.txt")).thenReturn(mockBlobClient);
            when(mockBlobClient.exists()).thenReturn(false);

            Optional<byte[]> result = azureBlobPersistence.getBlob("nonexistentBlob.txt");

            assertFalse(result.isPresent());
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void shouldGetAllBlobs() {
        try {
            // Mocking blob items
            BlobItem blobItem1 = mock(BlobItem.class);
            BlobItem blobItem2 = mock(BlobItem.class);
            when(blobItem1.getName()).thenReturn("blob1.txt");
            when(blobItem2.getName()).thenReturn("blob2.txt");

            // Mock listBlobs() to return a PagedIterable
            PagedIterable<BlobItem> pagedIterableMock = mock(PagedIterable.class);
            when(pagedIterableMock.iterator()).thenReturn(Arrays.asList(blobItem1, blobItem2).iterator());
            when(mockContainerClient.listBlobs()).thenReturn(pagedIterableMock);

            // Mock BlobClient calls
            when(mockContainerClient.getBlobClient("blob1.txt")).thenReturn(mockBlobClient);
            when(mockContainerClient.getBlobClient("blob2.txt")).thenReturn(mockBlobClient);
            when(mockBlobClient.exists()).thenReturn(true);
            doAnswer(invocation -> {
                ByteArrayOutputStream outputStream = invocation.getArgument(0);
                outputStream.write("Data1".getBytes());
                return null;
            }).when(mockBlobClient).downloadStream(any(ByteArrayOutputStream.class));

            Map<String, byte[]> blobs = azureBlobPersistence.getAllBlobs();

            assertEquals(2, blobs.size());
            assertArrayEquals("Data1".getBytes(), blobs.get("blob1.txt"));
            assertArrayEquals("Data1".getBytes(), blobs.get("blob2.txt"));

        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void shouldGetBlobByPrefix() {
        try {
            // Mock BlobItems
            BlobItem blobItem1 = mock(BlobItem.class);
            BlobItem blobItem2 = mock(BlobItem.class);
            when(blobItem1.getName()).thenReturn("test-blob1.txt");
            when(blobItem2.getName()).thenReturn("test-blob2.txt");

            // Mock listBlobs() to return a PagedIterable
            PagedIterable<BlobItem> pagedIterableMock = mock(PagedIterable.class);
            when(pagedIterableMock.stream()).thenReturn(Stream.of(blobItem1, blobItem2));
            when(mockContainerClient.listBlobs()).thenReturn(pagedIterableMock);

            // Mock individual BlobClients
            BlobClient mockBlobClient1 = mock(BlobClient.class);
            BlobClient mockBlobClient2 = mock(BlobClient.class);

            when(mockContainerClient.getBlobClient("test-blob1.txt")).thenReturn(mockBlobClient1);
            when(mockContainerClient.getBlobClient("test-blob2.txt")).thenReturn(mockBlobClient2);

            when(mockBlobClient1.exists()).thenReturn(true);
            when(mockBlobClient2.exists()).thenReturn(true);

            // Mock downloadStream behavior
            doAnswer(invocation -> {
                ByteArrayOutputStream outputStream = invocation.getArgument(0);
                outputStream.write("BlobData1".getBytes());
                return null;
            }).when(mockBlobClient1).downloadStream(any(ByteArrayOutputStream.class));

            doAnswer(invocation -> {
                ByteArrayOutputStream outputStream = invocation.getArgument(0);
                outputStream.write("BlobData2".getBytes());
                return null;
            }).when(mockBlobClient2).downloadStream(any(ByteArrayOutputStream.class));

            // Call the method under test
            Collection<byte[]> result = azureBlobPersistence.findBlobByPrefix("test-");

            // Assertions
            assertEquals(2, result.size());
            List<String> blobContents = result.stream().map(String::new).toList();

            assertTrue(blobContents.contains("BlobData1"));
            assertTrue(blobContents.contains("BlobData2"));

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    void shouldDeleteBlob() {
        try {
            when(mockContainerClient.getBlobClient("testBlob.txt")).thenReturn(mockBlobClient);
            when(mockBlobClient.exists()).thenReturn(true);
            doNothing().when(mockBlobClient).delete();

            boolean result = azureBlobPersistence.delete("testBlob.txt", List.of("process1"));

            assertTrue(result);
            verify(mockBlobClient, times(1)).delete();
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void shouldDeleteBlobNotFound() {
        try {
            when(mockContainerClient.getBlobClient("nonexistentBlob.txt")).thenReturn(mockBlobClient);
            when(mockBlobClient.exists()).thenReturn(false);

            boolean result = azureBlobPersistence.delete("nonexistentBlob.txt", List.of("process1"));

            assertFalse(result);
            verify(mockBlobClient, never()).delete();
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }
}
