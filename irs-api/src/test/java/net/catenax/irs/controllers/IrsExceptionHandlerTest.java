package net.catenax.irs.controllers;

import static net.catenax.irs.util.TestMother.registerJobWithoutDepthAndAspect;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.client.HttpServerErrorException.InternalServerError;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.catenax.irs.services.IrsItemGraphQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IrsController.class)
class IrsExceptionHandlerTest {
    @MockBean
    private IrsItemGraphQueryService service;
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void handleAll() throws Exception {
        when(service.registerItemJob(any())).thenThrow(InternalServerError.class);

        this.mockMvc.perform(post("/irs/jobs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(registerJobWithoutDepthAndAspect())))
                    .andExpect(status().is5xxServerError());
    }
}