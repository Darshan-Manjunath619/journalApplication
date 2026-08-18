package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.JournalEntry;
import com.darshan.journalApplication.journal.JournalMapper;
import com.darshan.journalApplication.service.JournalEntryService;
import com.darshan.journalApplication.shared.error.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JournalEntryControllerTests {
    @Mock JournalEntryService service;
    MockMvc mvc;
    final org.springframework.security.core.Authentication authentication =
            new UsernamePasswordAuthenticationToken("alice", "n/a");

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(
                new JournalEntryController(service, new JournalMapper()))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test void emptyListReturnsOkArray() throws Exception {
        when(service.getAllByOwner("alice")).thenReturn(List.of());
        mvc.perform(get("/journal").principal(authentication))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test void createValidatesAndReturnsSafeResponse() throws Exception {
        JournalEntry saved = JournalEntry.builder().id(1L).title("Title")
                .content("Body").date(LocalDateTime.of(2026,1,1,0,0)).build();
        when(service.EntryRecord(any(), eq("alice"))).thenReturn(saved);
        mvc.perform(post("/journal").principal(authentication)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Title\",\"content\":\"Body\",\"user\":{}}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user").doesNotExist());
    }

    @Test void blankCreateAndEmptyUpdateAreRejected() throws Exception {
        mvc.perform(post("/journal").principal(authentication)
                .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(patch("/journal/1").principal(authentication)
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
        verifyNoInteractions(service);
    }

    @Test void unownedReadReturnsProblemDetail() throws Exception {
        when(service.getOwned(9L, "alice"))
                .thenThrow(new ResourceNotFoundException("Journal entry not found"));
        mvc.perform(get("/journal/id/9").principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Journal entry not found"));
    }

    @Test void deleteOwnedEntryReturnsNoContent() throws Exception {
        mvc.perform(delete("/journal/3").principal(authentication))
                .andExpect(status().isNoContent());
        verify(service).deleteById(3L, "alice");
    }
}
