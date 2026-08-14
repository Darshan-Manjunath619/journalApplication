package com.darshan.journalApplication.auth;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrustedOriginServiceTests {
    private final TrustedOriginService origins =
            new TrustedOriginService(List.of("http://localhost:5173"));

    @Test
    void acceptsConfiguredBrowserOriginAndNonBrowserRequests() {
        assertDoesNotThrow(() -> origins.verify("http://localhost:5173"));
        assertDoesNotThrow(() -> origins.verify(null));
    }

    @Test
    void rejectsUntrustedBrowserOrigin() {
        assertThrows(UntrustedOriginException.class,
                () -> origins.verify("https://attacker.example"));
    }
}
