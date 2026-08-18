package com.darshan.journalApplication.config;

import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TagsIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void normalizesNamesAndRejectsDuplicates() throws Exception {
        register("tag_names", "tag-names@example.com");
        String token = login("tag_names");
        long tagId = createTag(token, " Spring ");

        mvc.perform(post("/api/v1/tags")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "spring"))))
                .andExpect(status().isConflict());

        mvc.perform(patch("/api/v1/tags/{id}", tagId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", "Java"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Java"));
    }

    @Test
    void assignsFiltersAndProtectsUserOwnedTags() throws Exception {
        register("tag_alice", "tag-alice@example.com");
        register("tag_bob", "tag-bob@example.com");
        String alice = login("tag_alice");
        String bob = login("tag_bob");
        long springTag = createTag(alice, "Spring");
        long bobTag = createTag(bob, "Private");

        mvc.perform(post("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Invalid", "tagIds", List.of(bobTag)))))
                .andExpect(status().isNotFound());

        MvcResult created = mvc.perform(post("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("title", "Spring notes",
                                "content", "JPA tags", "tagIds", List.of(springTag)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tags[0].id").value(springTag))
                .andReturn();
        long journalId = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asLong();

        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(alice))
                        .param("tag", Long.toString(springTag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring notes"));

        mvc.perform(delete("/api/v1/tags/{id}", springTag)
                        .header("Authorization", bearer(alice)))
                .andExpect(status().isConflict());

        mvc.perform(patch("/api/v1/journals/{id}", journalId)
                        .header("Authorization", bearer(alice))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("tagIds", List.of()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags").isEmpty());

        mvc.perform(delete("/api/v1/tags/{id}", springTag)
                        .header("Authorization", bearer(alice)))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/tags").header("Authorization", bearer(alice)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    private long createTag(String token, String name) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/tags")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("name", name))))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void register(String username, String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userName", username, "email", email,
                                "password", "password123", "sentimentAnalysis", false))))
                .andExpect(status().isCreated());
    }

    private String login(String username) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userName", username,
                                "password", "password123"))))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
