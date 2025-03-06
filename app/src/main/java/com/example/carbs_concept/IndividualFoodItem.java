package com.example.carbs_concept;

public class IndividualFoodItem {
    private int imageResId;
    private String description;

    public IndividualFoodItem(int imageResId, String description) {
        this.imageResId = imageResId;
        this.description = description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getDescription() {
        return description;
    }
}
