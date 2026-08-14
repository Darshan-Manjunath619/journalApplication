package com.darshan.journalApplication.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RefreshCookieServiceTests {
    private final RefreshCookieService cookies = new RefreshCookieService(
            "refresh_token", "/journal/api/v1/auth", false, Duration.ofDays(7));

    @Test
    void createsAnHttpOnlySameSiteCookieWithoutExposingItToJavascript() {
        String header = cookies.create("raw-secret");

        assertTrue(header.contains("refresh_token=raw-secret"));
        assertTrue(header.contains("HttpOnly"));
        assertTrue(header.contains("SameSite=Lax"));
        assertTrue(header.contains("Path=/journal/api/v1/auth"));
        assertTrue(header.contains("Max-Age=604800"));
    }

    @Test
    void readsOnlyTheConfiguredCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "ignored"),
                new Cookie("refresh_token", "presented"));

        assertEquals("presented", cookies.read(request));
    }

    @Test
    void clearingCookieExpiresItImmediately() {
        String header = cookies.clear();

        assertTrue(header.contains("refresh_token="));
        assertTrue(header.contains("Max-Age=0"));
    }
}
