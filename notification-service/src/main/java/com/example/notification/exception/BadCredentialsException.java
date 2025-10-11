package com.example.notification.exception;

public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException() { super("Invalid username, password, or OTP."); }
    public BadCredentialsException(String msg) { super(msg); }
}
