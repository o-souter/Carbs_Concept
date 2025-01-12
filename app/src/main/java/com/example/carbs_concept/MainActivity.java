package com.example.carbs_concept;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.view.PreviewView;

//OpenCV
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.ArucoDetector;
import org.opencv.objdetect.Dictionary;
import org.opencv.objdetect.Objdetect;
import org.opencv.objdetect.DetectorParameters;


import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageCapture imageCapture;
    private PreviewView previewView;
    private Button captureButton;
    private ArucoDetector arucoDetector;
    private TextView detectionFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //Confirm OpenCV initialised properly
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed!");
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully.");
        }

        //Request permissions
        getPermissions();

        //Setup widgets
        previewView = findViewById(R.id.previewView);
        captureButton = findViewById(R.id.captureButton);

        initializeArucoDetector();
        initializeCamera();

        detectionFeedback = findViewById((R.id.detectionFeedback));
        //Set up the GUI


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void getPermissions() {
        //Camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    private void initializeArucoDetector() {
        Dictionary dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);

        DetectorParameters params = new DetectorParameters();

        arucoDetector = new ArucoDetector(dictionary, params);
    }

    private void initializeCamera() {
        try {
            ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
            CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

            Preview preview = new Preview.Builder().build();
            preview.setSurfaceProvider(previewView.getSurfaceProvider());

            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyseFrame);

            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        } catch (Exception e) {
            Log.e("CameraX", "Camera initialization failed: ", e);
        }

    }

//        ProcessCameraProvider.getInstance(this).addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
//                //Select the back camera of phone
//                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
//
////                imageCapture = new ImageCapture.Builder().build();
//
//                Preview preview = new Preview.Builder().build();
//                preview.setSurfaceProvider(previewView.getSurfaceProvider());
//
////                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
//                //When button clicked, capture image
////                captureButton.setOnClickListener(v -> captureImage());
//
//
//            }
//            catch (Exception e) {
//                Log.e("CameraX", "Camera initialization failed: ", e);
//            }
//        }, ContextCompat.getMainExecutor(this));



    private void analyseFrame(ImageProxy imageProxy) {
        try {
            Mat frame = imageProxyToMat(imageProxy);

            List<Mat> markerCorners = new ArrayList<>();
            Mat markerIds = new Mat();

            Mat greyImage = new Mat();
            //Convert to greyscale
            Imgproc.cvtColor(frame, greyImage, Imgproc.COLOR_BGR2GRAY);

            //Increase contrast
            Mat contrastGreyImage = new Mat();
            Core.normalize(greyImage, contrastGreyImage, 0, 255, Core.NORM_MINMAX);

            //Apply gaussian blur
            Mat gaussianBlurImage = new Mat();
            Imgproc.GaussianBlur(contrastGreyImage, gaussianBlurImage, new Size(5, 5), 0);

            //Apply binary threshold to better find markers
            Mat thresholdedFrame = new Mat();
            Imgproc.adaptiveThreshold(contrastGreyImage, thresholdedFrame, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 5);

            Mat imageToDetect = gaussianBlurImage;

            Imgcodecs.imwrite(getFilesDir() + "/imageToDetect.jpg", imageToDetect);
//            Log.d("Aruco", "Thresholded frame saved at: " + getFilesDir() + "/imageToDetect.jpg");
            //Detect markers in the current frame
            arucoDetector.detectMarkers(imageToDetect, markerCorners, markerIds);

            if (!markerIds.empty()) {
                List<List<Point>> cornersList = new ArrayList<>();
                for (int i = 0; i < markerCorners.size(); i++) {
                    // Get the marker corners

                    Mat cornerMat = markerCorners.get(i);

                    MatOfPoint2f cornerMatOfPoint2f = new MatOfPoint2f();

                    cornerMat.convertTo(cornerMatOfPoint2f, CvType.CV_32F);

                    List<Point> points = new ArrayList<>();
                    for (int j = 0; j < cornerMatOfPoint2f.rows(); j++) {
                        double[] corner = cornerMatOfPoint2f.get(j, 0);
                        points.add(new Point(corner[0], corner[1]));
                    }
                    cornersList.add(points);
                }
                //Map coordinates to screen space coordinates
                Size imageSize = imageToDetect.size();
                int previewWidth = previewView.getWidth();
                int previewHeight = previewView.getHeight();

                for (List<Point> corners : cornersList) {
                    for (int i = 0; i < corners.size(); i++) {
                        corners.set(i, mapToScreenSpace(corners.get(i), imageSize, previewWidth, previewHeight));
                    }
                }

                runOnUiThread(() -> {
                    detectionFeedback.setText(markerIds.rows() + " Fiducial marker(s) detected. Take picture when ready!");
                    detectionFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    captureButton.setEnabled(true);

                    OverlayView overlayView = findViewById(R.id.overlayView);
                    overlayView.setVisibility(View.VISIBLE);
                    overlayView.setLayoutParams(previewView.getLayoutParams());
                    overlayView.setMarkerCorners(cornersList);
                });
            }
            else {
                runOnUiThread(() -> {
                    detectionFeedback.setText("No fiducial marker detected.");
                    detectionFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    captureButton.setEnabled(false);
                    OverlayView overlayView = findViewById(R.id.overlayView);
                    overlayView.setVisibility(View.INVISIBLE);
                });
            }

            Bitmap bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frame, bitmap);
        }
        catch (Exception e) {
            Log.e("Aruco", "Error analysing frame: ", e);
        }
        finally {
            imageProxy.close();
        }
    }
    private Point mapToScreenSpace(Point imagePoint, Size imageSize, int previewWidth, int previewHeight) {
        double scaleX = (double) previewWidth / imageSize.width;
        double scaleY = (double) previewHeight / imageSize.height;

        // Ensure consistent scaling (maintain aspect ratio)
        double scale = Math.min(scaleX, scaleY);

        // Center content within `previewView`
        double offsetX = (previewWidth - imageSize.width * scaleX) / 2.0;
        double offsetY = (previewHeight - imageSize.height * scaleY) / 2.0;

        // Swap x and y, and invert the swapped x-axis
        return new Point(
                (imageSize.height - imagePoint.y) * scale + offsetX,  // Swapped y -> inverted x
                imagePoint.x * scale + offsetY                       // Swapped x -> y
        );

//
    }

    private Mat imageProxyToMat(ImageProxy imageProxy) {
        //Convert imageproxy to openCV mat
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();

        ByteBuffer buffer = planes[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        Mat yuv = new Mat(imageProxy.getHeight() + imageProxy.getHeight() / 2, imageProxy.getWidth(), CvType.CV_8UC1);
        yuv.put(0,0, bytes);

        Mat rgb = new Mat();
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21);
        return rgb;
    }

    private void captureImage() {
        File file = new File(getFilesDir(), "captured_image.jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("CameraX", "Saved image: " + file.getAbsolutePath());
                        processImage(file.getAbsolutePath());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Image capture failed: " + exception.getMessage());
                    }
                }
        );
    }

    private void processImage(String imagePath) {
        //Load image with OpenCV
        Mat image = Imgcodecs.imread(imagePath);

        Mat preprocessedImage = new Mat();
        //Convert to greyscale
        Imgproc.cvtColor(image, preprocessedImage, Imgproc.COLOR_BGR2GRAY);

        //Set up ARUco detector for precise aruco marker to detect
        Dictionary dictionary = Objdetect.getPredefinedDictionary(Objdetect.DICT_4X4_50);


        //Parameters
        DetectorParameters params = new DetectorParameters();
        params.set_markerBorderBits(1);
        ArucoDetector arucoDetector = new ArucoDetector(dictionary, params);

        // Create Mat objects for marker corners and IDs
        List<Mat> markerCorners = new ArrayList<>();
        Mat markerIds = new Mat();

        //Detect markers
        arucoDetector.detectMarkers(preprocessedImage, markerCorners, markerIds);

        if (!markerIds.empty()) {
            Log.d("ARUco", "Markers detected: " + markerIds.dump());
            calculateScale(markerCorners);
        }
        else {
            Log.d("ARUco", "No Markers detected.");
        }
    }

    private void calculateScale(List<Mat> markerCorners) {
        //Calculate the scale using the corners of the aruco marker
        Mat firstMarker = markerCorners.get(0);

        Log.d("ARUco", "Scale: Rows: "+firstMarker.rows() + " Cols: "+firstMarker.cols());

        double[] point1 = firstMarker.get(0,0); //Top left
        double[] point2 = firstMarker.get(1, 0); //Top right

        double pixelWidth = Math.sqrt(Math.pow(point1[0] - point2[0], 2)  //X Diff
                + Math.pow(point1[1] - point2[1], 2));                    //Y Diff

        double expectedMarkerWidth = 53.0;

        double scale = expectedMarkerWidth / pixelWidth;
        Log.d("ARUco", "Scale (mm per pixel): " + scale);
    }
}