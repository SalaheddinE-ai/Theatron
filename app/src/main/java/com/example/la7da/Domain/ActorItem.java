package com.example.la7da.Domain;

public class ActorItem {
    private String name;
    private String character;
    private String profilePath;

    public ActorItem(String name, String character, String profilePath) {
        this.name = name;
        this.character = character;
        this.profilePath = profilePath;
    }

    public String getName() { return name; }
    public String getCharacter() { return character; }
    public String getProfilePath() { return profilePath; }
}