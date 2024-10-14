package com.is.efacerecognitionmodule.domain.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Pair;

import com.is.efacerecognitionmodule.data.model.Recognition;
import com.is.efacerecognitionmodule.utils.Logger;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//class Members {
//    // outputLocations: array of shape [Batchsize, NUM_DETECTIONS,4]
//    // contains the location of detected boxes
//    protected float[][][] outputLocations;
//    // outputClasses: array of shape [Batchsize, NUM_DETECTIONS]
//    // contains the classes of detected boxes
//    protected float[][] outputClasses;
//    // outputScores: array of shape [Batchsize, NUM_DETECTIONS]
//    // contains the scores of detected boxes
//    protected float[][] outputScores;
//    // numDetections: array of shape [Batchsize]
//    // contains the number of detected boxes
//    protected float[] numDetections;
//}

public class TFLiteFaceRecognitionModel extends DataRecognitionProcessor {

    private static final Logger LOGGER = new Logger();

    private static final String PREFERENCES_FILE = "FaceRecognitionPrefs";
    private static final String REGISTERED_DATA_KEY = "registered_data";

    private final HashMap<String, Recognition> registered = new HashMap<>();
    private final SharedPreferences sharedPreferences;


    private TFLiteFaceRecognitionModel(@NotNull Context context) {
        sharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        loadRegisteredData();
    }

    @NotNull
    public static SimilarityClassifier create(Context context,
                                              AssetManager assetManager,
                                              String modelFilename,
                                             /* String labelFilename,*/
                                              int inputSize,
                                              boolean isQuantized) throws IOException {

        TFLiteFaceRecognitionModel model = new TFLiteFaceRecognitionModel(context);
       // model.loadLabels(assetManager, labelFilename);
        model.inputSize = inputSize;
        model.isModelQuantized = isQuantized;
        model.tfLite = new Interpreter(model.loadModelFile(assetManager, modelFilename));
        model.allocateBuffers();

        return model;
    }
/*
    private void loadLabels(AssetManager assetManager, String labelFilename) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open(labelFilename.split("file:///android_asset/")[1])))) {
            String line;
            while ((line = br.readLine()) != null) {
                LOGGER.w("loadLabels: " + line);
            }
        }
    }*/


    @Override
    public void register(String name, Recognition recognition) {
        registered.put(name, recognition);
        saveRegisteredData();
    }

    @Override
    public float[][] generateEmbedding(Bitmap bitmap) {
        Trace.beginSection("generateEmbedding");
        preProcessBitmap(bitmap);

        Trace.beginSection("runInference");
        runInference();
        Trace.endSection();
        Trace.endSection();

        return embeddings;
    }

    /**
     * Recognizes the image and returns a list of recognitions.
     */
    //@SuppressLint("NewApi")
    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap, boolean storeExtra) {
        Trace.beginSection("recognizeImage");
        preProcessBitmap(bitmap);

        Trace.beginSection("runInference");
        runInference();
        Trace.endSection();

        String id = "0";
        String label = "?";
        float distance = Float.MAX_VALUE;

        if (!registered.isEmpty()) {
            Pair<String, Float> nearest = findNearest(embeddings[0]);
            if (nearest != null) {
                label = nearest.first;
                distance = nearest.second;
                id = distance <= 1.0f ? "1" : "0";
                LOGGER.i("registeredSz:" + registered.size() + ",Nearest: " + label + " - Distance: " + distance);
            }
        }
        Recognition.Builder builder = new Recognition.Builder()
                .setId(id)
                .setTitle(label)
                .setConfidence(distance)
                .setLocation(new RectF());
        // Recognition recognition = new Recognition(id, label, distance, new RectF());
        if (storeExtra) {
            // recognition.setExtraEmbed(embeddings);
            builder.setExtraEmbed(embeddings);
        }
        Trace.endSection();
        return Collections.singletonList(builder.create());
        //return new ArrayList<>(List.of(recognition));
    }

    /**
     * Finds the nearest embedding in the dataset using L2 norm.
     */
    private Pair<String, Float> findNearest(float[] emb) {
        Pair<String, Float> closest = null;
        for (Map.Entry<String, Recognition> entry : registered.entrySet()) {
            String name = entry.getKey();
            float[] knownEmb = ((float[][]) entry.getValue().getExtraEmbed())[0];
            float distance = calculateL2Norm(emb, knownEmb);
            if (closest == null || distance < closest.second) {
                closest = new Pair<>(name, distance);
            }
        }
        return closest;
    }

    /**
     * Calculates the L2 norm (Euclidean distance) between two float arrays.
     */
    private float calculateL2Norm(@NotNull float[] emb1, float[] emb2) {
        float distance = 0;
        for (int i = 0; i < emb1.length; i++) {
            float diff = emb1[i] - emb2[i];
            distance += diff * diff;
        }
        return (float) Math.sqrt(distance);
    }

    private void saveRegisteredData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Recognition> entry : registered.entrySet()) {
            sb.append(entry.getKey()).append(":");
            float[] emb = ((float[][]) entry.getValue().getExtraEmbed())[0];
            for (float v : emb) {
                sb.append(v).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(";");
        }

        editor.putString(REGISTERED_DATA_KEY, sb.toString());
        editor.apply();
    }

    private void loadRegisteredData() {
        String data = sharedPreferences.getString(REGISTERED_DATA_KEY, "");
        if (!data.isEmpty()) {
            String[] users = data.split(";");
            for (String user : users) {
                String[] userData = user.split(":");
                String name = userData[0];
                String[] embStrings = userData[1].split(",");
                float[] emb = new float[embStrings.length];
                for (int i = 0; i < embStrings.length; i++) {
                    emb[i] = Float.parseFloat(embStrings[i]);
                }
                Recognition.Builder builder = new Recognition.Builder()
                        .setId("0")
                        .setTitle(name)
                        .setConfidence(0.0f)
                        .setLocation(null)
                        .setExtraEmbed(new float[][]{emb});
                Recognition recognition = builder.create();
                //("0", name, 0.0f, null);
                //recognition.setExtraEmbed(new float[][]{emb});
                registered.put(name, recognition);
            }
        }
    }

    @Override
    public void enableStatLogging(boolean logStats) {
        // Optional logging functionality
    }

    @Override
    public String getStatString() {
        return ""; // Optional statistics reporting
    }

    @Override
    public void close() {
        if (tfLite != null) {
            tfLite.close();
            tfLite = null;
        }
    }

    @Override
    public void setUseNNAPI(boolean isChecked) {
        // if (tfLite != null) tfLite.setUseNNAPI(isChecked);
    }

//    public void setNumThreads(int num_threads) {
//        // if (tfLite != null) tfLite.setNumThreads(num_threads);
//    }
}
