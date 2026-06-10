package com.example.la7da.Domain;

public class FavoriteRequest {
    private int movie_id;
    private String movie_title;
    private String poster_path;
    private double vote_average;
    private String release_date;
    private String overview;

    public FavoriteRequest(int movieId, String title, String posterPath,
                           double voteAvg, String releaseDate, String overview) {
        this.movie_id = movieId;
        this.movie_title = title;
        this.poster_path = posterPath;
        this.vote_average = voteAvg;
        this.release_date = releaseDate;
        this.overview = overview;
    }
}