package com.example.la7da.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.la7da.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ImageView backBtn;
    private EditText emailEdt;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initView();
    }

    private void initView() {
        backBtn = findViewById(R.id.backBtn);
        emailEdt = findViewById(R.id.emailEdt);
        progressBar = findViewById(R.id.progressBar);

        backBtn.setOnClickListener(v -> finish());

        findViewById(R.id.resetBtn).setOnClickListener(v -> {
            String email = emailEdt.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Please enter your email address", Toast.LENGTH_SHORT).show();
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(ForgotPasswordActivity.this,
                        "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                // Simuler l'envoi du mail de réinitialisation
                progressBar.setVisibility(View.VISIBLE);
                findViewById(R.id.resetBtn).setEnabled(false);

                emailEdt.postDelayed(() -> {
                    progressBar.setVisibility(View.GONE);
                    findViewById(R.id.resetBtn).setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Reset link sent to " + email + " ✉️", Toast.LENGTH_LONG).show();
                    finish();
                }, 2000);
            }
        });
    }
}