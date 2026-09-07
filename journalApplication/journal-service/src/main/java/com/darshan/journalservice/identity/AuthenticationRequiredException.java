package com.darshan.journalservice.identity;

public class AuthenticationRequiredException extends RuntimeException {
    public AuthenticationRequiredException() {
        super("A valid access token is required");
    }
}
