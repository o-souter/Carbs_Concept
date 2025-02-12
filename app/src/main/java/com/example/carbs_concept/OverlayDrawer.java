package com.example.carbs_concept;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

public class OverlayDrawer {
    public static Bitmap drawDetections(Bitmap bitmap, List<YoloDetectionResult> results) {
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        for (YoloDetectionResult result : results) {
            canvas.drawRect(result.getBox(), paint);
        }

        return mutableBitmap;
    }
}
