package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.eclipse.tractusx.irs.InMemoryBlobStore;
import org.eclipse.tractusx.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.irs.bpdm.BpdmFacade;
import org.eclipse.tractusx.irs.connector.job.ResponseStatus;
import org.eclipse.tractusx.irs.connector.job.TransferInitiateResponse;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.eclipse.tractusx.irs.util.TestMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AASTransferProcessManagerTest {

    private final TestMother generate = new TestMother();

    DigitalTwinRegistryFacade digitalTwinRegistryFacade = mock(DigitalTwinRegistryFacade.class);
    SubmodelFacade submodelFacade = mock(SubmodelFacade.class);
    SemanticsHubFacade semanticsHubFacade = mock(SemanticsHubFacade.class);
    BpdmFacade bpdmFacade = mock(BpdmFacade.class);
    JsonValidatorService jsonValidatorService = mock(JsonValidatorService.class);
    ExecutorService pool = mock(ExecutorService.class);

    AASHandler aasHandler = new AASHandler(digitalTwinRegistryFacade, submodelFacade, semanticsHubFacade, bpdmFacade, jsonValidatorService, new JsonUtil());

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

}
