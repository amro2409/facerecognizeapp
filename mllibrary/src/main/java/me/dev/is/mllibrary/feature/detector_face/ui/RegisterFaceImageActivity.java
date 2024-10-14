package me.dev.is.mllibrary.feature.detector_face.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.common.annotation.KeepName;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.mlkit.vision.face.Face;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import me.dev.is.mllibrary.R;
import me.dev.is.mllibrary.feature.detector_face.SettingsActivity;
import me.dev.is.mllibrary.feature.detector_face.graphics.GraphicOverlay;
import me.dev.is.mllibrary.feature.detector_face.processor.FaceDetectorProcessor;
import me.dev.is.mllibrary.feature.detector_face.processor.VisionImageProcessor;
import me.dev.is.mllibrary.feature.detector_face.utils.BitmapUtils;

import static java.lang.Math.max;

/**
 * Activity demonstrating different image detector features with a still image from camera,galley.
 */
@KeepName
public final class RegisterFaceImageActivity extends StillImageBaseActivity {
    private static final String TAG = RegisterFaceImageActivity.class.getSimpleName();

    private GraphicOverlay graphicOverlay;

    //boolean isLandScape;
    //private int imageMaxWidth;
    //private int imageMaxHeight;
    private VisionImageProcessor imageProcessor;
    //private RegisterViewModel viewModel;
    //private FaceNetService viewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        setContentView(R.layout.activity_earn_face_image);

       //viewModel= new  ViewModelProvider(this).get(RegisterViewModel.class);
        //viewModel=new FaceNetService(this);
        findViewById(R.id.select_image_button).setOnClickListener(RegisterFaceImageActivity.this::onClick);

        previewImgV = findViewById(R.id.preview);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        populateFeatureSelector();
        populateSizeSelector();

        isLandScape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if (savedInstanceState != null) {
            imageUri = savedInstanceState.getParcelable(KEY_IMAGE_URI);
            selectedSize = savedInstanceState.getString(KEY_SELECTED_SIZE);
        }

        View rootView = findViewById(R.id.root);
        rootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Log.d(TAG, "rootView.getViewTreeObserver()->onGlobalLayout() called.");
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        imageMaxWidth = rootView.getWidth();
                        imageMaxHeight = rootView.getHeight() - findViewById(R.id.control).getHeight();
                        if (SIZE_SCREEN.equals(selectedSize)) {
                            tryReloadAndDetectInImage();
                        }
                    }
                });

        ImageView settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_LAUNCH_SOURCE, SettingsActivity.LaunchSource.STILL_IMAGE);
            startActivity(intent);
        });
    }

    private void onClick(View view) {
        // Menu for selecting either: a) take new photo b) select from existing
        PopupMenu popup = new PopupMenu(RegisterFaceImageActivity.this, view);
        popup.setOnMenuItemClickListener(
                menuItem -> {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.select_images_from_local) {
                        startChooseImageIntentForResult();
                        return true;
                    } else if (itemId == R.id.take_photo_using_camera) {
                        startCameraIntentForResult();
                        return true;
                    }
                    return false;
                });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.camera_button_menu, popup.getMenu());
        popup.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        createImageProcessor();
        tryReloadAndDetectInImage();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (imageProcessor != null) {
            imageProcessor.stop();
        }
        //viewModel.close();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_IMAGE_URI, imageUri);
        outState.putString(KEY_SELECTED_SIZE, selectedSize);
    }



    @Override
    protected void tryReloadAndDetectInImage(String...fromCall) {
        Log.d(TAG, "Try reload and detect image");
        try {
            if (imageUri == null) return;

            if (SIZE_SCREEN.equals(selectedSize) && imageMaxWidth == 0) {
                // UI layout has not finished yet, will reload once it's ready.
                return;
            }
            Log.d(TAG, "tryReloadAndDetectInImage() imageUri: " + imageUri.toString());

            Bitmap imageBitmap = BitmapUtils.getBitmapFromContentUri(getContentResolver(), imageUri);
            if (imageBitmap == null) {
                return;
            }

            // Clear the overlay first
            graphicOverlay.clear();

            Bitmap resizedBitmap;
            if (selectedSize.equals(SIZE_ORIGINAL)) {
                resizedBitmap = imageBitmap;
            } else {
                // Get the dimensions of the image view
                Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

                // Determine how much to scale down the image
                float scaleFactor = max(
                        ((float) imageBitmap.getWidth() / (float) targetedSize.first),
                        ((float) imageBitmap.getHeight() / (float) targetedSize.second));

                resizedBitmap = Bitmap.createScaledBitmap(
                        imageBitmap,
                        (int) (imageBitmap.getWidth() / scaleFactor),
                        (int) (imageBitmap.getHeight() / scaleFactor),
                        true);
            }
// [W: 480,H: 480],[getByteCount: 921600],[getRowBytes: 1920]
            logBtpTest(resizedBitmap);
            previewImgV.setImageBitmap(resizedBitmap);

            if (imageProcessor != null) {
                graphicOverlay.setImageSourceInfo(resizedBitmap.getWidth(), resizedBitmap.getHeight(), /* isFlipped= */ false);
                imageProcessor.processBitmap(resizedBitmap, graphicOverlay);
            } else {
                Log.e(TAG, "Null imageProcessor, please check adb logs for imageProcessor creation error");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving saved image ", e);
            imageUri = null;
        }
    }


    @Override
    protected void createImageProcessor() {
        if (imageProcessor != null) imageProcessor.stop();

        try {
            if (FACE_DETECTION.equals(selectedMode)) {
                Log.i(TAG, "Using Face Detector Processor");
                imageProcessor = new FaceDetectorProcessor(this).addStillImageListener(onStillImageListener);
                // fall through
            } else {
                Log.w(TAG, "Unknown selectedMode: " + selectedMode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Can not create image processor: " + selectedMode, e);
            Toast.makeText(getApplicationContext(), "Can not create image processor: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private final FaceDetectorProcessor.Success.OnStillImageListener onStillImageListener = (originalBitmap, faces) -> {
        Face face = faces.get(0);
        Bitmap croppedFaceBitmap = cropFaceFromBitmap(originalBitmap, face.getBoundingBox());
        Log.d(TAG, "onSuccess: عرض الوجه المقصوص في حوار");
        showImgDialog(croppedFaceBitmap);
    };

    // TODO: 25/08/2024 added this fun
    private Bitmap cropFaceFromBitmap(Bitmap source, Rect boundingBox) {
        if (source == null) return null;
        Log.d(TAG, "cropFaceFromBitmap:  تأكد من أن حدود القص داخل نطاق الصورة الأصلية لتجنب حدوث خطأ");
        int left = Math.max(0, boundingBox.left);
        int top = Math.max(0, boundingBox.top);
        int width = Math.min(source.getWidth() - left, boundingBox.width());
        int height = Math.min(source.getHeight() - top, boundingBox.height());

        return Bitmap.createBitmap(source, left, top, width, height);
    }

    private void showImgDialog(Bitmap bitmap) {
        View customLayout = getLayoutInflater().inflate(R.layout.lyt_dialog_embed, null);

        ImageView imageView = customLayout.findViewById(R.id.croppedImg);
        imageView.setImageBitmap(bitmap);
        EditText nameEditText = customLayout.findViewById(R.id.name_edt);

        new MaterialAlertDialogBuilder(this)
                .setTitle(String.format("ui:%s", getTitle() == null ? "Cropped Detected Face" : getTitle()))
                //.setMessage("Face detected in the image.")
                .setPositiveButton("OK", (dialog, which) -> {
                    String name = nameEditText.getText().toString();
                    if (name.length() <= 1) {
                        nameEditText.setError("Enter name personal.!!!");
                    } else {
                        shoProgress();
                        nameEditText.setError(null);
                        //float[] embed = viewModel.generateFaceEmbedding(bitmap);

                        //save info register
                        //viewModel.saveRegisterData(name, embed);
                        if (progressDialog.isShowing()) progressDialog.dismiss();
                    }

                }).setView(customLayout).show();

    }

    ProgressDialog progressDialog;

    private void shoProgress() {
        progressDialog = ProgressDialog.show(this,
                getString(R.string.title_progress_register),
                getString(R.string.message_progresss));
    }




}
