package com.example.la7da.Network;

import com.example.la7da.Domain.*;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ==================== Auth ====================
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    // ==================== Movies ====================
    @GET("api/movies/now-playing")
    Call<Map<String, Object>> getNowPlaying();

    @GET("api/movies/upcoming")
    Call<Map<String, Object>> getUpcoming();

    @GET("api/movies/search")
    Call<Map<String, Object>> searchMovies(@Query("query") String query);

    @GET("api/movies/{movieId}")
    Call<Map<String, Object>> getMovieDetails(@Path("movieId") int movieId);

    // ==================== Favorites ====================
    @GET("api/favorites")
    Call<List<FavoriteItem>> getFavorites(@Header("Authorization") String token);

    @POST("api/favorites")
    Call<FavoriteItem> addFavorite(
            @Header("Authorization") String token,
            @Body FavoriteRequest request);

    @DELETE("api/favorites/{movieId}")
    Call<Map<String, String>> removeFavorite(
            @Header("Authorization") String token,
            @Path("movieId") int movieId);

    // ==================== Purchases ====================
    @GET("api/purchases")
    Call<List<PurchasedItem>> getPurchases(@Header("Authorization") String token);

    @POST("api/purchases")
    Call<PurchasedItem> addPurchase(
            @Header("Authorization") String token,
            @Body PurchaseRequest request);

    @DELETE("api/purchases/{movieId}")
    Call<Map<String, String>> removePurchase(
            @Header("Authorization") String token,
            @Path("movieId") int movieId);

    // ==================== ChatBot ====================
    @POST("api/chatbot/message")
    Call<ChatBotResponse> sendMessage(@Body ChatBotRequest request);

    // ==================== Trending ====================
    @GET("api/trending/movies")
    Call<Map<String, Object>> getTrendingMovies();

    // ==================== Profile ====================
    @GET("api/profile")
    Call<UserProfile> getProfile(@Header("Authorization") String token);

    @PUT("api/profile")
    Call<UserProfile> updateProfile(
            @Header("Authorization") String token,
            @Body UserProfile profile);
}