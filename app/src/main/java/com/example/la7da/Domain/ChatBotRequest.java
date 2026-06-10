package com.example.la7da.Domain;

public class ChatBotRequest {
    private String message;
    private String language;

    public ChatBotRequest(String message, String language) {
        this.message = message;
        this.language = language;
    }
}