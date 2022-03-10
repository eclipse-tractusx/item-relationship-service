package org.eclipse.dataspaceconnector.transfer.core.transfer;

import com.github.javafaker.Faker;
import org.eclipse.dataspaceconnector.junit.launcher.EdcExtension;
import org.eclipse.dataspaceconnector.spi.transfer.TransferWaitStrategy;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;
import org.eclipse.dataspaceconnector.spi.types.domain.metadata.DataEntry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.DataRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.StatusChecker;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.StatusCheckerRegistry;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Properties;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.easymock.EasyMock.*;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess.Type.CONSUMER;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates.ERROR;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates.IN_PROGRESS;

@ExtendWith(EdcExtension.class)
public class TransferProcessWatchdogIntegrationTest {

    Faker faker = new Faker();

    @BeforeAll
    public static void setProperties() {
        Properties props = System.getProperties();
        // watchdog polling interval of 100ms with process timeout of 100ms
        props.setProperty("edc.watchdog.timeout.seconds", "0.1");
        props.setProperty("edc.watchdog.interval.seconds", "0.1");
    }

    @BeforeEach
    protected void before(EdcExtension extension) {
        // register a wait strategy of 1ms to speed up the interval between transfer manager iterations
        extension.registerServiceMock(TransferWaitStrategy.class, () -> 1);
    }

    @Test
    public void cancelLongRunningProcess(TransferProcessStore transferProcessStore, StatusCheckerRegistry statusCheckerRegistry) throws InterruptedException {
        // Arrange
        DataRequest request = DataRequest.Builder.newInstance()
                .protocol(faker.lorem().word())
                .dataEntry(DataEntry.Builder.newInstance().id(faker.lorem().characters()).build())
                .connectorId(faker.lorem().characters())
                .connectorAddress(faker.internet().url())
                .destinationType(faker.lorem().word())
                .managedResources(false)
                .build();

        StatusChecker statusChecker = mock(StatusChecker.class);
        expect(statusChecker.isComplete(anyObject(), anyObject())).andReturn(false);
        replay(statusChecker);
        statusCheckerRegistry.register(request.getDestinationType(), statusChecker);

        var process = TransferProcess.Builder.newInstance()
                .id(faker.lorem().characters())
                .dataRequest(request)
                .type(CONSUMER)
                .state(IN_PROGRESS.code())
                .build();

        // Act
        transferProcessStore.update(process);

        // Assert
        await()
            .atMost(1, SECONDS)
            .untilAsserted(() -> {
                assertThat(transferProcessStore.find(process.getId())).usingRecursiveComparison()
                        .ignoringFields("stateTimestamp", "stateCount")
                        .isEqualTo(TransferProcess.Builder.newInstance()
                                .id(process.getId())
                                .dataRequest(request)
                                .type(CONSUMER)
                                .state(ERROR.code())
                                .errorDetail("Timed out waiting for process to complete after > 100ms")
                                .build());
        });
    }
}
