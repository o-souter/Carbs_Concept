package com.example.carbs_concept;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import org.opencv.core.Point;

import java.util.List;

public class OverlayView extends View {
    private List<List<Point>> markerCornersList;
    private float rotationAngle = 0f;
    private float scaleX = 1f;
    private float scaleY = 1f;

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMarkerCorners(List<List<Point>> markerCorners) {
        this.markerCornersList = markerCorners;

//        for (List<Point> corners : markerCornersList){
//            for
//        }
        invalidate(); //Redraw view
    }

    public void setTransformation(float rotationAngle, float scaleX, float scaleY) {
        this.rotationAngle = rotationAngle;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        invalidate(); //Redraw view
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//
//        canvas.save();
//        canvas.rotate(rotationAngle, getWidth() / 2f, getHeight() / 2f);
//        canvas.scale(scaleX, scaleY, getWidth() / 2f, getHeight() / 2f);

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
//                    StaticLayout markerInfo = new StaticLayout("Marker\n x: " + center.x + "\ny: " +center.y, paint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                    paint.setColor(Color.RED);
                    paint.setTextSize(50);
                    canvas.drawText("Marker detected", (float) center.x, (float) center.y, paint);
                    canvas.drawText("x: "+center.x, (float) center.x, (float) center.y + 50, paint);
                    canvas.drawText("y: "+center.y, (float) center.x, (float) center.y + 100, paint);
                }

                drawBorder(canvas, paint);

//                canvas.restore();
            }
        }
    }

//    private Point pointToOverlayPoint(Point point) {
//        //Swap X and Y coordinates
//        Point newPoint = new Point(getWidth() - point.y, point.x);
//
//
//        return newPoint
//    }

    private Point getCenterPoint(List<Point> corners) {
        double x = 0, y = 0;
        for (Point corner : corners) {
            x += corner.x;
            y += corner.y;
        }
        return new Point(x / corners.size(), y / corners.size());
    }

    private void drawBorder(Canvas canvas, Paint paint) {
        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
