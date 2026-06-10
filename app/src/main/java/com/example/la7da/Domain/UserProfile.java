package com.example.la7da.Domain;

public class UserProfile {
    private int id;
    private String username;
    private String email;
    private String full_name;
    private String avatar_url;
    private String language;
    private int rating;
    private int reviews_count;

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return full_name; }
    public String getAvatarUrl() { return avatar_url; }
    public String getLanguage() { return language; }
    public int getRating() { return rating; }
    public int getReviewsCount() { return reviews_count; }
}