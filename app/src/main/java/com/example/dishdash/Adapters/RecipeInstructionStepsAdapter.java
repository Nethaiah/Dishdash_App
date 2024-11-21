package com.example.dishdash.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishdash.R;
import com.example.dishdash.RecipeInstructionClass.Step;

import java.util.ArrayList;

public class RecipeInstructionStepsAdapter extends RecyclerView.Adapter<RecipeInstructionStepsViewHolder> {
    Context context;
    ArrayList<Step> list;

    public RecipeInstructionStepsAdapter(Context context, ArrayList<Step> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecipeInstructionStepsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeInstructionStepsViewHolder(LayoutInflater.from(context).inflate(R.layout.recipe_instruction_number_steps, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecipeInstructionStepsViewHolder holder, int position) {
        holder.recipeInstructionNumber.setText(list.get(position).number + ".");
        holder.recipeInstructionSteps.setText(list.get(position).step);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
class RecipeInstructionStepsViewHolder extends RecyclerView.ViewHolder {
    TextView recipeInstructionNumber, recipeInstructionSteps;

    public RecipeInstructionStepsViewHolder(@NonNull View itemView) {
        super(itemView);
        recipeInstructionNumber = itemView.findViewById(R.id.recipeInstructionNumber);
        recipeInstructionSteps = itemView.findViewById(R.id.recipeInstructionSteps);
    }
}
