package com.darshan.journalApplication.auth;

public class RefreshTokenReuseException extends RuntimeException {
    public RefreshTokenReuseException() {
        super("Refresh token reuse was detected; active sessions were revoked");
    }
}
