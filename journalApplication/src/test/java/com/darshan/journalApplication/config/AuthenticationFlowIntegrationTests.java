package com.darshan.journalApplication.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationFlowIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void registerLoginProfileRotationAndReuseDetectionWorkTogether() throws Exception {
        register("security_flow", "security-flow@example.com");

        MvcResult login = login("security_flow");
        String accessToken = accessToken(login);
        Cookie originalRefresh = requiredRefreshCookie(login);

        mvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("security_flow"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        MvcResult rotation = mvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalRefresh)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie replacement = requiredRefreshCookie(rotation);
        assertNotEquals(originalRefresh.getValue(), replacement.getValue());

        mvc.perform(post("/api/v1/auth/refresh")
                        .cookie(originalRefresh)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Session invalidated"));

        mvc.perform(post("/api/v1/auth/refresh")
                        .cookie(replacement)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logoutClearsCookieAndPreventsFurtherRefresh() throws Exception {
        register("logout_flow", "logout-flow@example.com");
        Cookie refresh = requiredRefreshCookie(login("logout_flow"));

        mvc.perform(post("/api/v1/auth/logout")
                        .cookie(refresh)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("Max-Age=0")));

        mvc.perform(post("/api/v1/auth/refresh")
                        .cookie(refresh)
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isUnauthorized());
    }

    private void register(String username, String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"%s","email":"%s",
                                 "password":"password123","sentimentAnalysis":false}
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private MvcResult login(String username) throws Exception {
        return mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userName":"%s","password":"password123"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();
    }

    private String accessToken(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }

    private Cookie requiredRefreshCookie(MvcResult result) {
        Cookie cookie = result.getResponse().getCookie("refresh_token");
        assertNotNull(cookie, "refresh_token cookie must be present");
        assertTrue(cookie.isHttpOnly());
        return cookie;
    }
}
