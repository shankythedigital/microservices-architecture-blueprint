package com.example.authservice.dto;

public class EmailMobileChangeConfirmRequest {
    public String resetToken;
    public String newValue;
    public String otp;   // OTP sent to new value
}
