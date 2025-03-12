package com.example.carbs_concept;

public class IndividualFoodItem {
    private int imageResId;
    private String description;
    private double gramsCarbs;

    public IndividualFoodItem(int imageResId, String description, double gramsCarbs) {
        this.imageResId = imageResId;
        this.description = description;
        this.gramsCarbs = gramsCarbs;
    }

    public int getImageResId() {
        return imageResId;
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
