package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.shared.error.*;
import com.darshan.journalApplication.tag.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TagsControllerTests {
    TagService tags;
    MockMvc mvc;
    ObjectMapper json = new ObjectMapper();

    @BeforeEach void setUp() {
        tags = mock(TagService.class);
        mvc = MockMvcBuilders.standaloneSetup(new TagsController(tags, new TagMapper()))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test void blankAndOversizedNamesAreRejectedBeforeService() throws Exception {
        mvc.perform(post("/api/v1/tags").principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", " "))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
        mvc.perform(post("/api/v1/tags").principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "a".repeat(81)))))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(tags);
    }

    @Test void duplicateNameUsesProblemDetailConflict() throws Exception {
        when(tags.create("alice", "Spring"))
                .thenThrow(new ConflictException("Tag name already exists"));
        mvc.perform(post("/api/v1/tags").principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("name", "Spring"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Resource conflict"))
                .andExpect(jsonPath("$.detail").value("Tag name already exists"));
    }

    private UsernamePasswordAuthenticationToken principal() {
        return new UsernamePasswordAuthenticationToken("alice", "ignored", List.of());
    }
}
