package com.example.carbs_concept;

public class IndividualFoodItem {
    private String imagePath;
    private String description;
    private double gramsCarbs;

    public IndividualFoodItem(String imagePath, String description, double gramsCarbs) {
        this.imagePath = imagePath;
        this.description = description;
        this.gramsCarbs = gramsCarbs;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDescription() {
        String beautifiedDescription = description;
        beautifiedDescription = beautifiedDescription.replace("-", " ");
        beautifiedDescription = beautifiedDescription.substring(0, 1).toUpperCase() + beautifiedDescription.substring(1);
        return beautifiedDescription;
    }

    public double getGramsCarbs() {
        return gramsCarbs;
    }
}
