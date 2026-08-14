package com.darshan.journalApplication.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieService {
    public static final String DEFAULT_NAME = "refresh_token";

    private final String name;
    private final String path;
    private final boolean secure;
    private final Duration timeToLive;

    public RefreshCookieService(
            @Value("${security.refresh-cookie.name:refresh_token}") String name,
            @Value("${security.refresh-cookie.path:/journal/api/v1/auth}") String path,
            @Value("${security.refresh-cookie.secure:false}") boolean secure,
            @Value("${security.refresh-token-ttl:P7D}") Duration timeToLive) {
        this.name = name;
        this.path = path;
        this.secure = secure;
        this.timeToLive = timeToLive;
    }

    public String create(String rawToken) {
        return ResponseCookie.from(name, rawToken)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(timeToLive)
                .build()
                .toString();
    }

    public String clear() {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(Duration.ZERO)
                .build()
                .toString();
    }

    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
