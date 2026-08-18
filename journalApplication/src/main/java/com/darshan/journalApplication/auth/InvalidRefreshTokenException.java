package com.darshan.journalApplication.auth;

public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Refresh token is missing, invalid, expired, or revoked");
    }
}
