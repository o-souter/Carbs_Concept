package com.example.carbs_concept;

public class IndividualFoodItem {
    private String imagePath;
    private String description;
    private double gramsCarbs;
    private double estimatedWeight;
    private double estimatedVolume;
    private double detectionConfidence;
    private String uniqueId;
    public IndividualFoodItem(String imagePath, String description, double gramsCarbs, double estimatedWeight, double estimatedVolume, double detectionConfidence) {
        this.imagePath = imagePath;
        this.description = description;
        this.gramsCarbs = gramsCarbs;
        this.estimatedWeight = estimatedWeight;
        this.estimatedVolume = estimatedVolume;
        this.detectionConfidence = detectionConfidence;
        this.uniqueId = this.description + "_" + this.estimatedWeight;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDescription() {
        String beautifiedDescription = description;
        beautifiedDescription = beautifiedDescription.replace("-", " ");
        beautifiedDescription = beautifiedDescription.substring(0, 1).toUpperCase() + beautifiedDescription.substring(1);
        if (beautifiedDescription.contains("_")) {//If name contains unique number on end, remove for display e.g. hamburger_2 => hamburger
            beautifiedDescription = beautifiedDescription.split("_")[0];
        }
        return beautifiedDescription;
    }

    public double getGramsCarbs() {
        return gramsCarbs;
    }
    public double getEstimatedWeight() {
        return estimatedWeight;
    }
    public double getEstimatedVolume() {
        return estimatedVolume;
    }

    public int getDetectionConfidence() {
        return (int)(detectionConfidence*100);

    }

    public String getUniqueId() {
        return uniqueId;
    }
}
