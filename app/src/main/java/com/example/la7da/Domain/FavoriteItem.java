package com.example.la7da.Domain;

public class FavoriteItem {
    private int id;
    private int movie_id;
    private String movie_title;
    private String poster_path;
    private double vote_average;
    private String release_date;
    private String overview;

    public int getId() { return id; }
    public int getMovieId() { return movie_id; }
    public String getMovieTitle() { return movie_title; }
    public String getPosterPath() { return poster_path; }
    public double getVoteAverage() { return vote_average; }
    public String getReleaseDate() { return release_date; }
    public String getOverview() { return overview; }
}