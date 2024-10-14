package com.is.efacerecognitionmodule.ui.recognition;

import android.os.Bundle;

import com.is.efacerecognitionmodule.R;
import com.is.efacerecognitionmodule.data.model.ResultSuccess;
import com.is.efacerecognitionmodule.databinding.ActivitySuccessRecognitionBinding;
import com.is.efacerecognitionmodule.ui.ToolbarActivity;

import java.util.Locale;


public class RecognitionSuccessActivity extends ToolbarActivity {
    public static final String EXTRA_RESULT_SUCCESS = "com.is.efacerecognitionmodule.ui.recognition.RESULT_SUCCESS";
    ActivitySuccessRecognitionBinding binding;
    ResultSuccess resultSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessRecognitionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra(EXTRA_RESULT_SUCCESS)) {
            resultSuccess = (ResultSuccess) getIntent().getSerializableExtra(EXTRA_RESULT_SUCCESS);

            binding.tvName.setText(resultSuccess.getName());
            binding.tvDistance.setText(String.format(Locale.ENGLISH,"%.2f", resultSuccess.getDistance()));
            binding.tvTime.setText(String.format("%s", resultSuccess.getTime()));
            binding.tvStatus.setText(String.format("%s", resultSuccess.getStatus()));
        }

        binding.btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        toolbar.setTitle(R.string.head_success_recognition_result);
    }
}

