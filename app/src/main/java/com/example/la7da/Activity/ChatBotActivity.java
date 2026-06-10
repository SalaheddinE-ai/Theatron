package com.example.la7da.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.la7da.Adapter.ChatAdapter;
import com.example.la7da.Domain.ChatMessage;
import com.example.la7da.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageView sendBtn, backBtn, micBtn, suggestionsBtn;
    private ProgressBar loadingIndicator;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    // Suggestions
    private TextView suggestion1, suggestion2, suggestion3, suggestion4;
    private View suggestionsLayout;

    // Gemini API
    private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY";
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent?key=";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        initViews();
        setupChat();
        setupSuggestions();
        setupMicrophone();
    }

    private void initViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        backBtn = findViewById(R.id.backBtn);
        micBtn = findViewById(R.id.micBtn);
        suggestionsBtn = findViewById(R.id.suggestionsBtn);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Suggestions
        suggestion1 = findViewById(R.id.suggestion1);
        suggestion2 = findViewById(R.id.suggestion2);
        suggestion3 = findViewById(R.id.suggestion3);
        suggestion4 = findViewById(R.id.suggestion4);
        suggestionsLayout = findViewById(R.id.suggestionsLayout);

        backBtn.setOnClickListener(v -> finish());
    }

    private void setupChat() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Message de bienvenue
        addBotMessage("Hello! I'm Theatron AI assistant. 🎬\n\nI can help you:\n• Find movies by genre\n• Recommend films\n• Share trending movies\n• Tell you about actors\n\nHow can I help you today?");

        // Bouton Envoi
        sendBtn.setOnClickListener(v -> sendMessage());

        // Envoi avec la touche Entrée
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        // Bouton suggestions (afficher/masquer)
        suggestionsBtn.setOnClickListener(v -> {
            if (suggestionsLayout.getVisibility() == View.VISIBLE) {
                suggestionsLayout.setVisibility(View.GONE);
            } else {
                suggestionsLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Configure les suggestions cliquables
     */
    private void setupSuggestions() {
        suggestion1.setOnClickListener(v -> {
            String prompt = "Recommend a movie";
            messageInput.setText(prompt);
            sendMessage();
        });

        suggestion2.setOnClickListener(v -> {
            String prompt = "What are the trending movies right now?";
            messageInput.setText(prompt);
            sendMessage();
        });

        suggestion3.setOnClickListener(v -> {
            String prompt = "Who are the best actors in Hollywood?";
            messageInput.setText(prompt);
            sendMessage();
        });

        suggestion4.setOnClickListener(v -> {
            String prompt = "What are the latest new movie releases?";
            messageInput.setText(prompt);
            sendMessage();
        });
    }

    /**
     * Configure le microphone pour la recherche vocale
     */
    private void setupMicrophone() {
        micBtn.setOnClickListener(v -> startVoiceSearch());
    }

    /**
     * Lance la recherche vocale
     */
    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask me about movies...");

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
                messageInput.setText(spokenText);
                sendMessage();
            }
        }
    }

    /**
     * Envoie un message
     */
    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Ajouter le message de l'utilisateur
        addUserMessage(message);
        messageInput.setText("");

        // Masquer les suggestions après l'envoi
        suggestionsLayout.setVisibility(View.GONE);

        // Afficher le chargement
        loadingIndicator.setVisibility(View.VISIBLE);
        sendBtn.setEnabled(false);
        micBtn.setEnabled(false);

        // Appeler l'API Gemini
        callGeminiApi(message);
    }

    /**
     * Appelle l'API Gemini
     */
    private void callGeminiApi(String userMessage) {
        executorService.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                MediaType JSON = MediaType.get("application/json; charset=utf-8");

                // Prompt système pour le contexte cinéma
                String systemPrompt = "You are Theatron AI, a friendly and knowledgeable movie assistant. " +
                        "You help users with:\n" +
                        "- Finding movies based on genres, actors, or mood\n" +
                        "- Recommending similar movies\n" +
                        "- Providing movie information (plot, cast, ratings)\n" +
                        "- Sharing trending and new releases\n" +
                        "- Answering cinema-related questions\n" +
                        "Keep responses concise and engaging. Use emojis. " +
                        "If asked about trending or new movies, provide recent popular films.\n\n";

                String prompt = systemPrompt + "User: " + userMessage + "\nAssistant:";

                String jsonBody = "{\n" +
                        "  \"contents\": [{\n" +
                        "    \"parts\": [{\n" +
                        "      \"text\": \"" + escapeJson(prompt) + "\"\n" +
                        "    }]\n" +
                        "  }],\n" +
                        "  \"generationConfig\": {\n" +
                        "    \"temperature\": 0.9,\n" +
                        "    \"topK\": 1,\n" +
                        "    \"topP\": 1,\n" +
                        "    \"maxOutputTokens\": 2048\n" +
                        "  }\n" +
                        "}";

                RequestBody body = RequestBody.create(jsonBody, JSON);
                Request request = new Request.Builder()
                        .url(GEMINI_API_URL + GEMINI_API_KEY)
                        .post(body)
                        .header("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject firstCandidate = candidates.getJSONObject(0);
                        JSONObject content = firstCandidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        if (parts.length() > 0) {
                            String botReply = parts.getJSONObject(0).getString("text");
                            mainHandler.post(() -> {
                                addBotMessage(botReply);
                                resetInputState();
                            });
                        }
                    }
                } else {
                    // Fallback si l'API échoue
                    String fallbackResponse = getFallbackResponse(userMessage);
                    mainHandler.post(() -> {
                        addBotMessage(fallbackResponse);
                        resetInputState();
                    });
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
                String fallbackResponse = getFallbackResponse(userMessage);
                mainHandler.post(() -> {
                    addBotMessage(fallbackResponse);
                    resetInputState();
                });
            }
        });
    }

    /**
     * Réponses de fallback si l'API Gemini n'est pas disponible
     */
    private String getFallbackResponse(String userMessage) {
        String message = userMessage.toLowerCase();

        if (message.contains("recommend") || message.contains("suggest")) {
            return "🎬 Here are some great movies I recommend:\n\n" +
                    "🔥 **Action**: John Wick 4, Mission Impossible 7\n" +
                    "😂 **Comedy**: Barbie, The Holdovers\n" +
                    "🎭 **Drama**: Oppenheimer, Killers of the Flower Moon\n" +
                    "🚀 **Sci-Fi**: Dune Part 2, The Creator\n\n" +
                    "What genre are you in the mood for?";
        } else if (message.contains("trending") || message.contains("popular")) {
            return "🔥 **Trending Movies Right Now:**\n\n" +
                    "1. 🦸 Deadpool 3\n" +
                    "2. 🌸 Dune Part 2\n" +
                    "3. 🎀 Barbie\n" +
                    "4. 💥 Oppenheimer\n" +
                    "5. 🕷️ Spider-Man: Across the Spider-Verse\n\n" +
                    "Want more details about any of these?";
        } else if (message.contains("actor") || message.contains("cast")) {
            return "🎭 **Top Actors in Hollywood:**\n\n" +
                    "• Leonardo DiCaprio\n" +
                    "• Margot Robbie\n" +
                    "• Cillian Murphy\n" +
                    "• Timothée Chalamet\n" +
                    "• Zendaya\n" +
                    "• Ryan Gosling\n\n" +
                    "Want to know their best movies?";
        } else if (message.contains("new") || message.contains("release") || message.contains("latest")) {
            return "🍿 **Latest Movie Releases:**\n\n" +
                    "• 🦸 Deadpool 3 - July 2024\n" +
                    "• 🌸 Dune Part 2 - March 2024\n" +
                    "• 🎀 Barbie - July 2023\n" +
                    "• 💥 Oppenheimer - July 2023\n\n" +
                    "Which one interests you?";
        } else {
            return "I'm here to help with movies! 🎬\n\n" +
                    "You can ask me:\n" +
                    "• Recommend a movie\n" +
                    "• What's trending?\n" +
                    "• Best actors\n" +
                    "• New releases\n\n" +
                    "Or just type your question!";
        }
    }

    /**
     * Réinitialise l'état des boutons après une réponse
     */
    private void resetInputState() {
        loadingIndicator.setVisibility(View.GONE);
        sendBtn.setEnabled(true);
        micBtn.setEnabled(true);
    }

    /**
     * Échappe les caractères JSON
     */
    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Ajoute un message utilisateur
     */
    private void addUserMessage(String message) {
        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
    }

    /**
     * Ajoute un message du bot
     */
    private void addBotMessage(String message) {
        chatMessages.add(new ChatMessage(message, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}