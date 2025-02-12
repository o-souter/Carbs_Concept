package com.example.carbs_concept;

import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;

public class YoloInference {
    private Interpreter interpreter;

    public YoloInference(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public float[][][] runModel (Bitmap bitmap) {
        ByteBuffer input = ImageProcessor.convertBitmapToByteBuffer(bitmap);

        float[][][] output = new float[1][2535][85];
        interpreter.run(input, output);

        return output;
    }
}
