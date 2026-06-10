package com.example.la7da.Network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Changez cette URL selon votre configuration
    // Pour émulateur Android : http://10.0.2.2:8000
    // Pour appareil physique (même WiFi) : http://192.168.254.54:8000
    // Pour production : https://votre-serveur.com

    private static final String BASE_URL = "http://192.168.254.54:8000/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}