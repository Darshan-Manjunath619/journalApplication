package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.journal.JournalMapper;
import com.darshan.journalApplication.service.JournalEntryService;
import com.darshan.journalApplication.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JournalsControllerTests {
    JournalEntryService journals;
    MockMvc mvc;

    @BeforeEach void setUp() {
        journals = mock(JournalEntryService.class);
        mvc = MockMvcBuilders.standaloneSetup(
                        new JournalsController(journals, new JournalMapper()))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test void invalidFavoriteQueryReturnsSafeBadRequest() throws Exception {
        mvc.perform(get("/api/v1/journals").principal(principal())
                        .param("favorite", "sometimes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid query parameter"))
                .andExpect(jsonPath("$.detail")
                        .value("One or more query parameters are invalid"));
        verifyNoInteractions(journals);
    }

    @Test void nonPositiveTagIdAndEmptyPatchAreRejected() throws Exception {
        mvc.perform(post("/api/v1/journals").principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Entry","tagIds":[0]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
        mvc.perform(patch("/api/v1/journals/1").principal(principal())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
        verifyNoInteractions(journals);
    }

    private UsernamePasswordAuthenticationToken principal() {
        return new UsernamePasswordAuthenticationToken("alice", "ignored", List.of());
    }
}
