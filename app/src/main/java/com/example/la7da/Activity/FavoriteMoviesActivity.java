package com.example.la7da.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.la7da.R;

public class FavoriteMoviesActivity extends AppCompatActivity {

    private ImageView backBtn;
    private RecyclerView recyclerView;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        backBtn = findViewById(R.id.favBackBtn);
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyText = findViewById(R.id.emptyFavText);

        backBtn.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // TODO: Charger les favoris depuis la base de données
        // Pour l'instant, afficher le message vide
        emptyText.setVisibility(android.view.View.VISIBLE);
        recyclerView.setVisibility(android.view.View.GONE);
    }
}