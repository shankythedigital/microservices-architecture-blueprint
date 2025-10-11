package com.example.notification.exception;

public class CredentialsExpiredException extends RuntimeException {
    public CredentialsExpiredException() { super("Credentials expired. Please regenerate OTP or refresh token."); }
}
