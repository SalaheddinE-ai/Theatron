package com.example.la7da.Domain;

public class PurchaseRequest {
    private int movie_id;
    private String movie_title;
    private String poster_path;
    private String purchase_date;
    private String purchase_time;
    private double rating;

    public PurchaseRequest(int movieId, String movieTitle, String posterPath,
                           String purchaseDate, String purchaseTime, double rating) {
        this.movie_id = movieId;
        this.movie_title = movieTitle;
        this.poster_path = posterPath;
        this.purchase_date = purchaseDate;
        this.purchase_time = purchaseTime;
        this.rating = rating;
    }

    public int getMovieId() { return movie_id; }
    public void setMovieId(int movie_id) { this.movie_id = movie_id; }

    public String getMovieTitle() { return movie_title; }
    public void setMovieTitle(String movie_title) { this.movie_title = movie_title; }

    public String getPosterPath() { return poster_path; }
    public void setPosterPath(String poster_path) { this.poster_path = poster_path; }

    public String getPurchaseDate() { return purchase_date; }
    public void setPurchaseDate(String purchase_date) { this.purchase_date = purchase_date; }

    public String getPurchaseTime() { return purchase_time; }
    public void setPurchaseTime(String purchase_time) { this.purchase_time = purchase_time; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
}