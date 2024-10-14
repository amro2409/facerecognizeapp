package com.is.efacerecognitionmodule.domain.processor;

import android.graphics.Bitmap;

public class StrategyImage {
    public interface ImageProcessingStrategy {
        Bitmap processImage(Bitmap bitmap);
    }

    public static class GrayscaleStrategy implements ImageProcessingStrategy {
        @Override
        public Bitmap processImage(Bitmap bitmap) {
            // تحويل الصورة إلى تدرجات الرمادي
            // ...
            return bitmap;
        }
    }

    public static class EdgeDetectionStrategy implements ImageProcessingStrategy {
        @Override
        public Bitmap processImage(Bitmap bitmap) {
            // تطبيق خوارزمية اكتشاف الحواف على الصورة
            // ...
            return bitmap;
        }
    }

    public static class ImageProcessor {
        private ImageProcessingStrategy strategy;

        public ImageProcessor(ImageProcessingStrategy strategy) {
            this.strategy = strategy;
        }

        public void setStrategy(ImageProcessingStrategy strategy) {
            this.strategy = strategy;
        }

        public Bitmap process(Bitmap bitmap) {
            return strategy.processImage(bitmap);
        }
    }

    void testClient(){
        ImageProcessor processor = new ImageProcessor(new GrayscaleStrategy());
        Bitmap grayBitmap = processor.process(null);
// تغيير الاستراتيجية في وقت التشغيل
        processor.setStrategy(new EdgeDetectionStrategy());
        Bitmap edgeBitmap = processor.process(null);
    }


}

 class FaceDetectedConfig {
    private final int sensorOrientation;
    private final int previewWidth;
    private final int previewHeight;
    private final int TF_OD_API_INPUT_SIZE;

    private FaceDetectedConfig(Builder builder) {
        this.sensorOrientation = builder.sensorOrientation;
        this.previewWidth = builder.previewWidth;
        this.previewHeight = builder.previewHeight;
        this.TF_OD_API_INPUT_SIZE = builder.TF_OD_API_INPUT_SIZE;
    }

    public static class Builder {
        private int sensorOrientation;
        private int previewWidth;
        private int previewHeight;
        private int TF_OD_API_INPUT_SIZE;

        public Builder setSensorOrientation(int sensorOrientation) {
            this.sensorOrientation = sensorOrientation;
            return this;
        }

        public Builder setPreviewWidth(int previewWidth) {
            this.previewWidth = previewWidth;
            return this;
        }

        public Builder setPreviewHeight(int previewHeight) {
            this.previewHeight = previewHeight;
            return this;
        }

        public Builder setTF_OD_API_INPUT_SIZE(int TF_OD_API_INPUT_SIZE) {
            this.TF_OD_API_INPUT_SIZE = TF_OD_API_INPUT_SIZE;
            return this;
        }

        public FaceDetectedConfig build() {
            return new FaceDetectedConfig(this);
        }
    }

    void t(){
        FaceDetectedConfig config = new FaceDetectedConfig.Builder()
                .setSensorOrientation(90)
                .setPreviewWidth(640)
                .setPreviewHeight(480)
                .setTF_OD_API_INPUT_SIZE(112)
                .build();

    }
}

