package com.darshan.journalApplication.shared.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.darshan.journalApplication.shared.web.CorrelationIdFilter;

class GlobalExceptionHandlerTests {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void returnsFieldErrorsForInvalidInput() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void returnsSafeErrorForMalformedJson() throws Exception {
        mvc.perform(post("/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Malformed request"));
    }

    @Test
    void mapsDomainExceptions() throws Exception {
        mvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Journal was not found"));
        mvc.perform(get("/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Username already exists"));
    }

    @Test
    void hidesUnexpectedExceptionDetails() throws Exception {
        mvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("sensitive detail"))));
    }

    @Test
    void includesCorrelationIdInProblemResponse() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new CorrelationIdFilter()).build();
        mvc.perform(get("/missing").header(CorrelationIdFilter.HEADER, "request-42"))
                .andExpect(status().isNotFound())
                .andExpect(header().string(CorrelationIdFilter.HEADER, "request-42"))
                .andExpect(jsonPath("$.correlationId").value("request-42"));
    }

    record Input(@NotBlank String name) {}

    @RestController
    static class TestController {
        @PostMapping("/validate") void validate(@Valid @RequestBody Input input) {}
        @GetMapping("/missing") void missing() {
            throw new ResourceNotFoundException("Journal was not found");
        }
        @GetMapping("/conflict") void conflict() {
            throw new ConflictException("Username already exists");
        }
        @GetMapping("/unexpected") void unexpected() {
            throw new IllegalStateException("sensitive detail");
        }
    }
}
