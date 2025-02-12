package com.example.carbs_concept;

import android.graphics.RectF;

public class YoloDetectionResult {
    private RectF box;  // Bounding box
    private int classId; // Detected class index
    private float confidence; // Confidence score

    public YoloDetectionResult(RectF box, int classId, float confidence) {
        this.box = box;
        this.classId = classId;
        this.confidence = confidence;
    }

    public RectF getBox() {
        return box;
    }

    public int getClassId() {
        return classId;
    }

    public float getConfidence() {
        return confidence;
    }
}