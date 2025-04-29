// FoodAdapter.java - handles the food list recyclerview to display food items detected and their information
package com.example.carbs_concept;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<IndividualFoodItem> foodList;
    private static final DecimalFormat df = new DecimalFormat("0.00");
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
        ImageButton deleteBtn;


        public FoodViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.foodImageView);
            textView = itemView.findViewById(R.id.foodTextView);
            carbsView = itemView.findViewById(R.id.carbsTextView);
            confidenceView = itemView.findViewById(R.id.confidenceTextView);
            weightView = itemView.findViewById(R.id.weightTextView);
            volumeView = itemView.findViewById(R.id.volumeTextView);
            deleteBtn = itemView.findViewById(R.id.btnRemoveItem);
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
        } else {
            Bitmap imgBitmap = BitmapFactory.decodeFile(foodItem.getImagePath());
            holder.imageView.setImageBitmap(imgBitmap);
        }
        //Set the views to reflect food results
        holder.textView.setText(foodItem.getDescription());
        holder.carbsView.setText("Carbohydrates: " + df.format(foodItem.getGramsCarbs()) + "g");
        holder.confidenceView.setText("Confidence: " + foodItem.getDetectionConfidence() + "%");
        holder.weightView.setText("Estimated weight: " + df.format(foodItem.getEstimatedWeight()) + "g");
        holder.volumeView.setText("Estimated volume: " + df.format(foodItem.getEstimatedVolume()) + "cm³");
        holder.deleteBtn.setOnClickListener(v -> { //Set a listener to delete the food item if the user presses the x
            removeItem(foodItem.getUniqueId());
        });

        if (!foodItem.isCloseable()) { //If not a closable item (e.g. alert) then hide the x
            holder.deleteBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public List<IndividualFoodItem> getFoodItems() {
        return foodList;
    }

    public void removeItem(String idOfItemToDelete) {
        //Delete an item from the recycler view
        int positionToRemove = -1; // Find the position of the item to delete
        for (int i = 0; i < foodList.size(); i++) {
            if (Objects.equals(foodList.get(i).getUniqueId(), idOfItemToDelete)) {
                positionToRemove = i;
                break;
            }
        }

        // If item exists, remove it from the list and notify the adapter
        if (positionToRemove != -1) {
            foodList.remove(positionToRemove);
            notifyItemRemoved(positionToRemove);
            notifyItemRangeChanged(positionToRemove, foodList.size());  // Update the list after removal
            Log.d("FoodAdapter", "Removed item with id " + idOfItemToDelete);
        }
    }
}
