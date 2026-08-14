package com.darshan.journalApplication.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TrustedOriginService {
    private final List<String> allowedOrigins;

    public TrustedOriginService(
            @Value("${security.cors.allowed-origins:http://localhost:5173}")
            List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public void verify(String origin) {
        if (origin != null && !allowedOrigins.contains(origin)) {
            throw new UntrustedOriginException();
        }
    }
}
