package com.example.la7da.Domain;

public class FilmItem {
    private int id;
    private String title;
    private String posterPath;
    private String overview;
    private String releaseDate;
    private double voteAverage;

    public FilmItem(int id, String title, String posterPath, String overview, String releaseDate, double voteAverage) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public String getOverview() { return overview; }
    public String getReleaseDate() { return releaseDate; }
    public double getVoteAverage() { return voteAverage; }
}