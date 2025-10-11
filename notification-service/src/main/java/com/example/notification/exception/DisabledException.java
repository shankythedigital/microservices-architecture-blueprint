package com.example.notification.exception;

public class DisabledException extends RuntimeException {
    public DisabledException() { super("Account is disabled."); }
}
