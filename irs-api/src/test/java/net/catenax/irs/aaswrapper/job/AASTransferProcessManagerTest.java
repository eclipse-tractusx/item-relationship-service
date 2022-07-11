package net.catenax.irs.aaswrapper.job;

import static net.catenax.irs.util.TestMother.jobParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import io.github.resilience4j.retry.RetryRegistry;
import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.component.Tombstone;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import net.catenax.irs.util.TestMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AASTransferProcessManagerTest {

    private final TestMother generate = new TestMother();

    DigitalTwinRegistryFacade digitalTwinRegistryFacade = mock(DigitalTwinRegistryFacade.class);

    SubmodelFacade submodelFacade = mock(SubmodelFacade.class);

    ExecutorService pool = mock(ExecutorService.class);

    AASHandler aasHandler = new AASHandler(digitalTwinRegistryFacade, submodelFacade);

    final AASTransferProcessManager manager = new AASTransferProcessManager(aasHandler, pool, new InMemoryBlobStore());

    @Test
    void shouldExecuteThreadForProcessing() {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(UUID.randomUUID().toString());

        // when
        manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, jobParameter());

        // then
        verify(pool, times(1)).execute(any(Runnable.class));
    }

    @Test
    void shouldInitiateProcessingAndReturnOkStatus() {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(UUID.randomUUID().toString());

        // when
        final TransferInitiateResponse initiateResponse = manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, jobParameter());

        // then
        assertThat(initiateResponse.getTransferId()).isNotBlank();
        assertThat(initiateResponse.getStatus()).isEqualTo(ResponseStatus.OK);
    }

    @Test
    void fromTombstoneTest() {
        // arrange
        String catenaXId = "5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99";
        String endPointUrl = "http://localhost/dummy/interfaceinformation/urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        //act
        Tombstone tombstone = Tombstone.from(catenaXId, endPointUrl, new IllegalArgumentException("Error detail"),
                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts());

        // assert
        assertThat(tombstone.getProcessingError().getErrorDetail()).isEqualTo("Error detail");
        assertThat(tombstone.getEndpointURL()).isEqualTo(endPointUrl);
        assertThat(tombstone.getProcessingError().getRetryCounter()).isEqualTo(
                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts());
    }

}
