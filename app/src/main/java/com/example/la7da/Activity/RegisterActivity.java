package com.example.la7da.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.la7da.R;

public class RegisterActivity extends AppCompatActivity {
    private ImageView backBtn;
    private EditText usernameEdt, emailEdt, passwordEdt, confirmPasswordEdt;
    private ProgressBar progressBar;
    private TextView loginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
    }

    private void initView() {
        backBtn = findViewById(R.id.backBtn);
        usernameEdt = findViewById(R.id.usernameEdt);
        emailEdt = findViewById(R.id.emailEdt);
        passwordEdt = findViewById(R.id.passwordEdt);
        confirmPasswordEdt = findViewById(R.id.confirmPasswordEdt);
        progressBar = findViewById(R.id.progressBar);
        loginLink = findViewById(R.id.loginLink);

        backBtn.setOnClickListener(v -> finish());

        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.registerBtn).setOnClickListener(v -> {
            String username = usernameEdt.getText().toString().trim();
            String email = emailEdt.getText().toString().trim();
            String password = passwordEdt.getText().toString().trim();
            String confirmPassword = confirmPasswordEdt.getText().toString().trim();

            // Validation
            if (username.isEmpty() || email.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this,
                        "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegisterActivity.this,
                        "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 6) {
                Toast.makeText(RegisterActivity.this,
                        "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(RegisterActivity.this,
                        "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Simuler l'inscription
                progressBar.setVisibility(View.VISIBLE);

                usernameEdt.postDelayed(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(RegisterActivity.this,
                            "Welcome to Theatron! 🎬", Toast.LENGTH_SHORT).show();

                    // Redirection vers LoginActivity
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                }, 2000);
            }
        });
    }
}