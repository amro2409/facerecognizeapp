package com.is.efacerecognitionmodule.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.is.efacerecognitionmodule.R;

import java.util.Locale;

public class AppAlertDialog {

    public static void showRegisterCustomDialog(Context context, @NonNull Callback callback) {
        CustomDialog customDialog = new CustomDialog(context);
        customDialog.setCustomView(R.layout.tfe_image_edit_dialog)
                .setOnClickOkBtnListener(new CustomDialog.OnClickOkBtnListener() {
                    ImageView ivFace;
                    TextView tvFDesc;
                    TextView tvTitle;
                    EditText etName;

                    @Override
                    public void initViews(View parentView) {
                        ivFace = parentView.findViewById(R.id.image_face);
                        tvFDesc = parentView.findViewById(R.id.image_desc);
                        tvTitle = parentView.findViewById(R.id.dialog_title);
                        etName = parentView.findViewById(R.id.input_name);
                        // Set dialog title and image
                        tvTitle.setText(R.string.title_dailog_add_face);
                        Bitmap cropBitmap = callback.getCropBitmap();
                        ivFace.setImageBitmap(cropBitmap);
                        tvFDesc.setText(String.format(Locale.ENGLISH, "W:%d, H:%d", cropBitmap.getWidth(), cropBitmap.getHeight()));
                        etName.setHint("Enter name.");
                    }

                    @Override
                    public void onClickOk(CustomDialog.OnCompletedOkBtnListener listener) {

                        String name = etName.getText().toString();
                        if (name.isEmpty()) {
                            etName.setError("Please enter a name.");
                            return;
                        }
                        etName.setError(null);

                        callback.onclickOk(name, null);
                        listener.onCompletedOk();
                    }
                }).setCancelable1(false).show();//.setIsCancelOnKey(true)
    }

    public static void showRegisterDialog(Context context, @NonNull Callback callback) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);

        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogLayout = inflater.inflate(R.layout.tfe_image_edit_dialog, null);

        // Initialize views
        ImageView ivFace = dialogLayout.findViewById(R.id.image_face);
        TextView tvFDesc = dialogLayout.findViewById(R.id.image_desc);
        TextView tvTitle = dialogLayout.findViewById(R.id.dialog_title);
        EditText etName = dialogLayout.findViewById(R.id.input_name);

        // Set dialog title and image
        tvTitle.setText(R.string.title_dailog_add_face);
        Bitmap cropBitmap = callback.getCropBitmap();
        ivFace.setImageBitmap(cropBitmap);
        tvFDesc.setText(String.format(Locale.ENGLISH, "W:%d, H:%d", cropBitmap.getWidth(), cropBitmap.getHeight()));
        etName.setHint("Enter name.");

        // Set the custom view for the dialog
        dialogBuilder.setView(dialogLayout).setCancelable(false);
        // Create the dialog
        // AlertDialog alertDialog = dialogBuilder.setCancelable(false).create();

        // Handle the save button click
        dialogBuilder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                etName.setError("Please enter a name.");
                return;
            }
            etName.setError(null); // Clear the error
            callback.onclickOk(name, dialog);
            // Add knownFaces logic if needed
            dialog.dismiss();
        });

        // Handle the cancel button click
        dialogBuilder.setNeutralButton("الغاء", (dialog, which) -> dialog.dismiss());
        // Show the dialog
        dialogBuilder.show();

    }

    public interface Callback {
        void onclickOk(String name, DialogInterface alertDialog);

        Bitmap getCropBitmap();
    }
}