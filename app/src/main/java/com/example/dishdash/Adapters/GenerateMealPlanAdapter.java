package com.example.dishdash.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishdash.GenerateDailyMealPlannerClass.Meal;
import com.example.dishdash.Listeners.RecipeClickResponseListener;
import com.example.dishdash.R;

import java.util.List;

public class GenerateMealPlanAdapter extends RecyclerView.Adapter<GenerateMealPlanViewHolder> {
    Context context;
    List<Meal> mealList;
    RecipeClickResponseListener listener;
    String[] mealTypes = {"Breakfast", "Lunch", "Dinner"};
    String[] mealTimes;

    public GenerateMealPlanAdapter(Context context, List<Meal> mealList, RecipeClickResponseListener listener, String[] mealTimes) {
        this.context = context;
        this.mealList = mealList;
        this.listener = listener;
        this.mealTimes = mealTimes;
    }

    @NonNull
    @Override
    public GenerateMealPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GenerateMealPlanViewHolder(LayoutInflater.from(context).inflate(R.layout.meal_planner_recipe, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull GenerateMealPlanViewHolder holder, int position) {
        Meal meal = mealList.get(position);

        holder.mealPlannerRecipeTitle.setText(mealList.get(position).title);
        holder.mealPlannerRecipeReadyIn.setText(mealList.get(position).getReadyInMinutes() + " Minutes");
        holder.mealPlannerRecipeServing.setText(mealList.get(position).getServings() + " Servings");

        String imageUrl = "https://spoonacular.com/recipeImages/" + meal.getId() + "-556x370." + meal.getImageType();
        Glide.with(context).load(imageUrl).into(holder.recipePlannerRecipeImage);

        if (position < mealTypes.length) {
            holder.mealPlannerRecipeType.setText(mealTypes[position]);
            holder.mealPlannerNotificationTime.setText(mealTimes[position]);
        }

        holder.mealPlannerRecipeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecipeClick(String.valueOf(mealList.get(holder.getAdapterPosition()).id));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }
}

class GenerateMealPlanViewHolder extends RecyclerView.ViewHolder {
    CardView mealPlannerRecipeCardView;
    TextView mealPlannerRecipeTitle, mealPlannerRecipeReadyIn, mealPlannerRecipeServing, mealPlannerRecipeType, mealPlannerNotificationTime;
    ImageView recipePlannerRecipeImage;

    public GenerateMealPlanViewHolder(@NonNull View itemView) {
        super(itemView);
        mealPlannerRecipeCardView = itemView.findViewById(R.id.mealPlannerRecipeCardView);
        recipePlannerRecipeImage = itemView.findViewById(R.id.recipePlannerRecipeImage);
        mealPlannerRecipeTitle = itemView.findViewById(R.id.mealPlannerRecipeTitle);
        mealPlannerRecipeReadyIn = itemView.findViewById(R.id.mealPlannerRecipeReadyIn);
        mealPlannerRecipeServing = itemView.findViewById(R.id.mealPlannerRecipeServing);
        mealPlannerRecipeType = itemView.findViewById(R.id.mealPlannerRecipeType);
        mealPlannerNotificationTime = itemView.findViewById(R.id.mealPlannerNotificationTime);
    }
}
