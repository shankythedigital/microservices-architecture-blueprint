package com.example.common.exception;
public class CredentialsExpiredException extends RuntimeException {
    public CredentialsExpiredException(String msg){ super(msg); }
}
