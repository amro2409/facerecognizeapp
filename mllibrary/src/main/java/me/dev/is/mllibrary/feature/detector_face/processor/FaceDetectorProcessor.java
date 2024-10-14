package me.dev.is.mllibrary.feature.detector_face.processor;




import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import me.dev.is.mllibrary.feature.detector_face.graphics.FaceGraphic;
import me.dev.is.mllibrary.feature.detector_face.graphics.GraphicOverlay;
import me.dev.is.mllibrary.feature.detector_face.ui.components.AppAlertDialogBuilder;
import me.dev.is.mllibrary.feature.detector_face.utils.CXProvider;
import me.dev.is.mllibrary.feature.detector_face.utils.PreferenceUtils;

/**
 * Face Detector Demo.
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<Face>> {

    private static final String TAG = "FaceDetectorProcessor";

    private final FaceDetector detector;

    CXProvider cxProvider;

    private Success.OnStillImageListener mOnStillImageListener;

    public FaceDetectorProcessor(Context context) {
        super(context);
        FaceDetectorOptions faceDetectorOptions = PreferenceUtils.getFaceDetectorOptions(context);
        Log.v(MANUAL_TESTING_LOG, "Face detector options: " + faceDetectorOptions);
        detector = FaceDetection.getClient(faceDetectorOptions);
        //-- default test
        cxProvider = new CXProvider();
        cxProvider.setContext(context);
    }


    public void showImgDialog(Bitmap bitmap) {
        new AppAlertDialogBuilder().setTitle("Cropped Detected Face")
                .showImgDialog(bitmap, cxProvider.getContext(), (dialog, which) -> {

                });

    }


    @Override
    public void stop() {
        super.stop();
        detector.close();
        isDetectedFace = false;
        isShowDialog = false;
    }

    @Override
    protected Task<List<Face>> detectInImage(InputImage image) {
        return detector.process(image);
    }

    // TODO: 25/08/2024 added these variables
    private volatile boolean isDetectedFace = false, isShowDialog = false;
    private Bitmap originalBitmap;

    @Override
    protected Task<List<Face>> detectInImage(@NotNull InputImage image, Bitmap bitmap) {
        originalBitmap = (bitmap == null ? image.getBitmapInternal() : bitmap);
        if (isDetectedFace && !isShowDialog) {
            showImgDialog(originalBitmap);
            //logBtpTest(originalBitmap);
            isShowDialog = true;
        }
        return detector.process(image);
    }

    @Override
    protected void onSuccess(@NonNull List<Face> faces, @NonNull GraphicOverlay graphicOverlay) {
        Log.d(TAG, "start-->onSuccess() returned faces.size: " + faces.size() + ",isEmptyF: " + faces.isEmpty());

        if (!faces.isEmpty() && !isDetectedFace) {
            isDetectedFace = true;
            // TODO: 25/08/2024 updated this fun by add these comm lines
            if (mOnStillImageListener !=null){
                mOnStillImageListener.result(originalBitmap,faces);
            }
        }

        for (Face face : faces) {
            graphicOverlay.add(new FaceGraphic(graphicOverlay, face));//FaceGraphic
            logExtrasForTesting(face);
        }
        Log.d(TAG, "end->onSuccess() called with: faces = [" + faces + "], graphicOverlay = [" + graphicOverlay + "]");
    }


   public interface Success{

        interface OnStillImageListener {
            void result(Bitmap source,@NonNull List<Face> faces);
        }

       /* interface LiveImageListener{
            void result(@NonNull List<Face> faces);
        }*/
    }

    public FaceDetectorProcessor addStillImageListener(Success.OnStillImageListener mOnStillImageListener) {
        this.mOnStillImageListener = mOnStillImageListener;
        return this;
    }

    private static void logExtrasForTesting(Face face) {
        if (face != null) {
            // All landmarks
            int[] landMarkTypes =
                    new int[]{
                            FaceLandmark.MOUTH_BOTTOM,
                            FaceLandmark.MOUTH_RIGHT,
                            FaceLandmark.MOUTH_LEFT,
                            FaceLandmark.RIGHT_EYE,
                            FaceLandmark.LEFT_EYE,
                            FaceLandmark.RIGHT_EAR,
                            FaceLandmark.LEFT_EAR,
                            FaceLandmark.RIGHT_CHEEK,
                            FaceLandmark.LEFT_CHEEK,
                            FaceLandmark.NOSE_BASE
                    };
            String[] landMarkTypesStrings =
                    new String[]{
                            "MOUTH_BOTTOM",
                            "MOUTH_RIGHT",
                            "MOUTH_LEFT",
                            "RIGHT_EYE",
                            "LEFT_EYE",
                            "RIGHT_EAR",
                            "LEFT_EAR",
                            "RIGHT_CHEEK",
                            "LEFT_CHEEK",
                            "NOSE_BASE"
                    };
            if (!face.getAllLandmarks().isEmpty())
                for (int i = 0; i < landMarkTypes.length; i++) {
                    FaceLandmark landmark = face.getLandmark(landMarkTypes[i]);
                    if (landmark == null) {
                        Log.v(MANUAL_TESTING_LOG, "No landmark of type: " + landMarkTypesStrings[i] + " has been detected");
                    } else {
                        PointF landmarkPosition = landmark.getPosition();
                        String landmarkPositionStr = String.format(Locale.US, "x: %f , y: %f", landmarkPosition.x, landmarkPosition.y);
                        Log.v(MANUAL_TESTING_LOG, "FDP: Position for face landmark: " + landMarkTypesStrings[i] + " is :" + landmarkPositionStr);
                    }
                }
            logTest(face);
        }
    }

    void logBtpTest(Bitmap bitmap) {
        if (bitmap == null) return;
        Log.d(TAG, "logBtp() called with: bitmap = [" + bitmap + "]");
        Log.d(TAG, "logBtp() called with: bitmap = [W: " + bitmap.getWidth() + ",H: " + bitmap.getHeight() + "]");
        Log.d(TAG, "logBtp() called with: bitmap = [getByteCount: " + bitmap.getByteCount() + "]");
        Log.d(TAG, "logBtp() called with: bitmap = [getRowBytes: " + bitmap.getRowBytes() + "]");
    }

    static void logTest(@NotNull Face face) {
        final String line = "--------------->";
        Log.v(MANUAL_TESTING_LOG, line + "face bounding box: " + face.getBoundingBox().flattenToString());
        Log.v(MANUAL_TESTING_LOG, line + "face Euler Angle X: " + face.getHeadEulerAngleX());
        Log.v(MANUAL_TESTING_LOG, line + "face Euler Angle Y: " + face.getHeadEulerAngleY());
        Log.v(MANUAL_TESTING_LOG, line + "face Euler Angle Z: " + face.getHeadEulerAngleZ());

        //---------------------
        Log.v(MANUAL_TESTING_LOG, line + "face left eye open probability: " + face.getLeftEyeOpenProbability());
        Log.v(MANUAL_TESTING_LOG, line + "face right eye open probability: " + face.getRightEyeOpenProbability());
        Log.v(MANUAL_TESTING_LOG, line + "face smiling probability: " + face.getSmilingProbability());
        Log.v(MANUAL_TESTING_LOG, line + "face tracking id: " + face.getTrackingId());
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
