/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.is.efacerecognitionmodule.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.hardware.camera2.CameraCharacteristics;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Size;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.is.efacerecognitionmodule.R;
import com.is.efacerecognitionmodule.data.model.Recognition;
import com.is.efacerecognitionmodule.data.model.ResultSuccess;
import com.is.efacerecognitionmodule.domain.service.SimilarityClassifier;
import com.is.efacerecognitionmodule.domain.service.TFLiteFaceRecognitionModel;
import com.is.efacerecognitionmodule.ui.camera.CameraActivity;
import com.is.efacerecognitionmodule.ui.custom_view.GraphicOverlayView;
import com.is.efacerecognitionmodule.ui.dialog.AppAlertDialog;
import com.is.efacerecognitionmodule.ui.recognition.RecognitionSuccessActivity;
import com.is.efacerecognitionmodule.utils.BorderedText;
import com.is.efacerecognitionmodule.utils.ImageUtils;
import com.is.efacerecognitionmodule.utils.Logger;
import com.is.efacerecognitionmodule.utils.constant.MobileFaceNet;
import com.is.efacerecognitionmodule.utils.tracking.MultiBoxTracker;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.is.efacerecognitionmodule.ui.recognition.RecognitionSuccessActivity.EXTRA_RESULT_SUCCESS;
import static com.is.efacerecognitionmodule.utils.constant.MobileFaceNet.TF_OD_API_INPUT_SIZE;
import static com.is.efacerecognitionmodule.utils.constant.MobileFaceNet.TF_OD_API_IS_QUANTIZED;
import static com.is.efacerecognitionmodule.utils.constant.MobileFaceNet.TF_OD_API_MODEL_FILE;


/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */
public class LiveRecognitionActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();


    //private static final String TF_OD_API_LABELS_FILE = "file:///android_asset/labelmap.txt";

    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = MobileFaceNet.ConfidenceLevel.LEVEL08F;
    private static final boolean MAINTAIN_ASPECT = false;

    @SuppressLint("NewApi")
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    //private static final int CROP_SIZE = 320;
    //private static final Size CROP_SIZE = new Size(320, 320);

    private static final boolean SAVE_PREVIEW_BITMAP = false;
    private static final float TEXT_SIZE_DIP = 10;

    GraphicOverlayView trackingOverlay;
    private Integer sensorOrientation;

    private SimilarityClassifier detector;

    private long lastProcessingTimeMs;
    /**
     * where the preview frame is copied.
     */
    private Bitmap rgbFrameBitmap = null;
    /**
     * which is originally used to feed the inference model.
     */
    private Bitmap croppedBitmap = null;

    private Bitmap cropCopyBitmap = null;

    private boolean computingDetection = false;
    private final AtomicBoolean addPending = new AtomicBoolean(false);
    //private boolean adding = false;

    private long timestamp = 0;
    /**
     * converts coordinates from the original bitmap to the cropped bitmap space.
     */
    private Matrix frameToCropTransform;
    /**
     * converts the cropped bitmap space to coordinates the original bitmap .
     */
    private Matrix cropToFrameTransform;
    //private Matrix cropToPortraitTransform;

    private MultiBoxTracker tracker;

    // Face detector
    private FaceDetector faceDetector;

    // here the preview image is drawn in portrait way
    /**
     * rotate the input frame in portrait mode for devices that have the sensor in landscape orientation.
     */
    private Bitmap portraitBmp = null;
    // here the face is cropped and drawn
    /**
     * bitmap is used to draw every detected face, cropping its detected location,
     * and re-scaling to 112 x 112 px to be used as input for our MobileFaceNet model.
     */
    private Bitmap faceBmp = null;

    //private HashMap<String, Classifier.Recognition> knownFaces = new HashMap<>();

    private final AtomicBoolean hasSuccessVerify = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupFloatingActionButton();

        // Real-time contour detection of multiple faces
        setupFaceDetector();

        //checkWritePermission();
        setupDetector();

    }

    @Override
    public synchronized void onStop() {
        super.onStop();
        LOGGER.d("onStop");

        if (detector!=null)detector.close();
        if (faceDetector!=null)faceDetector.close();
    }

    private void setupFloatingActionButton() {
        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(view -> onAddClick());
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


    private void onAddClick() {
        addPending.set(true);
        //Toast.makeText(this, "click", Toast.LENGTH_LONG ).show();
    }

    private void setupDetector() {
        try {
            detector = TFLiteFaceRecognitionModel.create(
                    this,
                    getAssets(),
                    TF_OD_API_MODEL_FILE,
                    /*      TF_OD_API_LABELS_FILE,*/
                    TF_OD_API_INPUT_SIZE,
                    TF_OD_API_IS_QUANTIZED
            );
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.e(e, "Exception initializing classifier!");
            Toast.makeText(getApplicationContext(), "Classifier could not be initialized", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    //first
    @SuppressLint("NewApi")
    @Override
    public void onPreviewSizeChosen(@NotNull final Size size, final int rotation) {
//        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
//        BorderedText borderedText = new BorderedText(textSizePx);
//        borderedText.setTypeface(Typeface.MONOSPACE);
//
//        tracker = new MultiBoxTracker(this);
//
//        //setupDetector();
//
//        previewWidth = size.getWidth();
//        previewHeight = size.getHeight();
//        sensorOrientation = rotation - getScreenOrientation();
//        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);
//
//        LOGGER.i("Initializing at size %dx%d", previewWidth, previewHeight);
//        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
//
//
//        int targetW, targetH;
//        if (sensorOrientation == 90 || sensorOrientation == 270) {
//            targetH = previewWidth;
//            targetW = previewHeight;
//        }
//        else {
//            targetW = previewWidth;
//            targetH = previewHeight;
//        }
//        int cropW = (int) (targetW / 2.0);
//        int cropH = (int) (targetH / 2.0);
//
//        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);
//        portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
//        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);
//
//        frameToCropTransform = ImageUtils.getTransformationMatrix(
//                previewWidth, previewHeight,
//                cropW, cropH,
//                sensorOrientation, MAINTAIN_ASPECT);
//
//      //    frameToCropTransform =
////            ImageUtils.getTransformationMatrix(
////                    previewWidth, previewHeight,
////                    previewWidth, previewHeight,
////                    sensorOrientation, MAINTAIN_ASPECT);
//
//        cropToFrameTransform = new Matrix();
//        frameToCropTransform.invert(cropToFrameTransform);
//       //        Matrix frameToPortraitTransform = ImageUtils.getTransformationMatrix(
////                        previewWidth, previewHeight,
////                        targetW, targetH,
////                        sensorOrientation, MAINTAIN_ASPECT);
//        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
//        trackingOverlay.addCallback(canvas -> {
//            tracker.draw(canvas);
//            if (isDebug()) {
//                tracker.drawDebug(canvas);
//            }
//        });
//
//        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
        setupUIComponents(size, rotation);
    }

    private void setupUIComponents(@NotNull final Size size, final int rotation) {
        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        BorderedText borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        tracker = new MultiBoxTracker(this);

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();
        sensorOrientation = rotation - getScreenOrientation();
        LOGGER.i("Camera orientation relative to screen canvas: %d", sensorOrientation);

        LOGGER.i("setupUIComponents(),Initializing at size %dx%d", previewWidth, previewHeight);
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        setupBitmaps(size);
        setupTransformsMatrices();

        //        Matrix frameToPortraitTransform = ImageUtils.getTransformationMatrix(
//                        previewWidth, previewHeight,
//                        targetW, targetH,
//                        sensorOrientation, MAINTAIN_ASPECT);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(canvas -> {
            tracker.draw(canvas);
            if (isDebug()) {
                tracker.drawDebug(canvas);
            }
        });

        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);
    }

    private void setupBitmaps(final Size size) {

        int targetW = (sensorOrientation == 90 || sensorOrientation == 270) ? size.getHeight() : size.getWidth();
        int targetH = (sensorOrientation == 90 || sensorOrientation == 270) ? size.getWidth() : size.getHeight();
        int cropW = (int) (targetW / 2.0);
        int cropH = (int) (targetH / 2.0);

        //rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropW, cropH, Config.ARGB_8888);
        portraitBmp = Bitmap.createBitmap(targetW, targetH, Config.ARGB_8888);
        faceBmp = Bitmap.createBitmap(TF_OD_API_INPUT_SIZE, TF_OD_API_INPUT_SIZE, Config.ARGB_8888);

    }

    private void setupTransformsMatrices() {
        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        croppedBitmap.getWidth(), croppedBitmap.getHeight(),
                        sensorOrientation, MAINTAIN_ASPECT);
        //    frameToCropTransform =
//            ImageUtils.getTransformationMatrix(
//                    previewWidth, previewHeight,
//                    previewWidth, previewHeight,
//                    sensorOrientation, MAINTAIN_ASPECT);
        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);
    }


    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;

        LOGGER.i("processImage(),Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();
        // here code creates a Canvas object associated with the croppedBitmap.
        // It then draws the rgbFrameBitmap onto the canvas using the specified
        // transformation matrix (frameToCropTransform), allowing for resizing or
        // re-positioning of the bitmap before it is drawn.
        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        InputImage image = InputImage.fromBitmap(croppedBitmap, 0);
        faceDetector.process(image).addOnSuccessListener(faces -> {
            if (faces.size() == 0) {
                updateResults(currTimestamp, new LinkedList<>());
                return;
            }
            runInBackground(() -> {
                if (!hasSuccessVerify.get())
                    onFacesDetected(currTimestamp, faces, addPending.getAndSet(false));
                //addPending = false;
            });
        });


    }

    @Override
    protected int getLayoutId() {
        return R.layout.tfe_od_camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(() -> detector.setUseNNAPI(isChecked));
    }


    // Face Processing
    private Matrix createTransform(final int srcWidth, final int srcHeight, final int dstWidth, final int dstHeight, final int applyRotation) {

        Matrix matrix = new Matrix();
        if (applyRotation != 0) {
            if (applyRotation % 90 != 0) {
                LOGGER.w("Rotation of %d % 90 != 0", applyRotation);
            }

            // Translate so center of image is at origin.
            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);

            // Rotate around origin.
            matrix.postRotate(applyRotation);
        }

//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;

        if (applyRotation != 0) {
            // Translate back from origin centered reference to destination frame.
            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
        }

        return matrix;

    }

    private void showRegisterFaceDialog(Recognition rec) {

        runOnUiThread(() -> {
            AppAlertDialog.showRegisterCustomDialog(LiveRecognitionActivity.this, new AppAlertDialog.Callback() {
                @Override
                public void onclickOk(String name, DialogInterface alertDialog) {
                    detector.register(name, rec);
                    //alertDialog.dismiss();
                }

                @Override
                public Bitmap getCropBitmap() {
                    return rec.getCrop();
                }
            });
        });
    }

    private void updateResults(long currTimestamp, final List<Recognition> mappedRecognitions) {

        tracker.trackResults(mappedRecognitions, currTimestamp);
        trackingOverlay.postInvalidate();
        computingDetection = false;
        //adding = false;


        if (mappedRecognitions.size() > 0) {
            LOGGER.i("updateResults(,) ,Adding results:" + mappedRecognitions.size());
            Recognition rec = mappedRecognitions.get(0);
            if (rec.getExtraEmbed() != null) {
                showRegisterFaceDialog(rec);
            }
        } else {
            LOGGER.i("updateResults(,) :mappedRecognitions.size()=0");
        }

        runOnUiThread(() -> {
            showFrameInfo(previewWidth + "x" + previewHeight);
            showCropInfo(croppedBitmap.getWidth() + "x" + croppedBitmap.getHeight());
            showInference(lastProcessingTimeMs + "ms");
        });

    }

    //------------LOGIC AFTER Faces Detected-------------------------------------------------------

    private void onFacesDetected(long currTimestamp, List<Face> faces, boolean add) {

        prepareBitmapsAndCanvas();
        // draws the original image in portrait mode.
        drawOriginalImage(new Canvas(portraitBmp));
        List<Recognition> mappedRecognitions = processFaces(currTimestamp, faces, add);

        updateResults(currTimestamp, mappedRecognitions);
    }

    private void prepareBitmapsAndCanvas() {
        cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);
        //final Canvas canvas = new Canvas(cropCopyBitmap);
        final Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(2.0f);
    }

    @NotNull
    private Matrix prepareTransformMatrix() {
        int sourceW = rgbFrameBitmap.getWidth();
        int sourceH = rgbFrameBitmap.getHeight();
        int targetW = portraitBmp.getWidth();
        int targetH = portraitBmp.getHeight();
        return createTransform(sourceW, sourceH, targetW, targetH, sensorOrientation);
    }

    private void drawOriginalImage(@NotNull final Canvas cv) {
        cv.drawBitmap(rgbFrameBitmap, prepareTransformMatrix(), null);
    }

    @NotNull
    private List<Recognition> processFaces(long currTimestamp, @NotNull List<Face> faces, boolean add) {
        final List<Recognition> mappedRecognitions = new LinkedList<>();
        final Canvas cvFace = new Canvas(faceBmp);

        for (Face face : faces) {
            LOGGER.i("FACE" + face.toString());
            LOGGER.i("Running detection on face " + currTimestamp);

            final RectF boundingBox = new RectF(face.getBoundingBox());
            if (boundingBox != null) {
                // maps crop coordinates to original
                cropToFrameTransform.mapRect(boundingBox);

                // maps original coordinates to portrait coordinates
                RectF faceBB = new RectF(boundingBox);
                prepareTransformMatrix().mapRect(faceBB);

                // translates portrait to origin and scales to fit input inference size
                //cv.drawRect(faceBB, paint);
                float sx = ((float) TF_OD_API_INPUT_SIZE) / faceBB.width();
                float sy = ((float) TF_OD_API_INPUT_SIZE) / faceBB.height();
                Matrix matrix = new Matrix();
                matrix.postTranslate(-faceBB.left, -faceBB.top);
                matrix.postScale(sx, sy);

                cvFace.drawBitmap(portraitBmp, matrix, null);

                String label = "";
                float confidence = -1f;
                int color = Color.RED;//Color.BLUE;
                Object extra = null;
                Bitmap crop = null;

                if (add) {
                    crop = Bitmap.createBitmap(portraitBmp,
                            (int) faceBB.left,
                            (int) faceBB.top,
                            (int) faceBB.width(),
                            (int) faceBB.height());
                }

                final long startTime = SystemClock.uptimeMillis();
                final List<Recognition> resultsAux = detector.recognizeImage(faceBmp, add);
                lastProcessingTimeMs = SystemClock.uptimeMillis() - startTime;

                if (resultsAux.size() > 0) {
                    Recognition result = resultsAux.get(0);
                    extra = result.getExtraEmbed();
                    float conf = result.getDistance();
                    if (conf < MINIMUM_CONFIDENCE_TF_OD_API) {
                        confidence = conf;
                        label = result.getTitle();
                        color = result.getId().equals("1") ? Color.GREEN : Color.RED;
                        //showSuccessDialog(result);
                        startSuccessActivity(result);
                    }
                }

                if (getCameraFacing() == CameraCharacteristics.LENS_FACING_FRONT) {
                    Matrix flip = new Matrix();
                    if (sensorOrientation == 90 || sensorOrientation == 270) {
                        flip.postScale(1, -1, previewWidth / 2.0f, previewHeight / 2.0f);
                    } else {
                        flip.postScale(-1, 1, previewWidth / 2.0f, previewHeight / 2.0f);
                    }
                    flip.mapRect(boundingBox);
                }

                final Recognition.Builder builder = new Recognition.Builder()
                        .setId("0")
                        .setTitle(label)
                        .setConfidence(confidence)
                        .setColor(color)
                        .setLocation(boundingBox)
                        .setExtraEmbed(extra)
                        .setCrop(crop);

//                    final Recognition result = new Recognition("0", label, confidence, boundingBox);
//                    result.setColor(color);
//                    result.setLocation(boundingBox);
//                    result.setExtraEmbed(extra);
//                    result.setCrop(crop);
//
                mappedRecognitions.add(builder.create());
            }
        }
        return mappedRecognitions;
    }

//---------------TASK AFTER SUCCESS RECOGNITION------------------------------------------------

    private void startSuccessActivity(@NotNull Recognition recognition){
        LOGGER.d("startSuccessActivity");
        hasSuccessVerify.set(true);
        ResultSuccess resultSuccess=new ResultSuccess(recognition.getTitle(),recognition.getDistance());
        runOnUiThread(() -> {
            Intent intent=new Intent(getApplicationContext(), RecognitionSuccessActivity.class);
            intent.putExtra(EXTRA_RESULT_SUCCESS,resultSuccess);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             overridePendingTransition(R.anim.animate_zoom_enter, R.anim.animate_zoom_exit);
            startActivity(intent);
            overridePendingTransition(R.anim.animate_zoom_enter, R.anim.animate_zoom_exit);
            finish();
        });

    }

    private void showSuccessDialog(@NotNull Recognition recognition) {

        hasSuccessVerify.set(true);
        trackingOverlay.postInvalidate();

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.success_verify_dialog, null);
        ImageView ivFace = dialogLayout.findViewById(R.id.iv_face);
        TextView tvName = dialogLayout.findViewById(R.id.tv_name);
        TextView tvDistance = dialogLayout.findViewById(R.id.tv_distance);

        dialogLayout.setAnimation(AnimationUtils.loadAnimation(this,R.anim.animate_zoom_enter));

        ivFace.setImageBitmap(croppedBitmap);
        tvName.setText(recognition.getTitle());
        tvDistance.setText(String.format(Locale.ENGLISH, "%.2f", recognition.getDistance()));

        runOnUiThread(() ->
                new MaterialAlertDialogBuilder(this)
                        .setView(dialogLayout)
                        .setNeutralButton("ok", (dialog, which) -> {
                            computingDetection = false;
                            hasSuccessVerify.set(false);
                            dialog.dismiss();
                            finish();
                        }).setCancelable(false)
                        .show());

    }


}
