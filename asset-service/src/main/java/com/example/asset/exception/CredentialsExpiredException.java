package com.example.asset.exception;
public class CredentialsExpiredException extends RuntimeException {
    public CredentialsExpiredException(String msg){ super(msg); }
}
