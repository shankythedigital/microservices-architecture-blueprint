package com.example.authservice.dto;
public class LoginRequest {
    public String loginType; // PASSWORD, OTP, MPIN, AUTHCODE, RSA, BIOMETRIC, PASSKEY, PASSPHRASE, OAUTH
    public String username;
    public String password;
    public String otp;
    public String mpin;
    public String authCode;
    public String passphrase;
    public String rsaChallenge;
    public String deviceInfo;
    public String signature;       // for RSA or WebAuthn
    public String credentialId;    // for WebAuthn

}
