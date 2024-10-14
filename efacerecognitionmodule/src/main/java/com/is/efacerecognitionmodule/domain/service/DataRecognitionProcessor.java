package com.is.efacerecognitionmodule.domain.service;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * 1.Loading the model: The machine learning model is first loaded from the "assets" folder using the loadModelFile function.
 * <p>
 * 2.Initializing resources: Allocating memory and preparing to store the input and output data using allocateBuffers.
 * <p>
 * 3.Processing the input data: When there is an image that needs to be recognized, it is preprocessed using preProcessBitmap to convert the image into a format suitable for the model.
 * <p>
 * 4.Storing the input data: Depending on the data type (quantized or float), each pixel is transformed and stored in a ByteBuffer using putQuantizedPixel or putFloatPixel.
 * <p>
 * 5.Running inference: After preparing the input data, the model is run using runInference to extract patterns or features (e.g. embeddings).
 * <p>
 * 6.Using the output: After the inference process, the outputs such as embeddings can be used for other tasks such as identifying or recognizing a person based on previous training data.
 */
public abstract class DataRecognitionProcessor implements SimilarityClassifier {
    private static final String TAG = DataRecognitionProcessor.class.getSimpleName();
    protected static final int EMBEDDING_SIZE = 192;//512
    protected static final int DEFAULT_INPUT_SIZE = 112;//160
    protected static final int OUTPUT_SIZE = EMBEDDING_SIZE;
    //the number of color channels
    protected static final int NUM_COLOR = 3;
    // Only return this many results.
    //protected static final int NUM_DETECTIONS = 1;
    // Float model
    protected static final float IMAGE_MEAN = 128.0f;
    protected static final float IMAGE_STD = 128.0f;

    protected boolean isModelQuantized;
    protected int inputSize;
    /**
     * store the pixel values of the input image
     */
    protected int[] intValues;
    protected float[][] embeddings;
    protected ByteBuffer imgInputData;
    protected Interpreter tfLite;

    protected MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename) throws IOException {
        try (AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
             Log.d(TAG, "loadModelFile(), modelFilename: "+modelFilename);
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    private int getInputSize() {
        return inputSize == 0 ? DEFAULT_INPUT_SIZE : inputSize;
    }

    /**
     * Allocates memory buffers for input and output data.
     */
    protected void allocateBuffers() {
        int numBytesPerChannel = isModelQuantized ? 1 : 4; // Quantized vs Float model
        imgInputData = ByteBuffer.allocateDirect(getInputSize() * getInputSize() * NUM_COLOR * numBytesPerChannel);
        imgInputData.order(ByteOrder.nativeOrder());
        intValues = new int[inputSize * inputSize];
       // embeddings = new float[1][OUTPUT_SIZE];
    }


    /**
     * preProcessing: convert image(bitmap) to the format required to the model
     * <ul>
     * <li>Extract pixel data from the input image.</li>
     * <li>Rewind(clear) the data into the ByteBuffer to prepare it for storing new data.</li>
     * <li>for loop through each pixel in the image and converting it to a suitable format (either quantized or float data)
     * and storing it in the ByteBuffer.</li>
     * </ul>
     */
    protected void preProcessBitmap(@NotNull Bitmap bitmap) {
        Trace.beginSection("preProcessBitmap()");
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgInputData.rewind();

        for (int pixelVal : intValues) {
            if (isModelQuantized) {
                putQuantizedPixel(pixelVal);
            } else {
                putFloatPixel(pixelVal);
            }
        }

        /* for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    putQuantizedPixel(pixelValue);
                } else {
                    putFloatPixel(pixelValue);
                }
            }
        }*/
        Trace.endSection();
    }

    /**
     * Puts a quantized pixel(r,g,b) into the ByteBuffer.
     */
    protected void putQuantizedPixel(int pixelValue) {
        imgInputData.put((byte) ((pixelValue >> 16) & 0xFF));
        imgInputData.put((byte) ((pixelValue >> 8) & 0xFF));
        imgInputData.put((byte) (pixelValue & 0xFF));
    }

    /**
     * Convert the pixel value to float data, normalize it, and store it in a ByteBuffer.
     * <ul>
     *   <li>Extract pixel components (red, green, blue).</li>
     *   <li> Normalize each component using mean and standard deviation values​(IMAGE_MEAN and IMAGE_STD).</li>
     *   <li>Store float values in ByteBuffer.</li>
     * </ul>
     */
    protected void putFloatPixel(int pixelValue) {
        imgInputData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgInputData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgInputData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }

    /**
     * Run an inference process using the input model to analyze the image and extract features.
     * <p>
     * Prepare the input array for the model using ByteBuffer.
     * Create a map to store the outputs from the model, such as embeddings.
     * Call the runForMultipleInputsOutputs function to run the model and get the output.
     */
    protected void runInference() {
        Object[] inputArray = {imgInputData};
        Map<Integer, Object> outputMap = new HashMap<>();
        embeddings=new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeddings);
        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);
    }

}

