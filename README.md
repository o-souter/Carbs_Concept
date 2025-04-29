# CARBS Android App

## Overview
The **CARBS Android App** is the CARBS Capture Tool. It allows individuals with Type 1 Diabetes to capture images of meals and automatically estimate carbohydrate content using AI-powered backend processing.

## Features
- Capture meal images directly within the app.
- Real-time fiducial marker detection for accurate volume scaling.
- Backend server connection (local or Azure-hosted).
- Receive carbohydrate, weight, and volume estimates per food item.
- User control to remove misclassified items from results.
- Help section for printing fiducial markers and listing supported foods.

## Technologies
- Android Studio
- Java
- CameraX API
- OkHTTP
- Live ArUco marker detection with OpenCV for Android
