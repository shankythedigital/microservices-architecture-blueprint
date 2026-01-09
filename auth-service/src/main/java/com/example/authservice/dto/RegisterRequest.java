package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * RegisterRequest DTO for user registration
 * Includes additional fields: username, pincode, city, state, country, acceptTc, and countryCode
 */
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    public String username;
    
    public String password;
    public String email;
    
    @NotBlank(message = "Mobile number is required")
    public String mobile;
    
    @NotBlank(message = "Country code is required")
    public String countryCode; // e.g., "+91", "+1", "+44"
    
    @NotBlank(message = "Project type is required")
    public String projectType;
    
    // Address fields
    public String pincode;
    public String city;
    public String state;
    public String country;
    
    // Terms & Conditions acceptance flag
    @NotNull(message = "Terms and Conditions acceptance is required")
    public Boolean acceptTc; // Must be true to register
}
