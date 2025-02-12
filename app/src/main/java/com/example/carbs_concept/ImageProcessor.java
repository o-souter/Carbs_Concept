package com.example.carbs_concept;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageProcessor {
    private static final int INPUT_SIZE = 416; // Size needed for model

    public static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        resizedBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int pixelValue : intValues) {
            byteBuffer.putFloat(((pixelValue >> 16) & 0xFF) / 255.0f); // Normalize Red
            byteBuffer.putFloat(((pixelValue >> 8) & 0xFF) / 255.0f);  // Normalize Green
            byteBuffer.putFloat((pixelValue & 0xFF) / 255.0f);         // Normalize Blue
        }
        return byteBuffer;
    }
}
