package com.darshan.journalservice.identity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTests {
    private static final String SECRET = "test-jwt-secret-that-is-at-least-32-bytes-long";

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void healthRemainsPublic() throws Exception {
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void missingTokenReturnsProblemDetail() throws Exception {
        mvc.perform(get("/api/v1/journals"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication required"));
    }

    @Test
    void malformedTokenIsRejected() throws Exception {
        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongSignatureIsRejected() throws Exception {
        String wrongSecret = "different-test-secret-that-is-at-least-32-bytes";
        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(token(42L, wrongSecret,
                                Instant.now().plusSeconds(60)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        mvc.perform(get("/api/v1/journals")
                        .header("Authorization", bearer(token(42L, SECRET,
                                Instant.now().minusSeconds(1)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validTokenUidControlsJournalOwnership() throws Exception {
        String ownerToken = token(42L, SECRET, Instant.now().plusSeconds(60));
        String otherUserToken = token(43L, SECRET, Instant.now().plusSeconds(60));
        String request = objectMapper.writeValueAsString(Map.of("title", "Owned by uid 42"));

        String response = mvc.perform(post("/api/v1/journals")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(response);

        mvc.perform(get("/api/v1/journals/{id}", created.get("id").longValue())
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Owned by uid 42"));

        mvc.perform(get("/api/v1/journals/{id}", created.get("id").longValue())
                        .header("Authorization", bearer(otherUserToken)))
                .andExpect(status().isNotFound());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String token(Long userId, String secret, Instant expiration) {
        return Jwts.builder()
                .subject("user-" + userId)
                .claim("uid", userId)
                .claim("roles", List.of("USER"))
                .issuer("journal-application")
                .audience().add("journal-spa").and()
                .id("test-token-" + userId)
                .issuedAt(Date.from(Instant.now().minusSeconds(1)))
                .expiration(Date.from(expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
