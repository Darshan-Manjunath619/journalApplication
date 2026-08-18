package com.darshan.journalApplication.config;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JournalSearchIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void paginatesSearchesSortsFiltersAndIsolatesOwners() throws Exception {
        register("search_alice", "search-alice@example.com");
        register("search_bob", "search-bob@example.com");
        String alice = login("search_alice");
        String bob = login("search_bob");

        create(alice, "Zulu", "Spring pagination notes");
        create(alice, "Alpha", "Weekend plans");
        create(alice, "Middle", "More SPRING practice");
        create(bob, "Private Spring", "Bob only");

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .param("page", "0").param("size", "2")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Alpha"))
                .andExpect(jsonPath("$.content[1].title").value("Middle"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .param("q", "spring").param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Middle"))
                .andExpect(jsonPath("$.content[1].title").value("Zulu"));

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .param("from", "2000-01-01T00:00:00Z")
                        .param("to", "2100-01-01T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .param("page", "5").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void rejectsInvalidPaginationSortAndDateParameters() throws Exception {
        register("invalid_query", "invalid-query@example.com");
        String token = login("invalid_query");

        expectBadRequest(token, "page", "-1");
        expectBadRequest(token, "size", "101");
        expectBadRequest(token, "sort", "password,desc");
        expectBadRequest(token, "sort", "createdAt,sideways");
        expectBadRequest(token, "from", "not-a-date");

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(token))
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid query parameter"));
    }

    private void expectBadRequest(String token, String parameter, String value) throws Exception {
        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(token)).param(parameter, value))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid query parameter"));
    }

    private void register(String username, String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"%s","email":"%s",
                                 "password":"password123","sentimentAnalysis":false}
                                """.formatted(username, email)))
                .andExpect(status().isCreated());
    }

    private String login(String username) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"%s","password":"password123"}
                                """.formatted(username)))
                .andExpect(status().isOk()).andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private void create(String token, String title, String content) throws Exception {
        mvc.perform(post("/api/v1/journals")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("title", title, "content", content))))
                .andExpect(status().isCreated());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
