package com.example.dishdash.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishdash.GenerateDailyMealPlannerClass.Nutrients;
import com.example.dishdash.R;

import java.util.List;

public class GenerateMealPlanNutritionAdapter extends RecyclerView.Adapter<GenerateMealPlanNutrientsViewHolder>{
    Context context;
    Nutrients nutrients;

    public GenerateMealPlanNutritionAdapter(Context context, Nutrients nutrients) {
        this.context = context;
        this.nutrients = nutrients;
    }

    @NonNull
    @Override
    public GenerateMealPlanNutrientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GenerateMealPlanNutrientsViewHolder(LayoutInflater.from(context).inflate(R.layout.meal_planner_recipe_nutrition, parent, false));
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull GenerateMealPlanNutrientsViewHolder holder, int position) {
        holder.mealPlannerNutritionCalories.setText(String.format("%.2f kcal", nutrients.getCalories()));
        holder.mealPlannerNutritionCarbs.setText(String.format("%.2f g", nutrients.getCarbohydrates()));
        holder.mealPlannerNutritionFat.setText(String.format("%.2f g", nutrients.getFat()));
        holder.mealPlannerNutritionProtein.setText(String.format("%.2f g", nutrients.getProtein()));
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}

class GenerateMealPlanNutrientsViewHolder extends RecyclerView.ViewHolder{
    TextView mealPlannerNutritionCalories, mealPlannerNutritionCarbs, mealPlannerNutritionFat, mealPlannerNutritionProtein;

    public GenerateMealPlanNutrientsViewHolder(@NonNull View itemView) {
        super(itemView);
        mealPlannerNutritionCalories = itemView.findViewById(R.id.mealPlannerNutritionCalories);
        mealPlannerNutritionCarbs = itemView.findViewById(R.id.mealPlannerNutritionCarbohydrates);
        mealPlannerNutritionFat = itemView.findViewById(R.id.mealPlannerNutritionFat);
        mealPlannerNutritionProtein = itemView.findViewById(R.id.mealPlannerNutritionProtein);

    }
}
