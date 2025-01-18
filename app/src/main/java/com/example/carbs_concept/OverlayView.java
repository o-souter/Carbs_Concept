package com.example.carbs_concept;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import org.opencv.core.Point;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class OverlayView extends View {
    private List<List<Point>> markerCornersList;
    private float rotationAngle = 0f;
    private float scaleX = 1f;
    private float scaleY = 1f;
    private float imageWidth;
    private float imageHeight;
    private float overlayWidth;
    private float overlayHeight;
    private RectF imageRect;
    private List<List<PointF>> convertedMarkerCornersList; // Converted points for overlay
    private Bitmap imageBitmap;


    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //Set height and width

        convertedMarkerCornersList = new ArrayList<>();
        Log.d("OverlayView", "OverlayView width: " + getWidth() + " height: " + getHeight());
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.imageBitmap = bitmap;
        invalidate();
    }

    public void setMarkerCorners(List<List<Point>> markerCorners) {
        this.markerCornersList = markerCorners;

//        for (List<Point> corners : markerCornersList){
//            for
//        }
        invalidate(); //Redraw view
    }
    public void setImageScale(int imageWidth, int imageHeight, int previewWidth, int previewHeight) {
        float aspectRatioImage = (float) imageWidth / imageHeight;
        float aspectRatioPreview = (float) previewWidth / previewHeight;

        if (aspectRatioImage > aspectRatioPreview) {
            scaleX = (float) previewWidth / imageWidth;
            scaleY = scaleX; // Maintain aspect ratio
        } else {
            scaleY = (float) previewHeight / imageHeight;
            scaleX = scaleY; // Maintain aspect ratio
        }

        invalidate(); // Redraw the view with the updated scaling
    }
    public void setTransformation(float rotationAngle, float scaleX, float scaleY) {
        this.rotationAngle = rotationAngle;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        invalidate(); //Redraw view
    }

    public void setImageScale(float width, float height) {
        this.imageHeight = height;
        this.imageWidth = width;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.overlayWidth = w;
        this.overlayHeight = h;
        Log.d("OverlayView", "Overlay size: " + overlayWidth + "x" + overlayHeight);
    }

    private PointF convertImageCoordinates(float previewX, float previewY) {
        //Calculate scaling
        float scaleX =overlayWidth/imageWidth;
        float scaleY = overlayHeight/imageHeight;

        float overlayX = previewX * scaleX;
        float overlayY = previewY * scaleY;
//        Log.d("OverlayView", "Converted: (" + previewX + ", " + previewY + ") -> (" + overlayX + ", " + overlayY + ")");
//        Log.d("OverlayView", String.format("Input: (%.2f, %.2f), Scale: (%.2f, %.2f), Output: (%.2f, %.2f)",
//                previewX, previewY, scaleX, scaleY, overlayX, overlayY));
        return new PointF(overlayX, overlayY);
    }

    public void updateMarkerCorners(List<List<Point>> markerCornersList) {
        convertedMarkerCornersList.clear();

        for (List<Point> marker : markerCornersList) {
            List<PointF> convertedMarker = new ArrayList<>();
            for (Point point : marker) {
                PointF convertedPoint = convertImageCoordinates((float) point.x, (float) point.y);
                convertedMarker.add(convertedPoint);
            }
            convertedMarkerCornersList.add(convertedMarker);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("OverlayView", "OverlayView width: " + getWidth() + " height " + getHeight());
        super.onDraw(canvas);
//        canvas.rotate(90);
        if (imageBitmap != null) {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleX, scaleY, getWidth()/2f, getHeight()/2f);

            float viewWidth = getWidth(); // Get the OverlayView width
            float viewHeight = getHeight(); // Get the OverlayView height

            // No need to consider image size or aspect ratio, just stretch to fill the view
            imageRect = new RectF(0, 0, viewWidth, viewHeight); // Set image rect to match the OverlayView's dimensions
            // Calculate the offsets to center the image
            float scale = Math.min(scaleX, scaleY);
            float offsetX = (viewWidth - imageWidth * scale) / 2;
            float offsetY = (viewHeight - imageHeight * scale) / 2;

            canvas.translate(offsetX, offsetY); // Center the image
            canvas.scale(scale, scale);        // Scale the image

            // Draw the image
            canvas.drawBitmap(imageBitmap, 0, 0, null);
            Log.d("Bitmap", "Width: " + imageBitmap.getWidth() + " height: " + imageBitmap.getHeight());
        }


//        Set up drawing of bounding boxes
        Paint paint = new Paint();
//        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);


        for (List<PointF> marker : convertedMarkerCornersList) {
            Paint debugPaint = new Paint();
            debugPaint.setColor(Color.RED);
            debugPaint.setStyle(Paint.Style.FILL);
            debugPaint.setStrokeWidth(10f);
            for (PointF point : marker) {
                canvas.drawCircle(point.x, point.y, 10, debugPaint);
//                Log.d("OverlayView", "Debug point: (" + point.x + ", " + point.y + ")");
            }
            if (marker.size() >= 4) {
                for (int i = 0; i < marker.size(); i++) {
                    PointF start = marker.get(i);
                    PointF end = marker.get((i + 1) % marker.size());
                    canvas.drawLine(start.x, start.y, end.x, end.y, paint);
                }
            }

            PointF center = getCenterPoint(marker);
            canvas.drawText("Marker", center.x, center.y, paint);
        }
    }

    private PointF rotate90(PointF p) {
        return new PointF(-p.y, p.x);
    }

    private PointF getCenterPoint(List<PointF> corners) {
        double x = 0, y = 0;
        for (PointF corner : corners) {
            x += corner.x;
            y += corner.y;
        }
        return new PointF((float) (x / corners.size()), (float) (y / corners.size()));
    }

    private void drawBorder(Canvas canvas, Paint paint) {
//        paint.setColor(Color.GREEN);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }
}
