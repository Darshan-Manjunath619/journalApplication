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
class JournalsCrudIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void ownedJournalCrudAndCrossUserProtectionWorkTogether() throws Exception {
        register("journal_alice", "journal-alice@example.com");
        register("journal_bob", "journal-bob@example.com");
        String aliceToken = login("journal_alice");
        String bobToken = login("journal_bob");

        mvc.perform(get("/api/v1/journals").header("Authorization", bearer(bobToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        MvcResult created = mvc.perform(post("/api/v1/journals")
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" First day ","content":"Initial notes"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("First day"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.user").doesNotExist())
                .andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asLong();

        mvc.perform(get("/api/v1/journals/{id}", id)
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());

        mvc.perform(patch("/api/v1/journals/{id}", id)
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"Updated notes"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First day"))
                .andExpect(jsonPath("$.content").value("Updated notes"));

        mvc.perform(delete("/api/v1/journals/{id}", id)
                        .header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/v1/journals/{id}", id)
                        .header("Authorization", bearer(aliceToken)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/journals/{id}", id)
                        .header("Authorization", bearer(aliceToken)))
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

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
