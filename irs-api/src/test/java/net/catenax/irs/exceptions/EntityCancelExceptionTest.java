package net.catenax.irs.exceptions;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import net.catenax.irs.services.AsyncJobHandlerService;
import net.catenax.irs.services.IrsItemGraphQueryService;
import net.catenax.irs.util.JobsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntityCancelExceptionTest {

    @Mock
    AsyncJobHandlerService handler;

    @Mock
    IrsItemGraphQueryService queryService;

    JobsHelper helper = new JobsHelper();

    @BeforeEach
    public void setup() {
        queryService = mock(IrsItemGraphQueryService.class);
        handler = mock(AsyncJobHandlerService.class);
    }

    @Test
    void cancelExceptionTest() {
        final var ex = new EntityCancelException("test error");
        doThrow(ex).when(handler).getCompleteJobResult(any());

        assertThatExceptionOfType(EntityCancelException.class)
                .isThrownBy(() ->
                        handler.getCompleteJobResult(UUID.randomUUID()));

    }

}