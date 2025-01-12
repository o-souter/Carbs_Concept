package com.example.carbs_concept;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import org.opencv.core.Point;

import java.util.List;

public class OverlayView extends View {
    private List<List<Point>> markerCornersList;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMarkerCorners(List<List<Point>> markerCorners) {
        this.markerCornersList = markerCorners;
        invalidate(); //Redraw view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Set up drawing of bounding boxes
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        if (markerCornersList != null) {
            for (List<Point> corners : markerCornersList){
//                if (corners.size() < 2) {
//                    Log.d("Overlay", "Less than 2 corners found, skipping...");
//                    continue;
//                }
                //Draw bounding box for each marker
                Log.d("Overlay", "2 or more corners found, Overlaying...");
                for (int i = 0; i < corners.size(); i++) {
                    Point start = corners.get(i);
                    Point end = corners.get((i + 1) % corners.size());  // Wrap around to the first corner

                    // Draw lines between the corners
                    canvas.drawLine((float) start.x, (float) start.y, (float) end.x, (float) end.y, paint);
                }

                //Overlay with ID
                if (!corners.isEmpty()) {
                    Point center = getCenterPoint(corners);
                    paint.setColor(Color.RED);
                    paint.setTextSize(50);
                    canvas.drawText("Marker", (float) center.x, (float) center.y, paint);
                }
            }
        }
    }
    private Point getCenterPoint(List<Point> corners) {
        double x = 0, y = 0;
        for (Point corner : corners) {
            x += corner.x;
            y += corner.y;
        }
        return new Point(x / corners.size(), y / corners.size());
    }
}
