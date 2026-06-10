package com.example.la7da.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.la7da.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class VideoPlayer extends AppCompatActivity {

    private YouTubePlayerView youTubePlayerView;
    private ProgressBar progressBar;
    private ImageView backBtn;
    private String videoUrl;
    private String videoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoUrl = getIntent().getStringExtra("videoUrl");

        if (videoUrl == null || videoUrl.isEmpty()) {
            Toast.makeText(this, "No video URL provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Extraire l'ID de la vidéo
        videoId = extractVideoId(videoUrl);

        initViews();

        if (videoId != null) {
            setupYouTubePlayer();
        } else {
            // Si pas d'ID, ouvrir directement dans le navigateur
            openInBrowser(videoUrl);
        }
    }

    private void initViews() {
        youTubePlayerView = findViewById(R.id.youtubePlayerView);
        progressBar = findViewById(R.id.progressBar);
        backBtn = findViewById(R.id.backBtn);

        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }
    }

    private void setupYouTubePlayer() {
        getLifecycle().addObserver(youTubePlayerView);

        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(YouTubePlayer youTubePlayer) {
                progressBar.setVisibility(View.GONE);
                youTubePlayer.loadVideo(videoId, 0);
            }

            @Override
            public void onError(YouTubePlayer youTubePlayer,
                                com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError error) {
                progressBar.setVisibility(View.GONE);

                // Si erreur, ouvrir dans l'application YouTube ou le navigateur
                Toast.makeText(VideoPlayer.this,
                        "Opening in YouTube...", Toast.LENGTH_SHORT).show();
                openInYouTube();
            }
        });
    }

    /**
     * Ouvre la vidéo dans l'application YouTube
     */
    private void openInYouTube() {
        if (videoId == null) {
            openInBrowser(videoUrl);
            return;
        }

        // Essayer d'ouvrir dans l'application YouTube
        Intent appIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("vnd.youtube:" + videoId));

        if (appIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(appIntent);
        } else {
            // Si l'application YouTube n'est pas installée, ouvrir dans le navigateur
            openInBrowser("https://www.youtube.com/watch?v=" + videoId);
        }

        finish();
    }

    /**
     * Ouvre l'URL dans le navigateur
     */
    private void openInBrowser(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        if (browserIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "No browser available", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    /**
     * Extrait l'ID de la vidéo YouTube de l'URL
     */
    private String extractVideoId(String url) {
        if (url == null) return null;

        // Format: https://www.youtube.com/embed/VIDEO_ID
        if (url.contains("youtube.com/embed/")) {
            String[] parts = url.split("/embed/");
            if (parts.length > 1) {
                String id = parts[1];
                if (id.contains("?")) id = id.split("\\?")[0];
                return id;
            }
        }

        // Format: https://www.youtube.com/watch?v=VIDEO_ID
        if (url.contains("youtube.com/watch?v=")) {
            String[] parts = url.split("v=");
            if (parts.length > 1) {
                String id = parts[1];
                if (id.contains("&")) id = id.split("&")[0];
                return id;
            }
        }

        // Format: https://youtu.be/VIDEO_ID
        if (url.contains("youtu.be/")) {
            String[] parts = url.split("youtu.be/");
            if (parts.length > 1) {
                String id = parts[1];
                if (id.contains("?")) id = id.split("\\?")[0];
                if (id.contains("/")) id = id.split("/")[0];
                return id;
            }
        }

        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}