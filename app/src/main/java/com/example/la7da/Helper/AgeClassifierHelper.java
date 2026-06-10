package com.example.la7da.Helper;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class AgeClassifierHelper {

    private FaceDetector faceDetector;

    public AgeClassifierHelper(Context context) {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.3f)
                .build();

        faceDetector = FaceDetection.getClient(options);
    }

    /**
     * Analyse un visage et retourne les probabilités.
     * @param bitmap Bitmap du visage
     * @return float[] contenant [childProbability, adultProbability]
     */
    public float[] classify(Bitmap bitmap) {
        try {
            if (bitmap == null) {
                return new float[]{0.5f, 0.5f};
            }

            InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

            // Traitement synchrone
            List<Face> faces = faceDetector.process(inputImage)
                    .getResult();

            if (faces != null && !faces.isEmpty()) {
                Face face = faces.get(0);
                return analyzeFaceFeatures(face);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Si pas de visage détecté, retourner 50/50
        return new float[]{0.5f, 0.5f};
    }

    /**
     * Analyse les caractéristiques faciales pour estimer l'âge.
     */
    private float[] analyzeFaceFeatures(Face face) {
        // Récupérer les caractéristiques du visage
        float smilingProb = face.getSmilingProbability() != null ?
                face.getSmilingProbability() : 0.5f;
        float leftEyeOpenProb = face.getLeftEyeOpenProbability() != null ?
                face.getLeftEyeOpenProbability() : 0.5f;
        float rightEyeOpenProb = face.getRightEyeOpenProbability() != null ?
                face.getRightEyeOpenProbability() : 0.5f;

        // Dimensions du visage
        android.graphics.Rect bounds = face.getBoundingBox();
        float faceWidth = bounds.width();
        float faceHeight = bounds.height();
        float faceArea = faceWidth * faceHeight;
        float faceRatio = faceHeight / Math.max(faceWidth, 1);

        double adultScore = 0.0;
        double childScore = 0.0;

        // 1. Ratio facial (adultes ont un visage plus allongé)
        if (faceRatio > 1.2 && faceRatio < 1.6) {
            adultScore += 0.30;
        } else if (faceRatio <= 1.2) {
            childScore += 0.30;
        } else {
            adultScore += 0.15;
            childScore += 0.15;
        }

        // 2. Taille du visage (adultes ont un plus grand visage)
        if (faceArea > 60000) {
            adultScore += 0.25;
        } else if (faceArea > 30000) {
            adultScore += 0.15;
            childScore += 0.10;
        } else if (faceArea < 15000) {
            childScore += 0.25;
        } else {
            adultScore += 0.10;
            childScore += 0.15;
        }

        // 3. Sourire (les adultes ont un sourire plus modéré)
        if (smilingProb > 0.2 && smilingProb < 0.7) {
            adultScore += 0.25;
        } else if (smilingProb >= 0.7) {
            childScore += 0.20;
        } else {
            adultScore += 0.10;
            childScore += 0.10;
        }

        // 4. Ouverture des yeux (les adultes ont des yeux plus ouverts)
        float eyeOpenAvg = (leftEyeOpenProb + rightEyeOpenProb) / 2.0f;
        if (eyeOpenAvg > 0.6) {
            adultScore += 0.20;
        } else if (eyeOpenAvg > 0.3) {
            adultScore += 0.10;
            childScore += 0.10;
        } else {
            childScore += 0.20;
        }

        // Normaliser les scores
        double totalScore = adultScore + childScore;
        if (totalScore == 0) {
            adultScore = 0.5;
            childScore = 0.5;
        } else {
            adultScore = adultScore / totalScore;
            childScore = childScore / totalScore;
        }

        return new float[]{(float) childScore, (float) adultScore};
    }

    public void close() {
        if (faceDetector != null) {
            faceDetector.close();
        }
    }

    public boolean isLoaded() {
        return faceDetector != null;
    }

    // ==================== INNER CLASS ====================

    /**
     * Classe résultat de la classification d'âge.
     */
    public static class AgeResult {
        private final float adultProbability;
        private final float childProbability;
        private final boolean isAdult;

        public AgeResult(float adultProbability, float childProbability, boolean isAdult) {
            this.adultProbability = adultProbability;
            this.childProbability = childProbability;
            this.isAdult = isAdult;
        }

        public float getAdultProbability() {
            return adultProbability;
        }

        public float getChildProbability() {
            return childProbability;
        }

        public boolean isAdult() {
            return isAdult;
        }
    }
}