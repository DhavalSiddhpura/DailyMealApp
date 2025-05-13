package com.example.dailymeal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private Context context;
    private List<MealModal> mealList;
    private boolean showQuantityControls;

    public MealAdapter(Context context, List<MealModal> mealList, boolean showQuantityControls) {
        this.context = context;
        this.mealList = mealList;
        this.showQuantityControls = showQuantityControls;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.meal_item, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        MealModal meal = mealList.get(position);

        holder.tvMealName.setText(meal.getName());
        holder.tvMealDescription.setText(meal.getDescription());
        holder.tvMealTypeDay.setText(meal.getMealType());
        holder.tvMealPrice.setText(" " + meal.getPrice());

        // Load image from URL using Glide
        Glide.with(context)
                .load(meal.getImgurl())
                .placeholder(R.drawable.placeholder) // optional placeholder
                .into(holder.imgMeal);

        // Show/hide quantity layout
        if (showQuantityControls) {
            holder.quantityLayout.setVisibility(View.VISIBLE);
        } else {
            holder.quantityLayout.setVisibility(View.GONE);
        }

        // Order button functionality
        holder.btnOrder.setOnClickListener(v -> {
            SelectedMealManager.getInstance().addMeal(meal);

            Intent intent = new Intent(context, Order.class);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {

        ImageView imgMeal;
        TextView tvMealName, tvMealDescription, tvMealTypeDay, tvMealPrice;
        Button btnOrder;
        LinearLayout quantityLayout;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);

            imgMeal = itemView.findViewById(R.id.imgMeal);
            tvMealName = itemView.findViewById(R.id.tvMealName);
            tvMealDescription = itemView.findViewById(R.id.tvMealDescription);
            tvMealTypeDay = itemView.findViewById(R.id.tvMealTypeDay);
            tvMealPrice = itemView.findViewById(R.id.tvMealPrice);
            btnOrder = itemView.findViewById(R.id.btnOrder);
            quantityLayout = itemView.findViewById(R.id.quantityLayout); // Make sure this matches the ID in XML
        }
    }
}
