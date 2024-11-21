package com.example.dishdash.Listeners;

import com.example.dishdash.RecipeInstructionClass.RecipeInstructionAPIResponse;

import java.util.ArrayList;

public interface RecipeInstructionResponseListener {
    void didFetchRecipeInstruction(ArrayList<RecipeInstructionAPIResponse> recipeInstruction, String message);
    void didError(String message);
}
