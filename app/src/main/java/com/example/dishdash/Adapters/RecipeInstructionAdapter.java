package com.example.dishdash.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishdash.R;
import com.example.dishdash.RecipeInstructionClass.RecipeInstructionAPIResponse;

import java.util.ArrayList;

public class RecipeInstructionAdapter extends RecyclerView.Adapter<RecipeInstructionViewHolder> {
    Context context;
    ArrayList<RecipeInstructionAPIResponse> list;

    public RecipeInstructionAdapter(Context context, ArrayList<RecipeInstructionAPIResponse> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RecipeInstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecipeInstructionViewHolder(LayoutInflater.from(context).inflate(R.layout.instruction_steps, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeInstructionViewHolder holder, int position) {
        /*holder.instructionName.setText(list.get(position).name);*/
        holder.instructionNumberAndSteps.setHasFixedSize(true);
        holder.instructionNumberAndSteps.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        RecipeInstructionStepsAdapter recipeInstructionStepsAdapter = new RecipeInstructionStepsAdapter(context, list.get(position).steps);
        holder.instructionNumberAndSteps.setAdapter(recipeInstructionStepsAdapter);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
class RecipeInstructionViewHolder extends RecyclerView.ViewHolder {
    /*TextView instructionName;*/
    RecyclerView instructionNumberAndSteps;

    public RecipeInstructionViewHolder(@NonNull View itemView) {
        super(itemView);
        /*instructionName = itemView.findViewById(R.id.instructionName);*/
        instructionNumberAndSteps = itemView.findViewById(R.id.instructionNumberAndSteps);
    }
}

