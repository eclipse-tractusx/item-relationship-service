package net.catenax.irs.aaswrapper.job;

import static net.catenax.irs.dtos.IrsCommonConstants.LIFE_CYCLE_CONTEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import net.catenax.irs.InMemoryBlobStore;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.connector.job.ResponseStatus;
import net.catenax.irs.connector.job.TransferInitiateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AASTransferProcessManagerTest {

    DigitalTwinRegistryFacade digitalTwinRegistryFacade = mock(DigitalTwinRegistryFacade.class);

    SubmodelFacade submodelFacade = mock(SubmodelFacade.class);

    ExecutorService pool = mock(ExecutorService.class);

    final AASTransferProcessManager manager = new AASTransferProcessManager(digitalTwinRegistryFacade, submodelFacade,
            pool, new InMemoryBlobStore());

    @Test
    void shouldExecuteThreadForProcessing() {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(UUID.randomUUID().toString());

        // when
        manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, LIFE_CYCLE_CONTEXT);

        // then
        verify(pool, times(1)).submit(any(Runnable.class));
    }

    @Test
    void shouldInitiateProcessingAndReturnOkStatus() throws Exception {
        // given
        final ItemDataRequest itemDataRequest = ItemDataRequest.rootNode(UUID.randomUUID().toString());

        // when
        final TransferInitiateResponse initiateResponse = manager.initiateRequest(itemDataRequest, s -> {
        }, aasTransferProcess -> {
        }, LIFE_CYCLE_CONTEXT);

        // then
        assertThat(initiateResponse.getTransferId()).isNotBlank();
        assertThat(initiateResponse.getStatus()).isEqualTo(ResponseStatus.OK);
    }
}
