package com.example.carbs_concept;

import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.os.Bundle;

import android.util.Log;
import android.widget.Button;

import java.io.File;

import java.nio.ByteBuffer;

import java.util.ArrayList;

import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

//import com.example.carbs_concept.
import android.Manifest;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.MissingGlContextException;
import com.google.ar.core.exceptions.UnavailableException;
import org.opencv.core.Core;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageCapture imageCapture;
    private Button captureButton;
    private ImageButton helpButton;
    private ArucoDetector arucoDetector;
    private TextView detectionFeedback;
    private ImageView liveImageView;
    private ProcessCameraProvider cameraProvider;
    private String defaultBackendAddress = "192.168.1.168:8000";
//    private String defaultServerPort = "5000";
    private String correctBackendAddress;
//    private String correctServerPort;
    private Boolean backendFound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        String testBackendAddress = defaultBackendAddress;
//        String testPort = defaultServerPort;

        Intent intent = getIntent();
        String correctIP = intent.getStringExtra("correctBackendAddress");
//        String correctPort = intent.getStringExtra("correctPort");
        if (correctIP != null) { //If a correct IP has been tested and given by ServerConfigurationActivity, Use this to pass test checks. Otherwise keep as default
            testBackendAddress = correctIP;
//            testPort = correctPort;
            Log.d("Connection to Flask Backend", "Correct Backend address provided: " + testBackendAddress + ":");
        }

        helpButton = findViewById(R.id.btnHelp);
        helpButton.setOnClickListener(v -> {
            Intent showHelp = new Intent(this, HelpActivity.class);
            showHelp.putExtra("correctAddress", correctBackendAddress);
//            showHelp.putExtra("correctPort", correctServerPort);
            startActivity(showHelp);
        });
        helpButton.setEnabled(false);

        backendFound = false;
        testFlaskConnection(testBackendAddress); //Test connection with backend and handle accordingly

        //Confirm OpenCV initialised properly
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed!");
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully");
        }
        Log.d("OpenCV Version", Core.VERSION);

        //Set up versioning
        String versionName = BuildConfig.APP_VERSION_NAME;
        TextView versionText = findViewById(R.id.versionText);
        versionText.setText("C.A.R.B.S CaptureTool v" + versionName);// + " (" + versionCode + ")");
        //Request permissions
        getPermissions();
        //Setup widgets
        captureButton = findViewById(R.id.captureButton);
        captureButton.setOnClickListener(v -> {
            if (imageCapture != null) {
                captureImage();
            }
        });

        liveImageView = findViewById(R.id.liveImageView);
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

    private void testFlaskConnection(String address) {
        ServerConfigurationActivity.probeServerAndWaitForResponse(address, result -> {
            Log.d("Flask backend test", result);
            if (result.contains("Error") | result.contains("Failed") | result.contains("Timed out")) {//If cannot connect using default value
                backendFound = false;
                Intent intent = new Intent(this, ServerConfigurationActivity.class);
                startActivity(intent);


            }
            else {
                //If the address tested is correct, set as the correct address
                correctBackendAddress = address;
//                correctServerPort = port;
                backendFound = true;
                helpButton.setEnabled(true);
            }
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
            cameraProvider = ProcessCameraProvider.getInstance(this).get();
            CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
            Preview preview = new Preview.Builder().build();
            ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this::analyseFrame);
            imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
        } catch (Exception e) {
            Log.e("CameraX", "Camera initialization failed: ", e);
        }

    }


    private void analyseFrame(ImageProxy imageProxy) {
        try {
            Mat frame = rotateMat(imageProxyToMat(imageProxy), 270);

            List<Mat> markerCorners = new ArrayList<>();
            Mat markerIds = new Mat();
            Mat greyImage = new Mat();

            //Convert to greyscale
            Imgproc.cvtColor(frame, greyImage, Imgproc.COLOR_BGR2GRAY);
            Mat rgbFrame = new Mat();
            //Get colour image to show on screen
            Imgproc.cvtColor(frame, rgbFrame, COLOR_BGR2RGB);
            //Increase contrast
            Mat contrastGreyImage = new Mat();
            Core.normalize(greyImage, contrastGreyImage, 0, 255, Core.NORM_MINMAX);

            //Apply gaussian blur
            Mat gaussianBlurImage = new Mat();
            Imgproc.GaussianBlur(contrastGreyImage, gaussianBlurImage, new Size(5, 5), 0);

            //Apply binary threshold to better find markers
            Mat thresholdedFrame = new Mat();
            Imgproc.adaptiveThreshold(contrastGreyImage, thresholdedFrame, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 5);
            Imgcodecs.imwrite(getFilesDir() + "/imageToDetect.jpg", gaussianBlurImage);

            //Detect markers in the current frame
            arucoDetector.detectMarkers(gaussianBlurImage, markerCorners, markerIds);
            Objdetect.drawDetectedMarkers(rgbFrame, markerCorners, markerIds);
            Bitmap bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frame, bitmap);
            Bitmap rgbBitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgbFrame, rgbBitmap);

//            Mat rotatedFrame = rotateMat(frame, 270);
            liveImageView.setImageBitmap(rgbBitmap);

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

                runOnUiThread(() -> {
                    detectionFeedback.setText(markerIds.rows() + " Fiducial marker(s) detected.");
                    detectionFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                    if (backendFound) {
                        captureButton.setEnabled(true);
                        captureButton.setText("Capture");
                    }
                    else {
                        captureButton.setEnabled(false);
                        captureButton.setText("Connecting...");
                    }



                });
            }
            else {
                runOnUiThread(() -> {
                    detectionFeedback.setText("No fiducial marker detected.");
                    detectionFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_light));
//                    captureButton.setEnabled(false);
                    if (backendFound) {
                        captureButton.setEnabled(true);
                        captureButton.setText("Capture");
                    }
                    else {
                        captureButton.setEnabled(false);
                        captureButton.setText("Awaiting connection to backend...");
                    }

                });
            }

        }
        catch (Exception e) {
            Log.e("Aruco", "Error analysing frame: ", e);
        }
        finally {
            imageProxy.close();
        }
    }

    private Mat rotateMat(Mat mat, int rotationAngle) {
        // Get the center of the image
        Point center = new Point(mat.width() / 2, mat.height() / 2);

        // Create a rotation matrix
        Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, rotationAngle, 1.0);

        // Create an empty Mat to store the rotated image
        Mat rotatedMat = new Mat();

        // Apply the rotation
        Imgproc.warpAffine(mat, rotatedMat, rotationMatrix, mat.size());

        return rotatedMat;
    }

    private Mat imageProxyToMat(ImageProxy imageProxy) {
        //Convert imageproxy to openCV mat
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();

        Mat yuv = new Mat(height + height /2, width, CvType.CV_8UC1);

        // Copy Y plane
        ByteBuffer yBuffer = planes[0].getBuffer();
        int ySize = yBuffer.remaining();
        byte[] yBytes = new byte[ySize];
        yBuffer.get(yBytes);
        yuv.put(0, 0, yBytes);

        // Copy U and V planes (interleaved UV for NV21 format)
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int uvSize = uBuffer.remaining();
        byte[] uvBytes = new byte[uvSize * 2];
        uBuffer.get(uvBytes, 0, uvSize);
        vBuffer.get(uvBytes, uvSize, uvSize);
        yuv.put(height, 0, uvBytes);

        // Convert YUV to RGB
        Mat rgb = new Mat();
        Imgproc.cvtColor(yuv, rgb, Imgproc.COLOR_YUV2RGB_NV21);

        // Release the YUV Mat
        yuv.release();

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
                        Log.d("CameraX", "Image saved: " + file.getAbsolutePath());
//                        Mat intrinsicMatrix = getIntrinsicMatrix();
                        processImage(file.getAbsolutePath());

                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Image capture failed: " + exception.getMessage());
                    }
                }
        );
    }


    private void processImage(String imagePath){//, String pointCloudPath) {
        Intent intent = new Intent(this, PointCloudCaptureActivity.class);
        intent.putExtra("imagePath", imagePath);

        double[] matrixData = new double[9];
//        intrinsicMatrix.get(0, 0, matrixData);
//        intent.putExtra("intrinsicMatrix", matrixData);


        intent.putExtra("correctServerAddress", correctBackendAddress);
//        intent.putExtra("correctPort", correctServerPort);
        startActivity(intent);
    }


}