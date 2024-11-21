package com.example.dishdash.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishdash.Listeners.RecipeClickResponseListener;
import com.example.dishdash.R;
import com.example.dishdash.RandomRecipeClass.Recipe;

import java.util.List;
import java.util.Locale;

public class RandomRecipeAdapter extends RecyclerView.Adapter<RandomRecipeViewHolder> {
    Context context;
    List<Recipe> list;
    Recipe recipe;
    RecipeClickResponseListener listener;

    public RandomRecipeAdapter(Context context, List<Recipe> list, RecipeClickResponseListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RandomRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RandomRecipeViewHolder(LayoutInflater.from(context).inflate(R.layout.list_home_random_recipe, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RandomRecipeViewHolder holder, int position) {
        recipe = list.get(position);
        holder.homeRandomRecipeName.setText(String.valueOf(list.get(position).title));
        holder.randomRecipeReadyIn.setText(list.get(position).readyInMinutes + "Minutes");
        holder.randomRecipeServing.setText(String.valueOf(list.get(position).servings));
        holder.randomRecipeScore.setText(String.format(Locale.getDefault(), "%.2f", recipe.getSpoonacularScore()));
        Glide.with(context).load(list.get(position).image).into(holder.homeRandomRecipeImage);

        holder.homeRandomRecipeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecipeClick(String.valueOf(list.get(holder.getAdapterPosition()).id));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class RandomRecipeViewHolder extends RecyclerView.ViewHolder {
    CardView homeRandomRecipeCardView;
    TextView homeRandomRecipeName, randomRecipeReadyIn, randomRecipeServing, randomRecipeScore;
    ImageView homeRandomRecipeImage;

    public RandomRecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        homeRandomRecipeCardView = itemView.findViewById(R.id.homeRandomRecipeCardView);
        homeRandomRecipeImage = itemView.findViewById(R.id.homeRandomRecipeImage);
        homeRandomRecipeName = itemView.findViewById(R.id.randomRecipeTitle);
        randomRecipeReadyIn = itemView.findViewById(R.id.randomRecipeReadyIn);
        randomRecipeServing = itemView.findViewById(R.id.randomRecipeServing);
        randomRecipeScore = itemView.findViewById(R.id.randomRecipeScore);
    }
}
