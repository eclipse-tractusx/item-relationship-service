package net.catenax.irs.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.ErrorResponse;
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
        testee = new MinioBlobPersistence("test-bucket", client);
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
        testee.delete("testBlobName");

        // assert
        verify(client).removeObject(any());
    }

    @Test
    void shouldThrowCorrectExceptionWhenDeleting() throws Exception {
        // arrange
        doThrow(new IOException("Test")).when(client).removeObject(any());

        // act+assert
        assertThatThrownBy(() -> testee.delete("testBlobName")).isInstanceOf(BlobPersistenceException.class);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonexistentBlob() throws Exception {
        // arrange
        final ErrorResponse errorResponse = new ErrorResponse("NoSuchKey", "", "", "", "", "", "");
        doThrow(new ErrorResponseException(errorResponse, null, "")).when(client).removeObject(any());

        // act
        final boolean success = testee.delete("testBlobName");

        // assert
        verify(client).removeObject(any());
        assertThat(success).isFalse();
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