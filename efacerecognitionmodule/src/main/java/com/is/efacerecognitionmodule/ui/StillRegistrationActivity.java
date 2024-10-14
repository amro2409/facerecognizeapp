package com.is.efacerecognitionmodule.ui;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.is.efacerecognitionmodule.R;
import com.is.efacerecognitionmodule.data.model.Recognition;
import com.is.efacerecognitionmodule.domain.service.SimilarityClassifier;
import com.is.efacerecognitionmodule.domain.service.TFLiteFaceRecognitionModel;
import com.is.efacerecognitionmodule.ui.dialog.AppAlertDialog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import me.dev.is.mllibrary.feature.detector_face.graphics.FaceGraphic;
import me.dev.is.mllibrary.feature.detector_face.graphics.GraphicOverlay;
import me.dev.is.mllibrary.feature.detector_face.graphics.InferenceInfoGraphic;
import me.dev.is.mllibrary.feature.detector_face.ui.StillImageBaseActivity;
import me.dev.is.mllibrary.feature.detector_face.utils.BitmapUtils;
import me.dev.is.mllibrary.feature.detector_face.utils.PreferenceUtils;

import static com.is.efacerecognitionmodule.utils.constant.MobileFaceNet.TF_OD_API_INPUT_SIZE;
import static com.is.efacerecognitionmodule.utils.constant.MobileFaceNet.TF_OD_API_IS_QUANTIZED;
import static com.is.efacerecognitionmodule.utils.constant.MobileFaceNet.TF_OD_API_MODEL_FILE;
import static java.lang.Math.max;

public class StillRegistrationActivity extends StillImageBaseActivity implements View.OnClickListener {
    private static final String TAG = StillRegistrationActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private GraphicOverlay graphicOverlay;
    private FaceDetector faceDetector;
    private SimilarityClassifier detector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_still_registry);
        Log.d(TAG, "onCreate ");
        findViewById(me.dev.is.mllibrary.R.id.select_image_button).setOnClickListener(this);

        previewImgV = findViewById(R.id.preview);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        progressBar = findViewById(R.id.progressBar);

        populateSizeSelector();

        isLandScape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
            selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);
            Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        }

        View rootView = findViewById(me.dev.is.mllibrary.R.id.root);
        rootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        imageMaxWidth = rootView.getWidth();
                        imageMaxHeight = rootView.getHeight() - findViewById(R.id.control).getHeight();
                        Log.d(TAG, "onGlobalLayout() called");
                        if (SIZE_SCREEN.equals(selectedSize)) {
                            tryReloadAndDetectInImage("onGlobalLayout");
                        }
                    }
                });
        setupFaceDetector();
        setupDetector();
    }


    private void setupFaceDetector() {
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setContourMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .build();
        faceDetector = FaceDetection.getClient(options);
    }

    private void setupDetector() {
        try {
            detector = TFLiteFaceRecognitionModel.create(
                    this,
                    getAssets(),
                    TF_OD_API_MODEL_FILE,
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED
            );
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Exception initializing classifier!");
            Toast.makeText(getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
       final PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(
                menuItem -> {
                    int itemId = menuItem.getItemId();
                    if (itemId == me.dev.is.mllibrary.R.id.select_images_from_local) {
                        startChooseImageIntentForResult();
                        return true;
                    } else if (itemId == me.dev.is.mllibrary.R.id.take_photo_using_camera) {
                        startCameraIntentForResult();
                        return true;
                    }
                    return false;
                });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(me.dev.is.mllibrary.R.menu.camera_button_menu, popup.getMenu());
        popup.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        //createImageProcessor();
        tryReloadAndDetectInImage("onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceDetector != null) faceDetector.close();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState() called with: outState = [" + outState + "]");
        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        outState.putString(KEY_SELECTED_SIZE, selectedSize);
    }

    private void setProgressBarVisible(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        isWorkingDetection =visible;
    }

    boolean isWorkingDetection;

    @Override
    protected void tryReloadAndDetectInImage(@NotNull String...fromCall) {
        logFormat("tryReloadAndDetectInImage",fromCall[0]);
        try {
            if (imageUri == null || isWorkingDetection) return;

            if (SIZE_SCREEN.equals(selectedSize) && imageMaxWidth == 0) {
                return;
            }
            setProgressBarVisible(true);
            Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getContentResolver(), imageUri);
            if (imageBitmap == null) {
                setProgressBarVisible(false);
                return;
            }

            logBtpTest(imageBitmap);
            graphicOverlay.clear();

            Bitmap resizedBitmap;
            if (selectedSize.equals(SIZE_ORIGINAL)) {
                resizedBitmap = imageBitmap;
            } else {
                Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();
                float scaleFactor = max(
                        ((float) imageBitmap.getWidth() / (float) targetedSize.first),//2448/720 =3.4
                        ((float) imageBitmap.getHeight() / (float) targetedSize.second));//3264/1210=2.66

                resizedBitmap = Bitmap.createScaledBitmap(
                        imageBitmap,
                        (int) (imageBitmap.getWidth() / scaleFactor),
                        (int) (imageBitmap.getHeight() / scaleFactor),
                        true);
            }

            //isRunDetection=true;
            previewImgV.setImageBitmap(resizedBitmap);
            logBtpTest(resizedBitmap);

            try {
                graphicOverlay.setImageSourceInfo(resizedBitmap.getWidth(), resizedBitmap.getHeight(), false);
                faceDetector.process(InputImage.fromBitmap(resizedBitmap, 0))
                        .addOnSuccessListener(this, faces -> {
                            Log.d(TAG, "Faces detected: " + faces.size());
                            if (faces.size() > 0) {
                                showRegisterDialog(resizedBitmap, faces.get(0).getBoundingBox());
                            }
                            for (Face face : faces) {
                                graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
                            }
                            if (!PreferenceUtils.shouldHideDetectionInfo(graphicOverlay.getContext())) {
                                graphicOverlay.add(new InferenceInfoGraphic(graphicOverlay));
                            }
                            graphicOverlay.postInvalidate();
                            setProgressBarVisible(false);
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image ", e);
            imageUri = null;
        }
    }

    @Override
    protected void createImageProcessor() {
        //
    }

    void showRegisterDialog(Bitmap source, Rect boundingBox) {
        // Crop the face from the bitmap
        Bitmap finalSource = cropFaceFromBitmap(source, boundingBox);
        logBtpTest(finalSource);
        // Resize the cropped face to match the input size of the model
        Bitmap resizedFace = Bitmap.createScaledBitmap(finalSource, TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, false);

        logBtpTest(resizedFace);

        runOnUiThread(
                () -> AppAlertDialog.showRegisterCustomDialog(StillRegistrationActivity.this, new AppAlertDialog.Callback() {
                    @Override
                    public void onclickOk(String name, DialogInterface alertDialog) {
                        // Pass the resized face to the model to generate embedding
                        float[][] emb = detector.generateEmbedding(resizedFace);
                        Recognition.Builder builder = new Recognition.Builder()
                                .setCrop(resizedFace).setExtraEmbed(emb);
                        detector.register(name, builder.create());
                        Toast.makeText(StillRegistrationActivity.this, "Face registered successfully!", Toast.LENGTH_SHORT).show();
                       // alertDialog.dismiss();
                    }

                    @Override
                    public Bitmap getCropBitmap() {
                        return resizedFace;
                    }
                }));
    }



    private Bitmap cropFaceFromBitmap(Bitmap source, Rect boundingBox) {

        if (source == null) return null;
        int left = Math.max(0, boundingBox.left);
        int top = Math.max(0, boundingBox.top);
        int width = Math.min(source.getWidth() - left, boundingBox.width());
        int height = Math.min(source.getHeight() - top, boundingBox.height());

        return Bitmap.createBitmap(source, left, top, width, height);
    }

    /*
     *     ___L_______________________X+
     *    |
     *  T |   .(L,T)
     *    |
     *    |
     *    y+
     */
}

