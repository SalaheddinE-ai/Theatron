package com.example.la7da.Domain;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String full_name;

    public RegisterRequest(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.full_name = fullName;
    }
}