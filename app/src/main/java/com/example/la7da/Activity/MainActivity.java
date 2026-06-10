package com.example.la7da.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.la7da.Adapter.FilmListAdapter;
import com.example.la7da.Domain.FilmItem;
import com.example.la7da.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView newMoviesRecycler, upcomingRecycler;
    private ProgressBar loading1, loading2;
    private FilmListAdapter newAdapter, upcomingAdapter;
    private EditText searchEditText;
    private TextView titleNewMovies;
    private ScrollView scrollView;

    // Barre de navigation
    private ImageView btnProfile, btnPurchased, btnFavorites, btnTrending;

    // FAB et Long Press
    private FloatingActionButton fabMain;
    private final Handler longPressHandler = new Handler(Looper.getMainLooper());
    private Runnable longPressRunnable;
    private boolean isLongPress = false;
    private static final long LONG_PRESS_DURATION = 6000; // 6 secondes

    private final String API_KEY = "020092194a657c6395c0df233409f4b3";
    private final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerViews();
        setupSearchFunctionality();
        setupBottomNavigation();
        setupFabActions();

        // Charger les films au démarrage
        fetchMovies("movie/now_playing", newAdapter, loading1);
        fetchMovies("movie/upcoming", upcomingAdapter, loading2);
    }

    /**
     * Initialisation de toutes les vues
     */
    private void initViews() {
        newMoviesRecycler = findViewById(R.id.view1);
        upcomingRecycler = findViewById(R.id.view2);
        loading1 = findViewById(R.id.loading1);
        loading2 = findViewById(R.id.loading2);
        searchEditText = findViewById(R.id.editTextText2);
        titleNewMovies = findViewById(R.id.textView8);
        scrollView = findViewById(R.id.scrollView2);

        // Barre de navigation
        btnProfile = findViewById(R.id.imageView5);    // btn_4 - Profil
        btnPurchased = findViewById(R.id.imageView4);  // btn_3 - Films achetés
        btnFavorites = findViewById(R.id.imageView3);  // btn_2 - Films favoris
        btnTrending = findViewById(R.id.imageView2);   // btn_1 - Tendances/recherches

        // Floating Action Button
        fabMain = findViewById(R.id.fabMain);
    }

    /**
     * Configuration des RecyclerViews
     */
    private void setupRecyclerViews() {
        // Configuration du RecyclerView pour les nouveaux films (horizontal)
        newMoviesRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        upcomingRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        newAdapter = new FilmListAdapter(this, new ArrayList<>());
        upcomingAdapter = new FilmListAdapter(this, new ArrayList<>());

        newMoviesRecycler.setAdapter(newAdapter);
        upcomingRecycler.setAdapter(upcomingAdapter);
    }

    /**
     * Configuration de la recherche textuelle et vocale
     */
    private void setupSearchFunctionality() {
        // Recherche textuelle en temps réel
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Non utilisé
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchMovies(s.toString());
                } else {
                    // Recharger les films par défaut si la recherche est vide
                    titleNewMovies.setText("New Movies");
                    fetchMovies("movie/now_playing", newAdapter, loading1);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Non utilisé
            }
        });

        // Recherche vocale (clic sur l'icône du microphone à droite)
        searchEditText.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null &&
                        event.getRawX() >= (searchEditText.getRight() -
                                searchEditText.getCompoundDrawables()[DRAWABLE_RIGHT]
                                        .getBounds().width() - searchEditText.getPaddingEnd())) {
                    startVoiceSearch();
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Configuration de la barre de navigation inférieure
     */
    private void setupBottomNavigation() {
        // btn_4 - Profil utilisateur
        btnProfile.setOnClickListener(v -> {
            Toast.makeText(this, "User Profile - Coming Soon", Toast.LENGTH_SHORT).show();
            // TODO: Créer l'activité ProfileActivity
            // startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        // btn_3 - Films achetés par l'utilisateur
        btnPurchased.setOnClickListener(v -> {
            Toast.makeText(this, "Purchased Movies - Coming Soon", Toast.LENGTH_SHORT).show();
            // TODO: Créer l'activité PurchasedMoviesActivity
            // startActivity(new Intent(MainActivity.this, PurchasedMoviesActivity.class));
        });

        // btn_2 - Films favoris (adorés par l'utilisateur)
        btnFavorites.setOnClickListener(v -> {
            Toast.makeText(this, "Favorite Movies - Coming Soon", Toast.LENGTH_SHORT).show();
            // TODO: Créer l'activité FavoriteMoviesActivity
            // startActivity(new Intent(MainActivity.this, FavoriteMoviesActivity.class));
        });

        // btn_1 - Films les plus recherchés/Tendances
        btnTrending.setOnClickListener(v -> {
            Toast.makeText(this, "Trending Movies", Toast.LENGTH_SHORT).show();
            loadTrendingMovies();
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        });

        btnPurchased.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PurchasedMoviesActivity.class));
        });

        btnFavorites.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, FavoriteMoviesActivity.class));
        });
    }

    /**
     * Configuration du FloatingActionButton avec clic simple et clic long
     */
    private void setupFabActions() {
        // Clic simple : Action normale du FAB
        fabMain.setOnClickListener(v -> {
            if (!isLongPress) {
                // Faire défiler vers le haut
                scrollView.smoothScrollTo(0, 0);
                Toast.makeText(MainActivity.this,
                        "Back to top", Toast.LENGTH_SHORT).show();
            }
            isLongPress = false;
        });

        // Clic long (6 secondes) pour ouvrir le chatbot
        fabMain.setOnLongClickListener(v -> {
            isLongPress = false;
            Toast.makeText(MainActivity.this,
                    "Hold for 6 seconds to open AI Assistant...", Toast.LENGTH_SHORT).show();

            // Annuler tout runnable précédent
            if (longPressRunnable != null) {
                longPressHandler.removeCallbacks(longPressRunnable);
            }

            // Programmer l'ouverture du chatbot après 6 secondes
            longPressRunnable = () -> {
                isLongPress = true;
                Toast.makeText(MainActivity.this,
                        "Opening AI Assistant...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ChatBotActivity.class);
                startActivity(intent);
            };
            longPressHandler.postDelayed(longPressRunnable, LONG_PRESS_DURATION);

            return true;
        });

        // Détecter quand l'utilisateur relâche le bouton avant 6 secondes
        fabMain.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP ||
                    event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                // Annuler le long press si relâché avant la durée requise
                if (longPressRunnable != null) {
                    longPressHandler.removeCallbacks(longPressRunnable);
                }
            }
            return false;
        });
    }

    /**
     * Lance la recherche vocale
     */
    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a movie name...");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Speech recognition not supported on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Récupère le résultat de la recherche vocale
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0);
                searchEditText.setText(spokenText);
                searchMovies(spokenText);
            }
        }
    }

    /**
     * Récupère les films depuis l'API TMDB
     */
    private void fetchMovies(String endpoint, FilmListAdapter adapter, ProgressBar loader) {
        if (loader != null) {
            loader.setVisibility(View.VISIBLE);
        }

        String url = BASE_URL + endpoint + "?api_key=" + API_KEY;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<FilmItem> films = new ArrayList<>();
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            FilmItem film = new FilmItem(
                                    obj.getInt("id"),
                                    obj.getString("title"),
                                    obj.optString("poster_path", ""),
                                    obj.optString("overview", ""),
                                    obj.optString("release_date", ""),
                                    obj.optDouble("vote_average", 0)
                            );
                            films.add(film);
                        }
                        adapter.setItems(films);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "Error loading movies", Toast.LENGTH_SHORT).show();
                    }
                    if (loader != null) {
                        loader.setVisibility(View.GONE);
                    }
                },
                error -> {
                    if (loader != null) {
                        loader.setVisibility(View.GONE);
                    }
                    Toast.makeText(MainActivity.this,
                            "Network error. Please check your connection.",
                            Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    /**
     * Recherche des films par mot-clé
     */
    private void searchMovies(String query) {
        if (query.isEmpty()) {
            titleNewMovies.setText("New Movies");
            fetchMovies("movie/now_playing", newAdapter, loading1);
            return;
        }

        loading1.setVisibility(View.VISIBLE);
        titleNewMovies.setText("Search Results");

        // Encoder la requête pour l'URL
        String encodedQuery = query.replace(" ", "%20");
        String url = BASE_URL + "search/movie?api_key=" + API_KEY + "&query=" + encodedQuery;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        List<FilmItem> films = new ArrayList<>();
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject obj = results.getJSONObject(i);
                            FilmItem film = new FilmItem(
                                    obj.getInt("id"),
                                    obj.getString("title"),
                                    obj.optString("poster_path", ""),
                                    obj.optString("overview", ""),
                                    obj.optString("release_date", ""),
                                    obj.optDouble("vote_average", 0)
                            );
                            films.add(film);
                        }
                        newAdapter.setItems(films);

                        if (films.isEmpty()) {
                            Toast.makeText(MainActivity.this,
                                    "No movies found for \"" + query + "\"",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this,
                                "Search error", Toast.LENGTH_SHORT).show();
                    }
                    loading1.setVisibility(View.GONE);
                },
                error -> {
                    loading1.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this,
                            "Network error during search", Toast.LENGTH_SHORT).show();
                });
        queue.add(request);
    }

    /**
     * Charge les films tendances/populaires
     */
    private void loadTrendingMovies() {
        titleNewMovies.setText("🔥 Trending Movies");
        fetchMovies("movie/popular", newAdapter, loading1);
        // Scroll vers le haut pour voir les résultats
        scrollView.smoothScrollTo(0, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Nettoyer le handler pour éviter les fuites de mémoire
        if (longPressRunnable != null) {
            longPressHandler.removeCallbacks(longPressRunnable);
        }
    }

}