package com.example.carbs_concept;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FoodDetectionModel {
    private Interpreter tflite;

    //Load model from assets
    public void loadModel(Context context) {
        try {
            AssetManager assetManager = context.getAssets();
            Interpreter.Options options = new Interpreter.Options();
            tflite = new Interpreter(loadModelFile(context), options);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("yolov2-food100.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
