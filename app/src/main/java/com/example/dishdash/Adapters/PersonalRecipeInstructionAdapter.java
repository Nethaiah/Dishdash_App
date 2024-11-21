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
import com.example.dishdash.PersonalRecipeClass.RecipeInstructionClass;
import com.example.dishdash.R;

import java.util.List;

public class PersonalRecipeInstructionAdapter extends RecyclerView.Adapter<InstructionViewHolder> {
    Context context;
    List<RecipeInstructionClass> instructions;

    public PersonalRecipeInstructionAdapter(Context context, List<RecipeInstructionClass> instructions) {
        this.context = context;
        this.instructions = instructions;
    }

    @NonNull
    @Override
    public InstructionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InstructionViewHolder(LayoutInflater.from(context).inflate(R.layout.personal_recipe_instruction, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InstructionViewHolder holder, int position) {
        RecipeInstructionClass instruction = instructions.get(position);

        if (holder.instructionEditText.getTag() instanceof TextWatcher) {
            holder.instructionEditText.removeTextChangedListener((TextWatcher) holder.instructionEditText.getTag());
        }

        holder.instructionEditText.setText(instruction.getInstruction());

        // Add a new TextWatcher
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                instruction.setInstruction(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        holder.instructionEditText.addTextChangedListener(textWatcher);

        holder.instructionEditText.setTag(textWatcher);

        holder.deleteButton.setOnClickListener(v -> {
            instructions.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, instructions.size());
        });
    }

    @Override
    public int getItemCount() {
        return instructions.size();
    }
}

class InstructionViewHolder extends RecyclerView.ViewHolder {
    EditText instructionEditText;
    ImageButton deleteButton;

    public InstructionViewHolder(@NonNull View itemView) {
        super(itemView);
        instructionEditText = itemView.findViewById(R.id.personalRecipeInstruction);
        deleteButton = itemView.findViewById(R.id.instructionDeleteButton);
    }
}
