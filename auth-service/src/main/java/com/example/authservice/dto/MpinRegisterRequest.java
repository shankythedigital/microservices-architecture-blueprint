package com.example.authservice.dto;

public class MpinRegisterRequest {
    public Long userId;
    public String mpin;       // raw mpin, will be hashed
    public String deviceInfo; // optional, to differentiate devices
}
