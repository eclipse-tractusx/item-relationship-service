package org.eclipse.dataspaceconnector.transfer.core.transfer;

import com.github.javafaker.Faker;
import org.easymock.Capture;
import org.easymock.EasyMockExtension;
import org.easymock.Mock;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.transfer.store.TransferProcessStore;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.*;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess.Type.CONSUMER;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess.Type.PROVIDER;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates.ERROR;
import static org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcessStates.IN_PROGRESS;

@ExtendWith(EasyMockExtension.class)
class CancelLongRunningProcessesTest {

    private static final Duration STATE_TIMEOUT_MS = Duration.of(20, ChronoUnit.SECONDS);
    private static final int BATCH_SIZE = 5;
    private static final Clock CLOCK = Clock.fixed(Instant.now(), ZoneOffset.UTC);
    private static final Date TIMEOUT_DATE = Date.from(now(CLOCK).minus(STATE_TIMEOUT_MS));

    Faker faker = new Faker();

    @Mock
    Monitor monitor;
    @Mock
    TransferProcessStore transferProcessStore;

    Capture<TransferProcess> transferProcessCaptor = Capture.newInstance();

    CancelLongRunningProcesses sut;

    @BeforeEach
    public void setup() {
        sut = CancelLongRunningProcesses.builder()
                .monitor(monitor)
                .transferProcessStore(transferProcessStore)
                .batchSize(BATCH_SIZE)
                .stateTimeout(STATE_TIMEOUT_MS)
                .clock(CLOCK)
                .build();
    }

    @Test
    public void run_shouldCancelProcessInTimeout() {
        // Arrange
        var activeProcessNotInTimeout = createTransferProcess(CONSUMER, faker.date().future(1, TimeUnit.SECONDS, TIMEOUT_DATE));
        var activeProcessInTimeout =  createTransferProcess(CONSUMER, faker.date().past(1, TimeUnit.SECONDS, TIMEOUT_DATE));

        expect(transferProcessStore.nextForState(IN_PROGRESS.code(), BATCH_SIZE)).andReturn(List.of(activeProcessNotInTimeout, activeProcessInTimeout));
        transferProcessStore.update(capture(transferProcessCaptor));
        expectLastCall();
        replay(transferProcessStore);

        // Act
        sut.run();

        // Assert
        verify(transferProcessStore);
        assertThat(transferProcessCaptor.getValue())
                .usingRecursiveComparison()
                .ignoringFields("stateCount", "stateTimestamp")
                .isEqualTo(
                    TransferProcess.Builder.newInstance()
                        .id(activeProcessInTimeout.getId())
                        .state(ERROR.code())
                        .errorDetail("Timed out waiting for process to complete after > " + STATE_TIMEOUT_MS.toMillis() + "ms")
                        .build()
                );
    }

    @Test
    public void run_shouldNotProcessProviderProcessesInTimeout() {
        // Arrange
        var providerProcessInTimeout =  createTransferProcess(PROVIDER, faker.date().past(1, TimeUnit.SECONDS, TIMEOUT_DATE));

        expect(transferProcessStore.nextForState(IN_PROGRESS.code(), BATCH_SIZE)).andReturn(List.of(providerProcessInTimeout));
        replay(transferProcessStore);

        // Act
        sut.run();

        // Assert
        verify(transferProcessStore);
    }

    private TransferProcess createTransferProcess(TransferProcess.Type type, Date stateTimestamp) {
        return TransferProcess.Builder.newInstance()
                .id(faker.lorem().characters())
                .stateTimestamp(stateTimestamp.getTime())
                .type(type)
                .build();
    }
}