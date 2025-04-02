package org.eclipse.tractusx.irs.testing.testdata;

import org.eclipse.tractusx.irs.testing.containers.AzuriteContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AzuriteContainerTest {

    private static AzuriteContainer azurite;

    @BeforeAll
    static void startContainer() {
        azurite = new AzuriteContainer();
        azurite.start();
    }

    @AfterAll
    static void stopContainer() {
        if (azurite != null) {
            azurite.stop();
        }
    }

    @Test
    void shouldExposeBlobEndpoint() {
        String endpoint = azurite.getBlobEndpoint();
        assertNotNull(endpoint);
        assertTrue(endpoint.startsWith("http://"));
    }

    @Test
    void shouldExposeQueueEndpoint() {
        String endpoint = azurite.getQueueEndpoint();
        assertNotNull(endpoint);
        assertTrue(endpoint.startsWith("http://"));
    }

    @Test
    void shouldExposeTableEndpoint() {
        String endpoint = azurite.getTableEndpoint();
        assertNotNull(endpoint);
        assertTrue(endpoint.startsWith("http://"));
    }

    @Test
    void shouldBuildValidConnectionString() {
        String connStr = azurite.getConnectionString();
        assertNotNull(connStr);
        assertTrue(connStr.contains("AccountName=devstoreaccount1"));
        assertTrue(connStr.contains("BlobEndpoint="));
    }
}
