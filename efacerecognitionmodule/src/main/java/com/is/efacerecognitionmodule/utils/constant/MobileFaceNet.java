package com.is.efacerecognitionmodule.utils.constant;

public interface MobileFaceNet {
    int TF_OD_API_INPUT_SIZE = 112;
    boolean TF_OD_API_IS_QUANTIZED = false;
    String TF_OD_API_MODEL_FILE = "mobile_face_net.tflite";

    interface ConfidenceLevel{
        float LEVEL1F =1.0f;
        float LEVEL08F =0.89f;
        float LEVEL05F=0.5f;
    }
}
