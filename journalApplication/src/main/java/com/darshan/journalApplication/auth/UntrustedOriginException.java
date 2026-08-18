package com.darshan.journalApplication.auth;

public class UntrustedOriginException extends RuntimeException {
    public UntrustedOriginException() {
        super("Request origin is not allowed");
    }
}
