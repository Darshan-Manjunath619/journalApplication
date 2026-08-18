package com.darshan.journalApplication.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void permitsPublicHealthEndpoint() throws Exception {
        mvc.perform(get("/public/health-checkup"))
                .andExpect(status().isOk());
    }

    @Test
    void permitsVersionedLoginEndpointWithoutAnAccessToken() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void returnsProblemDetailForMissingAccessToken() throws Exception {
        mvc.perform(get("/journal").header("X-Correlation-ID", "security-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Authentication required"))
                .andExpect(jsonPath("$.correlationId").value("security-401"));
    }

    @Test
    void protectsVersionedProfileEndpoint() throws Exception {
        mvc.perform(get("/api/v1/users/me")
                        .header("X-Correlation-ID", "profile-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication required"))
                .andExpect(jsonPath("$.correlationId").value("profile-401"));
    }

    @Test
    void protectsVersionedJournalsEndpoint() throws Exception {
        mvc.perform(get("/api/v1/journals"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsProblemDetailForMalformedAccessToken() throws Exception {
        mvc.perform(get("/journal")
                        .header("Authorization", "Bearer malformed")
                        .header("X-Correlation-ID", "malformed-401"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.correlationId").value("malformed-401"));
    }

    @Test
    @WithMockUser(username = "member", roles = "USER")
    void returnsProblemDetailWhenUserLacksAdminRole() throws Exception {
        mvc.perform(get("/admin").header("X-Correlation-ID", "security-403"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Access denied"))
                .andExpect(jsonPath("$.correlationId").value("security-403"));
    }

    @Test
    void acceptsCorsPreflightFromConfiguredFrontend() throws Exception {
        mvc.perform(options("/journal")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void rejectsCorsPreflightFromUntrustedOrigin() throws Exception {
        mvc.perform(options("/journal")
                        .header("Origin", "https://attacker.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "administrator", roles = "ADMIN")
    void permitsAdminRoleOnAdminEndpoint() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }
}
