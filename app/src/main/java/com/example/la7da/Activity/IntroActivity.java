package com.example.la7da.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.la7da.Helper.AgeClassifierHelper;
import com.example.la7da.R;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IntroActivity extends AppCompatActivity {

    // ==================== Vues ====================
    private Button getInBtn;
    private Button ageVerifyBtn;
    private ImageView captureBtn;
    private ImageView closeCameraBtn;
    private ImageView switchCameraBtn;
    private TextView statusText;
    private TextView ageResultText;
    private TextView cameraInstruction;
    private ProgressBar analysisProgress;
    private PreviewView previewView;
    private View cameraContainer;
    private View scrollView;

    // ==================== Caméra ====================
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private int currentLensFacing = CameraSelector.LENS_FACING_FRONT;

    // ==================== Détection de visage ====================
    private FaceDetector faceDetector;

    // ==================== Classification d'âge ====================
    private AgeClassifierHelper ageClassifier;

    // ==================== État ====================
    private boolean isAdultVerified = false;
    private boolean isCameraActive = false;
    private Bitmap capturedBitmap = null;
    private Face detectedFace = null;

    // ==================== Constantes ====================
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final String TAG = "IntroActivity";

    // ==================== Cycle de vie ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initViews();
        setupFaceDetector();
        loadAgeClassifier();
        setupClickListeners();
        cameraExecutor = Executors.newSingleThreadExecutor();

        autoTestModel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCamera();
            } else {
                Toast.makeText(this,
                        "Camera permission is required for age verification",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cleanupResources();
    }

    // ==================== Initialisation ====================

    private void initViews() {
        getInBtn = findViewById(R.id.button);
        ageVerifyBtn = findViewById(R.id.ageVerifyBtn);
        captureBtn = findViewById(R.id.captureBtn);
        closeCameraBtn = findViewById(R.id.closeCameraBtn);
        switchCameraBtn = findViewById(R.id.switchCameraBtn);
        statusText = findViewById(R.id.statusText);
        ageResultText = findViewById(R.id.ageResultText);
        cameraInstruction = findViewById(R.id.cameraInstruction);
        previewView = findViewById(R.id.previewView);
        analysisProgress = findViewById(R.id.analysisProgress);
        cameraContainer = findViewById(R.id.cameraContainer);
        scrollView = findViewById(R.id.scrollView);
        disableGetInButton();
    }

    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.3f)
                .enableTracking()
                .build();

        faceDetector = FaceDetection.getClient(options);
    }

    private void loadAgeClassifier() {
        try {
            String[] assetsList = getAssets().list("");
            boolean modelFound = false;
            if (assetsList != null) {
                for (String asset : assetsList) {
                    if ("AndroidAge.tflite".equals(asset)) {
                        modelFound = true;
                        break;
                    }
                }
            }

            if (modelFound) {
                android.util.Log.d(TAG, "✅ AndroidAge.tflite found in assets");
                ageClassifier = new AgeClassifierHelper(this);
                android.util.Log.d(TAG, "✅ Age classifier loaded");
            } else {
                android.util.Log.w(TAG, "⚠️ AndroidAge.tflite not found, using fallback");
                ageClassifier = null;
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "❌ Failed to load age classifier: " + e.getMessage());
            e.printStackTrace();
            ageClassifier = null;
        }
    }

    private void setupClickListeners() {
        getInBtn.setOnClickListener(v -> {
            if (isAdultVerified) {
                navigateToLogin();
            } else {
                Toast.makeText(this,
                        "Please verify your age first using the camera",
                        Toast.LENGTH_LONG).show();
            }
        });

        ageVerifyBtn.setOnClickListener(v -> {
            if (hasCameraPermission()) {
                showCamera();
            } else {
                requestCameraPermission();
            }
        });

        captureBtn.setOnClickListener(v -> captureAndAnalyze());
        closeCameraBtn.setOnClickListener(v -> hideCamera());
        switchCameraBtn.setOnClickListener(v -> switchCamera());

        View logoImage = findViewById(R.id.logoImage);
        if (logoImage != null) {
            logoImage.setOnLongClickListener(v -> {
                runFullDiagnostic();
                return true;
            });
        }

        TextView titleText = findViewById(R.id.textView);
        if (titleText != null) {
            titleText.setOnLongClickListener(v -> {
                testAgeModel();
                return true;
            });
        }
    }

    // ==================== Permissions ====================

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(this,
                    "Camera permission is needed for age verification",
                    Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);
    }

    // ==================== Gestion de la caméra ====================

    private void showCamera() {
        if (isCameraActive) return;

        scrollView.setVisibility(View.GONE);
        cameraContainer.setVisibility(View.VISIBLE);
        previewView.setVisibility(View.VISIBLE);

        resetCameraUI();
        startCamera();
        isCameraActive = true;
    }

    private void hideCamera() {
        scrollView.setVisibility(View.VISIBLE);
        cameraContainer.setVisibility(View.GONE);
        previewView.setVisibility(View.GONE);
        isCameraActive = false;

        if (isAdultVerified) {
            showVerifiedStatus();
        }
    }

    private void resetCameraUI() {
        cameraInstruction.setText("Center your face in the frame");
        ageResultText.setText("");
        analysisProgress.setVisibility(View.GONE);
        captureBtn.setEnabled(true);
        capturedBitmap = null;
        detectedFace = null;
    }

    private void switchCamera() {
        currentLensFacing = (currentLensFacing == CameraSelector.LENS_FACING_FRONT)
                ? CameraSelector.LENS_FACING_BACK
                : CameraSelector.LENS_FACING_FRONT;
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // ✅ FIX: Force JPEG format to ensure correct buffer encoding
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(currentLensFacing)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this,
                        "Failed to start camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                hideCamera();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ==================== Capture et analyse ====================

    private void captureAndAnalyze() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d(TAG, "Starting capture...");
        cameraInstruction.setText("Capturing...");
        analysisProgress.setVisibility(View.VISIBLE);
        captureBtn.setEnabled(false);
        capturedBitmap = null;
        detectedFace = null;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy image) {
                        android.util.Log.d(TAG, "Image captured successfully");
                        capturedBitmap = imageProxyToBitmap(image);
                        image.close();

                        if (capturedBitmap != null) {
                            android.util.Log.d(TAG, "Bitmap ready: " +
                                    capturedBitmap.getWidth() + "x" + capturedBitmap.getHeight());
                            detectFaces(capturedBitmap);
                        } else {
                            android.util.Log.e(TAG, "Bitmap conversion returned null");
                            onCaptureFailed("Failed to process image. Please try again.");
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        android.util.Log.e(TAG, "Capture error: " + exception.getMessage());
                        onCaptureFailed("Capture failed: " + exception.getMessage());
                    }
                });
    }

    /**
     * ✅ FIXED: Converts ImageProxy to Bitmap using JPEG byte decoding.
     * The previous implementation used copyPixelsFromBuffer() directly on a YUV buffer,
     * which produced garbled or null results. CameraX captures in YUV_420_888 by default,
     * where planes[0] only contains the Y (luminance) channel — not full ARGB pixels.
     * BitmapFactory.decodeByteArray() correctly decodes the JPEG-encoded bytes.
     */
    private Bitmap imageProxyToBitmap(ImageProxy image) {
        try {
            // Step 1: Try toBitmap() first (CameraX 1.1+), handles YUV internally
            try {
                Bitmap rawBitmap = image.toBitmap();
                if (rawBitmap != null) {
                    android.util.Log.d(TAG, "toBitmap() succeeded");
                    return applyRotationAndMirror(rawBitmap, image);
                }
            } catch (Exception e) {
                android.util.Log.w(TAG, "toBitmap() not available, falling back to JPEG decode");
            }

            // Step 2: Fallback — decode JPEG bytes from buffer
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            if (planes == null || planes.length == 0) {
                android.util.Log.e(TAG, "No planes available in ImageProxy");
                return null;
            }

            ByteBuffer buffer = planes[0].getBuffer();
            if (buffer == null) {
                android.util.Log.e(TAG, "Buffer is null");
                return null;
            }

            buffer.rewind();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            // ✅ BitmapFactory correctly decodes JPEG bytes
            Bitmap rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (rawBitmap == null) {
                android.util.Log.e(TAG, "BitmapFactory.decodeByteArray() returned null");
                return null;
            }

            android.util.Log.d(TAG, "JPEG decode succeeded: "
                    + rawBitmap.getWidth() + "x" + rawBitmap.getHeight());

            return applyRotationAndMirror(rawBitmap, image);

        } catch (Exception e) {
            android.util.Log.e(TAG, "imageProxyToBitmap error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Applies rotation and front-camera horizontal mirror to a raw bitmap.
     */
    private Bitmap applyRotationAndMirror(Bitmap source, ImageProxy image) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();

        int rotation = image.getImageInfo().getRotationDegrees();
        if (rotation != 0) {
            matrix.postRotate(rotation);
        }

        if (currentLensFacing == CameraSelector.LENS_FACING_FRONT) {
            matrix.postScale(-1, 1, source.getWidth() / 2f, source.getHeight() / 2f);
        }

        Bitmap result = Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        if (result != source) {
            source.recycle();
        }

        return result;
    }

    @SuppressWarnings("UnsafeOptInUsageError")
    private void detectFaces(Bitmap bitmap) {
        if (bitmap == null) {
            onAnalysisFailed("Invalid image for face detection");
            return;
        }

        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

        faceDetector.process(inputImage)
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()) {
                        onAnalysisFailed("No face detected. Please try again.");
                    } else {
                        detectedFace = faces.get(0);
                        for (Face face : faces) {
                            if (face.getBoundingBox().width() >
                                    detectedFace.getBoundingBox().width()) {
                                detectedFace = face;
                            }
                        }
                        cameraInstruction.setText("Face detected! Analyzing age...");
                        classifyAge();
                    }
                })
                .addOnFailureListener(e -> onAnalysisFailed("Face detection failed"));
    }

    private void classifyAge() {
        if (detectedFace == null || capturedBitmap == null) {
            onAnalysisFailed("No face data available");
            return;
        }

        cameraExecutor.execute(() -> {
            try {
                // ✅ ML Kit (face features) = décision finale
                // Analyse directement les propriétés du visage déjà détecté :
                // ratio facial, taille, sourire, ouverture des yeux
                AgeClassifierHelper.AgeResult mlKitResult = fallbackClassifyAge();

                android.util.Log.d(TAG, "ML Kit decision → Adult: "
                        + Math.round(mlKitResult.getAdultProbability() * 100) + "% / Child: "
                        + Math.round(mlKitResult.getChildProbability() * 100) + "%");

                runOnUiThread(() -> {
                    analysisProgress.setVisibility(View.GONE);
                    captureBtn.setEnabled(true);

                    if (mlKitResult.isAdult()) {
                        onAdultVerified("Adult ("
                                + Math.round(mlKitResult.getAdultProbability() * 100)
                                + "% confidence)");
                    } else {
                        onNotAdultVerified("Not Adult ("
                                + Math.round(mlKitResult.getChildProbability() * 100)
                                + "% confidence)");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    analysisProgress.setVisibility(View.GONE);
                    captureBtn.setEnabled(true);
                    onAnalysisFailed("Age analysis failed");
                });
            }
        });
    }

    private Bitmap extractFaceBitmap(Bitmap fullBitmap, Face face) {
        try {
            android.graphics.Rect bounds = face.getBoundingBox();
            int marginX = (int) (bounds.width() * 0.3);
            int marginY = (int) (bounds.height() * 0.3);
            int left = Math.max(0, bounds.left - marginX);
            int top = Math.max(0, bounds.top - marginY);
            int right = Math.min(fullBitmap.getWidth(), bounds.right + marginX);
            int bottom = Math.min(fullBitmap.getHeight(), bounds.bottom + marginY);
            int width = right - left;
            int height = bottom - top;
            if (width > 0 && height > 0) {
                return Bitmap.createBitmap(fullBitmap, left, top, width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AgeClassifierHelper.AgeResult fallbackClassifyAge() {
        if (detectedFace == null) {
            return new AgeClassifierHelper.AgeResult(0, 0, false);
        }

        float smilingProb = detectedFace.getSmilingProbability() != null ?
                detectedFace.getSmilingProbability() : 0.5f;
        float leftEyeOpenProb = detectedFace.getLeftEyeOpenProbability() != null ?
                detectedFace.getLeftEyeOpenProbability() : 0.5f;
        float rightEyeOpenProb = detectedFace.getRightEyeOpenProbability() != null ?
                detectedFace.getRightEyeOpenProbability() : 0.5f;

        android.graphics.Rect bounds = detectedFace.getBoundingBox();
        float faceWidth = bounds.width();
        float faceHeight = bounds.height();
        float faceRatio = faceHeight / Math.max(faceWidth, 1);
        float faceArea = faceWidth * faceHeight;

        double adultScore = 0.0;
        double childScore = 0.0;

        if (faceRatio > 1.2 && faceRatio < 1.6) adultScore += 0.3;
        else if (faceRatio <= 1.2) childScore += 0.3;
        if (faceArea > 50000) adultScore += 0.25;
        else if (faceArea < 20000) childScore += 0.25;
        if (smilingProb > 0.3 && smilingProb < 0.8) adultScore += 0.25;
        float eyeOpenAvg = (leftEyeOpenProb + rightEyeOpenProb) / 2.0f;
        if (eyeOpenAvg > 0.5) adultScore += 0.2;

        double totalScore = adultScore + childScore;
        if (totalScore == 0) {
            adultScore = 0.5;
            childScore = 0.5;
        } else {
            adultScore = adultScore / totalScore;
            childScore = childScore / totalScore;
        }

        return new AgeClassifierHelper.AgeResult(
                (float) adultScore, (float) childScore, adultScore > 0.5);
    }

    // ==================== Test du modèle ====================

    private void testAgeModel() {
        Bitmap testBitmap = createTestFaceBitmap();
        if (testBitmap == null) {
            showTestResult("Failed to create test image", false);
            return;
        }

        Toast.makeText(this, "Testing age model...", Toast.LENGTH_SHORT).show();

        cameraExecutor.execute(() -> {
            try {
                if (ageClassifier == null || !ageClassifier.isLoaded()) {
                    runOnUiThread(() -> showTestResult(
                            "Age model not loaded.\nUsing fallback method.", false));
                    return;
                }

                float[] probabilities = ageClassifier.classify(testBitmap);

                if (probabilities == null || probabilities.length == 0) {
                    runOnUiThread(() -> showTestResult("Model returned empty result", false));
                    return;
                }

                float adultProb = probabilities.length > 1 ? probabilities[1] : probabilities[0];
                float childProb = probabilities.length > 1 ? probabilities[0] : (1 - probabilities[0]);

                AgeClassifierHelper.AgeResult fallbackResult = fallbackClassifyAge();

                String testDetails = String.format(
                        "✅ Model Working!\n\n" +
                                "━━━ TensorFlow Lite Model ━━━\n" +
                                "  • Adult: %.0f%%\n" +
                                "  • Child: %.0f%%\n" +
                                "  • Decision: %s\n\n" +
                                "━━━ Fallback (ML Kit) ━━━\n" +
                                "  • Adult: %.0f%%\n" +
                                "  • Child: %.0f%%\n" +
                                "  • Decision: %s\n\n" +
                                "━━━ System ━━━\n" +
                                "  • Camera: %s\n" +
                                "  • Executor: Running",
                        adultProb * 100,
                        childProb * 100,
                        adultProb > childProb ? "ADULT ✅" : "CHILD",
                        fallbackResult.getAdultProbability() * 100,
                        fallbackResult.getChildProbability() * 100,
                        fallbackResult.isAdult() ? "ADULT ✅" : "CHILD",
                        hasCameraPermission() ? "Granted" : "Denied"
                );

                runOnUiThread(() -> showTestResult(testDetails, true));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showTestResult("Exception: " + e.getMessage(), false));
            } finally {
                if (!testBitmap.isRecycled()) testBitmap.recycle();
            }
        });
    }

    private Bitmap createTestFaceBitmap() {
        try {
            int width = 224;
            int height = 224;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            int[] pixels = new int[width * height];
            int centerX = width / 2;
            int centerY = height / 2;
            int faceRadius = width / 3;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double distanceFromCenter = Math.sqrt(
                            Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                    int color;
                    if (distanceFromCenter < faceRadius) {
                        double variation = Math.sin(x * 0.05) * Math.cos(y * 0.05) * 20;
                        int skinTone = (int) (200 + variation);
                        skinTone = Math.max(0, Math.min(255, skinTone));
                        boolean isEye = false;
                        if (y > centerY - 40 && y < centerY - 10) {
                            if ((x > centerX - 50 && x < centerX - 20) ||
                                    (x > centerX + 20 && x < centerX + 50)) {
                                isEye = true;
                            }
                        }
                        if (isEye) {
                            color = 0xFF000000;
                        } else {
                            color = 0xFF000000 | (skinTone << 16) |
                                    ((int) (skinTone * 0.7) << 8) | (int) (skinTone * 0.5);
                        }
                    } else {
                        color = 0xFF333333;
                    }
                    pixels[y * width + x] = color;
                }
            }
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showTestResult(String message, boolean success) {
        new AlertDialog.Builder(this)
                .setTitle(success ? "✅ Model Test Passed" : "❌ Model Test Failed")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void autoTestModel() {
        cameraExecutor.execute(() -> {
            try {
                Thread.sleep(1500);
                if (ageClassifier != null && ageClassifier.isLoaded()) {
                    Bitmap testBitmap = createTestFaceBitmap();
                    if (testBitmap != null) {
                        float[] result = ageClassifier.classify(testBitmap);
                        final boolean modelWorks = (result != null && result.length > 0);
                        runOnUiThread(() -> {
                            if (!modelWorks) {
                                statusText.setVisibility(View.VISIBLE);
                                statusText.setText("⚠️ Age model may not work properly");
                                statusText.setTextColor(0xFFFFC107);
                            }
                        });
                        testBitmap.recycle();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void runFullDiagnostic() {
        StringBuilder report = new StringBuilder();
        report.append("🔍 AGE VERIFICATION DIAGNOSTIC\n");
        report.append("═══════════════════════════\n\n");

        report.append("📷 Camera Permission: ");
        report.append(hasCameraPermission() ? "✅ GRANTED\n" : "❌ DENIED\n");

        report.append("🧠 TF Lite Model: ");
        report.append(ageClassifier != null && ageClassifier.isLoaded() ?
                "✅ LOADED\n" : "❌ NOT LOADED\n");

        report.append("👤 Face Detector: ");
        report.append(faceDetector != null ? "✅ READY\n" : "❌ NOT READY\n");

        report.append("📁 Model File: ");
        try {
            String[] assets = getAssets().list("");
            boolean found = false;
            if (assets != null) {
                for (String asset : assets) {
                    if ("AndroidAge.tflite".equals(asset)) {
                        found = true;
                        break;
                    }
                }
            }
            report.append(found ? "✅ FOUND\n" : "❌ NOT FOUND\n");
        } catch (Exception e) {
            report.append("❌ Error\n");
        }

        report.append("⚙️ Executor: ");
        report.append(cameraExecutor != null && !cameraExecutor.isShutdown() ?
                "✅ RUNNING\n" : "❌ STOPPED\n");

        report.append("📸 Camera: ");
        report.append(isCameraActive ? "✅ ACTIVE\n" : "⬜ INACTIVE\n");

        report.append("🔞 Verified: ");
        report.append(isAdultVerified ? "✅ YES\n" : "⬜ NO\n");

        new AlertDialog.Builder(this)
                .setTitle("System Diagnostic")
                .setMessage(report.toString())
                .setPositiveButton("OK", null)
                .setNegativeButton("Test Model", (dialog, which) -> testAgeModel())
                .show();
    }

    // ==================== Gestion des résultats ====================

    private void onAdultVerified(String message) {
        isAdultVerified = true;
        ageResultText.setText("✅ " + message);
        ageResultText.setTextColor(0xFF4CAF50);
        cameraInstruction.setText("Verification Successful! 🎉");
        enableGetInButton();
        new Handler(Looper.getMainLooper()).postDelayed(this::hideCamera, 2000);
    }

    private void onNotAdultVerified(String message) {
        isAdultVerified = false;
        ageResultText.setText("❌ " + message);
        ageResultText.setTextColor(0xFFFF0000);
        cameraInstruction.setText("Access Denied - 18+ Only");
        disableGetInButton();
    }

    private void onCaptureFailed(String message) {
        analysisProgress.setVisibility(View.GONE);
        captureBtn.setEnabled(true);
        cameraInstruction.setText("Center your face in the frame");
        ageResultText.setText("❌ " + message);
        ageResultText.setTextColor(0xFFFF0000);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void onAnalysisFailed(String message) {
        analysisProgress.setVisibility(View.GONE);
        captureBtn.setEnabled(true);
        cameraInstruction.setText("Center your face in the frame");
        ageResultText.setText("❌ " + message);
        ageResultText.setTextColor(0xFFFF0000);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // ==================== Navigation ====================

    private void navigateToLogin() {
        Toast.makeText(this, "Welcome to Theatron! 🎬", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(IntroActivity.this, LoginActivity.class));
        finish();
    }

    // ==================== UI Helpers ====================

    private void enableGetInButton() {
        getInBtn.setEnabled(true);
        getInBtn.setBackgroundResource(R.drawable.btn_enabled_background);
        getInBtn.setTextColor(0xFFFFFFFF);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("✅ Age verified. You can now enter!");
        statusText.setTextColor(0xFF4CAF50);
    }

    private void disableGetInButton() {
        getInBtn.setEnabled(false);
        getInBtn.setBackgroundResource(R.drawable.btn_disabled_background);
        getInBtn.setTextColor(0xFF666666);
        if (statusText != null) {
            statusText.setVisibility(View.GONE);
        }
    }

    private void showVerifiedStatus() {
        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText("✅ Age verified. Welcome to Theatron!");
            statusText.setTextColor(0xFF4CAF50);
        }
    }

    // ==================== Nettoyage ====================

    private void cleanupResources() {
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (faceDetector != null) {
            faceDetector.close();
        }
        if (ageClassifier != null) {
            ageClassifier.close();
        }
        if (capturedBitmap != null && !capturedBitmap.isRecycled()) {
            capturedBitmap.recycle();
        }
    }
}