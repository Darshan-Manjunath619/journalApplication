package com.darshan.journalApplication.controller;

import com.darshan.journalApplication.auth.*;
import com.darshan.journalApplication.auth.dto.*;
import com.darshan.journalApplication.entity.User;
import com.darshan.journalApplication.service.UserEntryService;
import com.darshan.journalApplication.user.UserMapper;
import com.darshan.journalApplication.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTests {
    private static final Instant EXPIRY = Instant.parse("2026-08-21T00:00:00Z");

    private UserEntryService users;
    private AuthenticationManager authenticationManager;
    private JwtUtil jwt;
    private RefreshTokenService refreshTokens;
    private AuthController controller;

    @BeforeEach
    void setUp() {
        users = mock(UserEntryService.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwt = mock(JwtUtil.class);
        refreshTokens = mock(RefreshTokenService.class);
        when(jwt.generateToken(anyString())).thenReturn("access-jwt");
        when(jwt.getAccessTokenExpiresInSeconds()).thenReturn(900L);
        controller = new AuthController(
                users, mock(UserMapper.class), authenticationManager, jwt,
                refreshTokens,
                new RefreshCookieService("refresh_token", "/journal/api/v1/auth",
                        false, Duration.ofDays(7)),
                new TrustedOriginService(List.of("http://localhost:5173")));
    }

    @Test
    void loginReturnsAccessTokenAndHttpOnlyRefreshCookie() {
        User user = User.builder().id(1L).userName("darshan").build();
        when(users.findByUserName("darshan")).thenReturn(user);
        when(refreshTokens.issue(user)).thenReturn(new IssuedRefreshToken("refresh-1", EXPIRY));

        ResponseEntity<AuthResponse> response = controller.login(
                new LoginRequest("  DARSHAN  ", "password123"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("access-jwt", response.getBody().accessToken());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("HttpOnly"));
        verify(users).findByUserName("darshan");
        verify(refreshTokens).issue(user);
    }

    @Test
    void refreshRotatesCookieAndReturnsNewAccessToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh_token", "old-refresh"));
        when(refreshTokens.rotate("old-refresh"))
                .thenReturn(new RotatedRefreshToken("darshan", "new-refresh", EXPIRY));

        ResponseEntity<AuthResponse> response = controller.refresh(
                request, "http://localhost:5173");

        assertEquals("access-jwt", response.getBody().accessToken());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)
                .contains("refresh_token=new-refresh"));
    }

    @Test
    void logoutRevokesPresentedTokenAndClearsCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("refresh_token", "refresh-1"));

        ResponseEntity<Void> response = controller.logout(
                request, "http://localhost:5173");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("Max-Age=0"));
        verify(refreshTokens).revokePresented("refresh-1");
    }
}
