package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.*;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.utils.JwtUtil;
import com.darshan.journalApplication.shared.error.GlobalExceptionHandler;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.MediaType;

class PublicControllerTests {
    @Mock UserEntryService users;
    @Mock AuthenticationManager authenticationManager;
    @Mock UserDetailsImp userDetails;
    @Mock JwtUtil jwt;
    MockMvc mvc;

    @BeforeEach void setUp() {
        MockitoAnnotations.openMocks(this);
        PublicController controller = new PublicController();
        set(controller, "userEntryService", users);
        set(controller, "authenticationManager", authenticationManager);
        set(controller, "userDetailsImp", userDetails);
        set(controller, "jwtUtil", jwt);
        set(controller, "userMapper", new UserMapper());
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    private void set(Object target, String field, Object value) {
        try { var f=target.getClass().getDeclaredField(field); f.setAccessible(true); f.set(target,value); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    @Test void rejectsInvalidRegistrationBeforeService() throws Exception {
        mvc.perform(post("/public/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"a!\",\"email\":\"bad\",\"password\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
        verifyNoInteractions(users);
    }

    @Test void signupReturnsSafeDtoWithoutPassword() throws Exception {
        User saved = User.builder().id(7L).userName("alice").email("alice@example.com")
                .password("bcrypt-hash").role(java.util.List.of("USER")).build();
        when(users.saveNewUser(any())).thenReturn(saved);
        mvc.perform(post("/public/signup").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"alice\",\"email\":\"alice@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.journalEntries").doesNotExist());
    }

    @Test void loginReturnsJsonTokenContract() throws Exception {
        var principal = org.springframework.security.core.userdetails.User
                .withUsername("alice").password("ignored").roles("USER").build();
        when(userDetails.loadUserByUsername("alice")).thenReturn(principal);
        when(jwt.generateToken("alice")).thenReturn("signed-token");
        mvc.perform(post("/public/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(3600));
    }

    @Test void loginReturnsProblemDetailForInvalidCredentials() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(
                new org.springframework.security.authentication.BadCredentialsException("hidden"));
        mvc.perform(post("/public/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userName\":\"alice\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Authentication failed"))
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }
}
