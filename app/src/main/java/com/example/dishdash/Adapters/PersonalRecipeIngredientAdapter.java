package com.example.dishdash.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishdash.PersonalRecipeClass.RecipeIngredientClass;
import com.example.dishdash.R;

import java.util.List;

public class PersonalRecipeIngredientAdapter extends RecyclerView.Adapter<IngredientViewHolder> {
    Context context;
    List<RecipeIngredientClass> ingredients;

    public PersonalRecipeIngredientAdapter(Context context, List<RecipeIngredientClass> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IngredientViewHolder(LayoutInflater.from(context).inflate(R.layout.personal_recipe_ingredient, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        RecipeIngredientClass ingredient = ingredients.get(position);

        if (holder.ingredientEditText.getTag() instanceof TextWatcher) {
            holder.ingredientEditText.removeTextChangedListener((TextWatcher) holder.ingredientEditText.getTag());
        }

        holder.ingredientEditText.setText(ingredient.getIngredient());

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ingredient.setIngredient(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        holder.ingredientEditText.addTextChangedListener(textWatcher);

        holder.ingredientEditText.setTag(textWatcher);

        holder.deleteButton.setOnClickListener(v -> {
            ingredients.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, ingredients.size());
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }
}

class IngredientViewHolder extends RecyclerView.ViewHolder {
    EditText ingredientEditText;
    ImageButton deleteButton;

    public IngredientViewHolder(@NonNull View itemView) {
        super(itemView);
        ingredientEditText = itemView.findViewById(R.id.personalRecipeIngredient);
        deleteButton = itemView.findViewById(R.id.ingredientsDeleteButton);
    }
}