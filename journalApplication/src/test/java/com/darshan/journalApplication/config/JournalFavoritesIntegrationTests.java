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
class JournalFavoritesIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void updatesAndFiltersFavoritesWithoutLeakingOtherUsersEntries() throws Exception {
        register("favorite_alice", "favorite-alice@example.com");
        register("favorite_bob", "favorite-bob@example.com");
        String alice = login("favorite_alice");
        String bob = login("favorite_bob");
        long aliceJournal = create(alice, "Important entry");
        create(bob, "Bob favorite");

        mvc.perform(patch("/api/v1/journals/{id}", aliceJournal)
                        .header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"favorite":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .param("favorite", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Important entry"));

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .param("favorite", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mvc.perform(patch("/api/v1/journals/{id}", aliceJournal)
                        .header("Authorization", bearer(bob))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"favorite":false}
                                """))
                .andExpect(status().isNotFound());
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

    private long create(String token, String title) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/journals")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                java.util.Map.of("title", title, "content", "notes"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.favorite").value(false))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
