package com.example.carbs_concept;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<IndividualFoodItem> foodList;

    public FoodAdapter(List<IndividualFoodItem> foodList) {
        this.foodList = foodList;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        TextView carbsView;
        TextView confidenceView;
        TextView weightView;
        TextView volumeView;

        public FoodViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.foodImageView);
            textView = itemView.findViewById(R.id.foodTextView);
            carbsView = itemView.findViewById(R.id.carbsTextView);
            confidenceView = itemView.findViewById(R.id.confidenceTextView);
            weightView = itemView.findViewById(R.id.weightTextView);
            volumeView = itemView.findViewById(R.id.volumeTextView);
        }
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        IndividualFoodItem foodItem = foodList.get(position);
        String imagePath = foodItem.getImagePath();
        if (imagePath == null) { //Invalid detection
            holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        else {
            Bitmap imgBitmap = BitmapFactory.decodeFile(foodItem.getImagePath());
            holder.imageView.setImageBitmap(imgBitmap);
        }

        holder.textView.setText(foodItem.getDescription());
        holder.carbsView.setText("Carbohydrates: " + foodItem.getGramsCarbs() + "g");
        holder.confidenceView.setText("Confidence: " + foodItem.getDetectionConfidence() + "%");
        holder.weightView.setText("Estimated weight: " + foodItem.getEstimatedWeight() + "g");
        holder.volumeView.setText("Estimated volume: " + foodItem.getEstimatedVolume() + "cm^3");
    }
    @Override
    public int getItemCount() {
        return foodList.size();
    }
}
