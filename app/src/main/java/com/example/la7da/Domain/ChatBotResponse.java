package com.example.la7da.Domain;

import java.util.List;

public class ChatBotResponse {
    private String reply;
    private List<String> suggestions;
    private boolean success;

    public String getReply() { return reply; }
    public List<String> getSuggestions() { return suggestions; }
    public boolean isSuccess() { return success; }
}