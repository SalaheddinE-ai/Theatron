package com.example.la7da.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.la7da.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView backBtn;
    private TextView profileName, profileEmail, selectedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        backBtn = findViewById(R.id.profileBackBtn);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        selectedLanguage = findViewById(R.id.selectedLanguage);
        Button logoutBtn = findViewById(R.id.logoutBtn);

        backBtn.setOnClickListener(v -> finish());

        // Sélecteur de langue
        findViewById(R.id.languageSetting).setOnClickListener(v -> showLanguageDialog());

        // Déconnexion
        logoutBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Français", "العربية"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Language")
                .setItems(languages, (dialog, which) -> {
                    selectedLanguage.setText(languages[which]);
                    Toast.makeText(this, "Language: " + languages[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}