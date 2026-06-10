package com.example.la7da.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.la7da.R;
import java.util.ArrayList;
import java.util.List;

public class PurchasedMoviesActivity extends AppCompatActivity {

    private ImageView backBtn;
    private RecyclerView recyclerView;
    private TextView emptyText, purchasedCount;

    // Données simulées
    private List<PurchasedMovie> purchasedMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased_movies);

        backBtn = findViewById(R.id.purchasedBackBtn);
        recyclerView = findViewById(R.id.purchasedRecyclerView);
        emptyText = findViewById(R.id.emptyPurchasedText);
        purchasedCount = findViewById(R.id.purchasedCount);

        backBtn.setOnClickListener(v -> finish());

        // Données de test
        loadTestData();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (purchasedMovies.isEmpty()) {
            emptyText.setVisibility(android.view.View.VISIBLE);
            recyclerView.setVisibility(android.view.View.GONE);
        } else {
            // TODO: Setup adapter avec les données réelles
            purchasedCount.setText(purchasedMovies.size() + " films");
        }
    }

    private void loadTestData() {
        // Simuler des données
        purchasedMovies.add(new PurchasedMovie("Inception", "15/06/2024", "20:00", 4.8));
        purchasedMovies.add(new PurchasedMovie("Interstellar", "20/07/2024", "19:30", 4.9));
        purchasedMovies.add(new PurchasedMovie("The Dark Knight", "10/08/2024", "21:00", 4.7));
    }

    // Classe interne pour les données
    static class PurchasedMovie {
        String title, date, time;
        double rating;

        PurchasedMovie(String title, String date, String time, double rating) {
            this.title = title;
            this.date = date;
            this.time = time;
            this.rating = rating;
        }
    }
}