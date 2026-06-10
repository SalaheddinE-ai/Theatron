package com.example.la7da.Security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class SecurityHelper {

    private static final String KEY_ALIAS = "theatron_key";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * Stocke le token de manière sécurisée
     */
    public static void saveToken(Context context, String token) {
        try {
            String encryptedToken = encrypt(token);
            SharedPreferences prefs = context.getSharedPreferences(
                    "TheatronSecurePrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("auth_token", encryptedToken).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère le token de manière sécurisée
     */
    public static String getToken(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(
                    "TheatronSecurePrefs", Context.MODE_PRIVATE);
            String encryptedToken = prefs.getString("auth_token", null);
            if (encryptedToken != null) {
                return decrypt(encryptedToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Supprime le token
     */
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                "TheatronSecurePrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private static String encrypt(String data) throws Exception {
        SecretKey key = getSecretKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV();
        byte[] encrypted = cipher.doFinal(data.getBytes("UTF-8"));

        // Combiner IV + données encryptées
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    private static String decrypt(String encryptedData) throws Exception {
        byte[] combined = Base64.decode(encryptedData, Base64.DEFAULT);
        SecretKey key = getSecretKey();

        // Extraire IV (12 premiers bytes pour GCM)
        GCMParameterSpec spec = new GCMParameterSpec(128, combined, 0, 12);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] decrypted = cipher.doFinal(combined, 12, combined.length - 12);
        return new String(decrypted, "UTF-8");
    }

    private static SecretKey getSecretKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);

            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());

            return keyGenerator.generateKey();
        }

        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(
                KEY_ALIAS, null)).getSecretKey();
    }
}