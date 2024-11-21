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
import com.example.dishdash.SearchRecipeClass.MissedIngredient;
import com.example.dishdash.SearchRecipeClass.SearchRecipeAPIResponse;
import com.example.dishdash.SearchRecipeClass.UsedIngredient;

import java.util.ArrayList;

public class SearchRecipeAdapter extends RecyclerView.Adapter<SearchRecipeViewHolder> {
    Context context;
    ArrayList<SearchRecipeAPIResponse> list;
    SearchRecipeAPIResponse recipe;
    RecipeClickResponseListener listener;

    public SearchRecipeAdapter(Context context, ArrayList<SearchRecipeAPIResponse> list, RecipeClickResponseListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchRecipeViewHolder(LayoutInflater.from(context).inflate(R.layout.list_search_recipe, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchRecipeViewHolder holder, int position) {
        recipe = list.get(position);
        StringBuilder usedIngredients = new StringBuilder();
        StringBuilder missedIngredients = new StringBuilder();

        holder.searchRecipeName.setText(recipe.title);

        for (UsedIngredient ingredient : recipe.usedIngredients) {
            usedIngredients.append(ingredient.name).append(", ");
        }

        if (usedIngredients.length() > 0) {
            usedIngredients.setLength(usedIngredients.length() - 2);
        }
        holder.searchRecipeUsedIngredients.setText(usedIngredients.toString());

        for (MissedIngredient ingredient : recipe.missedIngredients) {
            missedIngredients.append(ingredient.name).append(", ");
        }

        if (missedIngredients.length() > 0) {
            missedIngredients.setLength(missedIngredients.length() - 2);
        }
        holder.searchRecipeMissedIngredients.setText(missedIngredients.toString());

        Glide.with(context).load(recipe.image).into(holder.searchRecipeImage);

        holder.searchRecipeCardView.setOnClickListener(new View.OnClickListener() {
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

class SearchRecipeViewHolder extends RecyclerView.ViewHolder {
    CardView searchRecipeCardView;
    TextView searchRecipeName, searchRecipeUsedIngredients, searchRecipeMissedIngredients;
    ImageView searchRecipeImage;

    public SearchRecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        searchRecipeCardView = itemView.findViewById(R.id.searchRecipeCardView);
        searchRecipeImage = itemView.findViewById(R.id.searchRecipeImage);
        searchRecipeName = itemView.findViewById(R.id.searchRecipeTitle);
        searchRecipeUsedIngredients = itemView.findViewById(R.id.searchRecipeUsedIngredients);
        searchRecipeMissedIngredients = itemView.findViewById(R.id.searchRecipeMissedIngredients);
    }
}
