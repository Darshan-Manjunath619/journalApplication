package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.shared.error.GlobalExceptionHandler;
import com.darshan.journalApplication.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsersControllerTests {
    private UserEntryService users;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        users = mock(UserEntryService.class);
        mvc = MockMvcBuilders.standaloneSetup(new UsersController(users, new UserMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsSafeProfileForAuthenticatedUser() throws Exception {
        User user = User.builder().id(10L).userName("alice")
                .email("alice@example.com").password("secret-hash")
                .role(List.of("USER")).build();
        when(users.getProfile("alice")).thenReturn(user);

        mvc.perform(get("/api/v1/users/me").principal(principal()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("alice"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void rejectsEmptyProfilePatch() throws Exception {
        mvc.perform(patch("/api/v1/users/me").principal(principal())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void validatesPasswordChangeBeforeCallingService() throws Exception {
        mvc.perform(patch("/api/v1/users/me/password").principal(principal())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"short","newPassword":"tiny"}
                                """))
                .andExpect(status().isBadRequest());

        verify(users, never()).changePassword(anyString(), anyString(), anyString());
    }

    private UsernamePasswordAuthenticationToken principal() {
        return new UsernamePasswordAuthenticationToken("alice", "ignored", List.of());
    }
}
