package com.example.la7da.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.la7da.Adapter.ActorAdapter;
import com.example.la7da.Adapter.ImageAdapter;
import com.example.la7da.Domain.ActorItem;
import com.example.la7da.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView posterBig, posterNormal, backBtn, favoriteBtn;
    private TextView movieName, movieGenre, movieRate, movieTime, movieDate, summary, cinemaInfo;
    private RecyclerView imagesRecycler, actorsRecyclerView;
    private ProgressBar detailLoading;
    private Button playTrailerBtn, watchOnlineBtn;

    private GoogleMap mMap;
    private boolean isFavorite = false;
    private int movieId;
    private String movieTitleStr = "";
    private String posterPath = "";
    private String trailerKey = "";

    private final String API_KEY = "020092194a657c6395c0df233409f4b3";
    private final String BASE_URL = "https://api.themoviedb.org/3/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initViews();
        setupRecyclerViews();

        movieId = getIntent().getIntExtra("movieId", -1);
        if (movieId != -1) {
            fetchMovieDetails(movieId);
        } else {
            Toast.makeText(this, "No movie selected", Toast.LENGTH_SHORT).show();
            finish();
        }

        backBtn.setOnClickListener(v -> finish());
        favoriteBtn.setOnClickListener(v -> toggleFavorite());
        playTrailerBtn.setOnClickListener(v -> playTrailer());
        watchOnlineBtn.setOnClickListener(v -> watchOnline());

        // Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initViews() {
        posterBig = findViewById(R.id.posterBiglmg);
        posterNormal = findViewById(R.id.posterNormallmg);
        backBtn = findViewById(R.id.imageView9);
        favoriteBtn = findViewById(R.id.imageView10);
        movieName = findViewById(R.id.movieNameTxt);
        movieGenre = findViewById(R.id.movieGenreTxt);
        movieRate = findViewById(R.id.movieRateTxt);
        movieTime = findViewById(R.id.movieTimeTxt);
        movieDate = findViewById(R.id.movieDateTxt);
        summary = findViewById(R.id.movieSummaryInfo);
        cinemaInfo = findViewById(R.id.cinemaInfoTxt);
        imagesRecycler = findViewById(R.id.imagesRecyclerView);
        actorsRecyclerView = findViewById(R.id.actorsRecyclerView);
        detailLoading = findViewById(R.id.detailLoding);
        playTrailerBtn = findViewById(R.id.playTrailerBtn);
        watchOnlineBtn = findViewById(R.id.watchOnlineBtn);
    }

    private void setupRecyclerViews() {
        imagesRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        actorsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        if (isFavorite) {
            favoriteBtn.setColorFilter(Color.parseColor("#FFC107")); // Jaune
            Toast.makeText(this, "Added to favorites! ❤️", Toast.LENGTH_SHORT).show();
        } else {
            favoriteBtn.setColorFilter(Color.WHITE);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        }
    }

    private void playTrailer() {
        if (!trailerKey.isEmpty()) {
            Intent intent = new Intent(this, VideoPlayer.class);
            intent.putExtra("videoUrl", "https://www.youtube.com/embed/" + trailerKey);
            startActivity(intent);
        } else {
            Toast.makeText(this, "No trailer available", Toast.LENGTH_SHORT).show();
        }
    }

    private void watchOnline() {
        // Rechercher le film en ligne
        String searchQuery = movieTitleStr.replace(" ", "+") + "+full+movie+online";
        String url = "https://www.google.com/search?q=" + searchQuery;
        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
        startActivity(intent);
    }

    private void fetchMovieDetails(int movieId) {
        detailLoading.setVisibility(View.VISIBLE);
        String url = BASE_URL + "movie/" + movieId + "?api_key=" + API_KEY +
                "&append_to_response=credits,images,videos";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        posterPath = response.optString("poster_path", "");
                        movieTitleStr = response.optString("title", "Unknown");
                        String overview = response.optString("overview", "No description");
                        String date = response.optString("release_date", "N/A");
                        double vote = response.optDouble("vote_average", 0);
                        int runtime = response.optInt("runtime", 0);

                        // Posters
                        Glide.with(this).load("https://image.tmdb.org/t/p/w500" + posterPath).into(posterNormal);
                        Glide.with(this).load("https://image.tmdb.org/t/p/w500" + posterPath).into(posterBig);

                        movieName.setText(movieTitleStr);
                        movieRate.setText(String.valueOf(vote));
                        movieTime.setText(runtime + " min");
                        movieDate.setText(date);
                        summary.setText(overview);

                        // Genres
                        JSONArray genres = response.optJSONArray("genres");
                        if (genres != null && genres.length() > 0) {
                            StringBuilder genreSb = new StringBuilder();
                            for (int i = 0; i < genres.length(); i++) {
                                JSONObject genre = genres.getJSONObject(i);
                                genreSb.append(genre.optString("name", ""));
                                if (i < genres.length() - 1) genreSb.append(", ");
                            }
                            movieGenre.setText(genreSb.toString());
                        }

                        // Casting (acteurs avec images)
                        JSONObject credits = response.optJSONObject("credits");
                        if (credits != null) {
                            JSONArray cast = credits.optJSONArray("cast");
                            List<ActorItem> actorList = new ArrayList<>();
                            if (cast != null) {
                                for (int i = 0; i < Math.min(cast.length(), 10); i++) {
                                    JSONObject actor = cast.getJSONObject(i);
                                    String name = actor.optString("name", "Unknown");
                                    String profilePath = actor.optString("profile_path", "");
                                    String character = actor.optString("character", "");
                                    actorList.add(new ActorItem(name, character, profilePath));
                                }
                            }
                            actorsRecyclerView.setAdapter(new ActorAdapter(this, actorList));
                        }

                        // Images
                        JSONObject images = response.optJSONObject("images");
                        if (images != null) {
                            JSONArray backdrops = images.optJSONArray("backdrops");
                            List<String> imageUrls = new ArrayList<>();
                            if (backdrops != null) {
                                for (int i = 0; i < Math.min(backdrops.length(), 10); i++) {
                                    JSONObject img = backdrops.getJSONObject(i);
                                    imageUrls.add("https://image.tmdb.org/t/p/w500" +
                                            img.optString("file_path", ""));
                                }
                            }
                            imagesRecycler.setAdapter(new ImageAdapter(this, imageUrls));
                        }

                        // Trailer
                        JSONObject videos = response.optJSONObject("videos");
                        if (videos != null) {
                            JSONArray results = videos.optJSONArray("results");
                            if (results != null) {
                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject video = results.getJSONObject(i);
                                    if ("Trailer".equals(video.optString("type", "")) &&
                                            "YouTube".equals(video.optString("site", ""))) {
                                        trailerKey = video.optString("key", "");
                                        break;
                                    }
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    detailLoading.setVisibility(View.GONE);
                },
                error -> {
                    detailLoading.setVisibility(View.GONE);
                    Toast.makeText(DetailActivity.this, "Error loading details", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Cinémas à proximité (Casablanca - exemple)
        LatLng casablanca = new LatLng(33.5731, -7.5898);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(casablanca, 12));

        // Ajouter des cinémas
        addCinemaMarker(new LatLng(33.5964, -7.6154), "Cinema Megarama");
        addCinemaMarker(new LatLng(33.5731, -7.5898), "Cinema Rif");
        addCinemaMarker(new LatLng(33.5820, -7.6100), "IMAX Morocco Mall");

        // Info cinémas
        cinemaInfo.setText("🎬 Megarama Casablanca\n🎬 Cinema Rif\n🎬 IMAX Morocco Mall");
    }

    private void addCinemaMarker(LatLng position, String title) {
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }
}