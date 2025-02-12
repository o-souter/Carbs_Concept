package com.example.carbs_concept;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class YoloPostProcessor {
    private static final float CONFIDENCE_THRESHOLD = 0.5f;

    public static List<YoloDetectionResult> processOutput(float[][][] output) {
        List<YoloDetectionResult> results = new ArrayList<>();

        for (int i = 0; i < output[0].length; i++) {
            float confidence = output[0][i][4]; // Confidence score

            if (confidence > CONFIDENCE_THRESHOLD) {
                float x = output[0][i][0];
                float y = output[0][i][1];
                float width = output[0][i][2];
                float height = output[0][i][3];

                int classId = getClassId(output[0][i]);
                float classConfidence = output[0][i][5 + classId];

                RectF box = new RectF(x - width / 2, y - height / 2, x + width / 2, y + height / 2);
                results.add(new YoloDetectionResult(box, classId, classConfidence));
            }
        }
        return results;
    }
    private static int getClassId(float[] outputRow) {
        int classId = -1;
        float maxProb = 0;

        for (int j = 5; j < outputRow.length; j++) {
            if (outputRow[j] > maxProb) {
                maxProb = outputRow[j];
                classId = j - 5;
            }
        }
        return classId;
    }
}
