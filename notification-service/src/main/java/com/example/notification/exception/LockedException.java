package com.example.notification.exception;

public class LockedException extends RuntimeException {
    public LockedException() { super("Account is locked. Contact support."); }
}
