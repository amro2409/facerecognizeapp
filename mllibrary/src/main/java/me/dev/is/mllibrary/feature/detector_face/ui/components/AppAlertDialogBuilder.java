package me.dev.is.mllibrary.feature.detector_face.ui.components;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class AppAlertDialogBuilder {
    private String title;
    private String message;

    public void showImgDialog(Bitmap bitmap, Context context,DialogInterface.OnClickListener listener) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            // We're already on the main UI thread, so we can directly show the dialog
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(bitmap);

            new MaterialAlertDialogBuilder(context)
                    .setTitle(String.format("ui:%s", getTitle() == null ? "Cropped Detected Face" : getTitle()))
                    //.setMessage("Face detected in the image.")
                    .setPositiveButton("OK", listener)
                    .setView(imageView).show();
        } else {
            // We're on a background thread, so we need to switch to the main UI thread
            new Handler(Looper.getMainLooper()).post(() -> {
                ImageView imageView = new ImageView(context);
                imageView.setImageBitmap(bitmap);

                new MaterialAlertDialogBuilder(context)
                        .setTitle(String.format("bg:%s", getTitle() == null ? "Cropped Detected Face" : getTitle()))
                        //.setMessage("Face detected in the image.")
                        .setPositiveButton("OK", listener)
                        .setView(imageView).show();
            });

        }

        /*Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            // تعامل مع الاستثناء هنا
            Log.e("MyApp", "Uncaught exception in Handler", ex);
        });
   */
    }



    public AppAlertDialogBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public AppAlertDialogBuilder setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

}
