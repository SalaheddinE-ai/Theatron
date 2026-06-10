package com.example.la7da.Domain;

public class PurchasedItem {
    private int id;
    private int movie_id;
    private String movie_title;
    private String poster_path;
    private String purchase_date;
    private String purchase_time;
    private double rating;

    public int getMovieId() { return movie_id; }
    public String getMovieTitle() { return movie_title; }
    // ... autres getters
}