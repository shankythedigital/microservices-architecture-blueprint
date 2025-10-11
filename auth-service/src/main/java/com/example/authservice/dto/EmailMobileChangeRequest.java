package com.example.authservice.dto;

public class EmailMobileChangeRequest {
    public Long userId;
    public String oldValue;
    public String otp;   // OTP sent to old value
    public String type;  // "EMAIL" or "MOBILE"
}
