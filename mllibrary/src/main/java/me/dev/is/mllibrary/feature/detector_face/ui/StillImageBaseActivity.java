package me.dev.is.mllibrary.feature.detector_face.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import me.dev.is.mllibrary.R;

public abstract class StillImageBaseActivity extends AppCompatActivity {

    protected static final String KEY_IMAGE_URI = "me.dev.is.mllibrary.KEY_IMAGE_URI";
    protected static final String KEY_SELECTED_SIZE = "me.dev.is.mllibrary.KEY_SELECTED_SIZE";

    protected static final String SIZE_SCREEN = "w:screen"; // Match screen width
    protected static final String SIZE_1024_768 = "w:1024"; // ~1024*768 in a normal ratio
    protected static final String SIZE_640_480 = "w:640"; // ~640*480 in a normal ratio
    protected static final String SIZE_ORIGINAL = "w:original"; // Original image size

    protected static final String OBJECT_DETECTION = "Object Detection";
    protected static final String FACE_DETECTION = "Face Detection";
    protected static final String FACE_MESH_DETECTION = "Face Mesh Detection (Beta)";

    protected static final int REQUEST_IMAGE_CAPTURE = 1001;
    protected static final int REQUEST_CHOOSE_IMAGE = 1002;
    private static final String TAG = StillImageBaseActivity.class.getSimpleName();

    protected ImageView previewImgV;
    protected Uri imageUri;

    protected String selectedMode = OBJECT_DETECTION;
    protected String selectedSize = SIZE_SCREEN;

    protected int imageMaxWidth;
    protected int imageMaxHeight;
    protected boolean isLandScape;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            tryReloadAndDetectInImage("onActivityResult:camera");
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            // In this case, imageUri is returned by the chooser, save it.
            imageUri = data.getData();
            tryReloadAndDetectInImage("onActivityResult:gallery");
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void populateFeatureSelector() {
        Log.d(TAG, "populateFeatureSelector() called");

        Spinner featureSpinner = findViewById(R.id.feature_selector);
        final List<String> options = Arrays.asList(FACE_DETECTION, FACE_MESH_DETECTION);

        // Creating adapter for featureSpinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        featureSpinner.setAdapter(dataAdapter);
        featureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selectedMode = parentView.getItemAtPosition(pos).toString();
                createImageProcessor();
                tryReloadAndDetectInImage("populateFeatureSelector:onItemSelected:"+pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    protected final void populateSizeSelector() {
        Log.d(TAG, "populateSizeSelector() called");

        final Spinner sizeSpinner = findViewById(R.id.size_selector);
        final List<String> options = Arrays.asList(SIZE_SCREEN, SIZE_1024_768, SIZE_640_480, SIZE_ORIGINAL);

        // Creating adapter for sizeSpinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        sizeSpinner.setAdapter(dataAdapter);
        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int pos, long id) {
                selectedSize = parentView.getItemAtPosition(pos).toString();
                tryReloadAndDetectInImage("populateSizeSelector:onItemSelected:"+pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    protected final void startCameraIntentForResult() {
        Log.d(TAG, "startCameraIntentForResult() called");
        // Clean up last time's image
        imageUri = null;
        previewImgV.setImageBitmap(null);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
            imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    protected final void startChooseImageIntentForResult() {
        Log.d(TAG, "startChooseImageIntentForResult() called");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CHOOSE_IMAGE);
    }

    protected Pair<Integer, Integer> getTargetedWidthHeight() {
        Log.d(TAG, "getTargetedWidthHeight() called");

        int targetWidth;
        int targetHeight;

        switch (selectedSize) {
            case SIZE_SCREEN:
                targetWidth = imageMaxWidth;
                targetHeight = imageMaxHeight;
                break;
            case SIZE_640_480:
                targetWidth = isLandScape ? 640 : 480;
                targetHeight = isLandScape ? 480 : 640;
                break;
            case SIZE_1024_768:
                targetWidth = isLandScape ? 1024 : 768;
                targetHeight = isLandScape ? 768 : 1024;
                break;
            default:
                throw new IllegalStateException("Unknown size");
        }
        Log.d(TAG, "getTargetedWidthHeight() returned: W:" + targetWidth+",H:"+targetHeight);
        return new Pair<>(targetWidth, targetHeight);
    }

   protected void logBtpTest(Bitmap bitmap) {
       Log.d(TAG, this.getLocalClassName()+".logBtp() called with: imageMaxWidth = [" + imageMaxWidth + "]"+"imageMaxHeight:"+imageMaxHeight);
        if (bitmap == null) return;
        Log.d(TAG, "logBtp() called with: bitmap = [" + bitmap.getClass().getName() + "]");
        Log.d(TAG, "logBtp() called with: bitmap = [W: " + bitmap.getWidth() + ",H: " + bitmap.getHeight() + "]");
        Log.d(TAG, "logBtp() called with: bitmap = [getByteCount: " + bitmap.getByteCount() + "]");
        Log.d(TAG, "logBtp() called with: bitmap = [getRowBytes: " + bitmap.getRowBytes() + "]");
    }

    protected void logFormat(@NotNull String...strings){
        Log.d(TAG, String.format("%s <------:-|-:called from: [%s]",strings[0],strings[1]));
    }
//------------------------------------------------

    protected abstract void createImageProcessor();
    protected abstract void tryReloadAndDetectInImage(String...fromCall);

}
