package com.example.la7da.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.la7da.Domain.LoginRequest;
import com.example.la7da.Domain.LoginResponse;
import com.example.la7da.Network.ApiClient;
import com.example.la7da.Network.ApiService;
import com.example.la7da.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText userEdt, passEdt;
    private Button loginBtn;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser Retrofit
        apiService = ApiClient.getClient().create(ApiService.class);

        initView();
    }

    private void initView() {
        userEdt = findViewById(R.id.editTextText);
        passEdt = findViewById(R.id.editTextPassword);
        loginBtn = findViewById(R.id.LoginBtn);

        loginBtn.setOnClickListener(v -> {
            String username = userEdt.getText().toString().trim();
            String password = passEdt.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Appeler l'API
            loginWithApi(username, password);
        });
    }

    private void loginWithApi(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    // Sauvegarder le token
                    saveToken(loginResponse.getAccessToken());

                    Toast.makeText(LoginActivity.this,
                            "Welcome " + loginResponse.getUser().getFullName() + "! 🎬",
                            Toast.LENGTH_SHORT).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("TheatronPrefs", MODE_PRIVATE);
        prefs.edit().putString("auth_token", "Bearer " + token).apply();
    }
}