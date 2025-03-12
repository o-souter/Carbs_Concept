package com.example.carbs_concept;

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

        public FoodViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.foodImageView);
            textView = itemView.findViewById(R.id.foodTextView);
            carbsView = itemView.findViewById(R.id.carbsTextView);
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
        holder.imageView.setImageResource(foodItem.getImageResId());
        holder.textView.setText(foodItem.getDescription());
        holder.carbsView.setText("Carbohydrates: " + foodItem.getGramsCarbs() + "g");
    }
    @Override
    public int getItemCount() {
        return foodList.size();
    }
}
