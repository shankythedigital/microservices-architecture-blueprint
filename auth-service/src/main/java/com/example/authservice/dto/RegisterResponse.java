
package com.example.authservice.dto;

public class RegisterResponse {
    private String username;
    private String email;
    private String mobile;
    private String employeeId;

    public RegisterResponse(String username, String email, String mobile, String employeeId) {
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.employeeId = employeeId;
    }

    // getters & setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}


