package com.example.la7da.Domain;

public class LoginResponse {
    private String access_token;
    private String token_type;
    private UserProfile user;

    public String getAccessToken() { return access_token; }
    public UserProfile getUser() { return user; }
}