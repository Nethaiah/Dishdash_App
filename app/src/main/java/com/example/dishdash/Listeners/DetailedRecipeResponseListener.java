package com.example.dishdash.Listeners;

import com.example.dishdash.DetailedRecipeClass.DetailedRecipeAPIResponse;

public interface DetailedRecipeResponseListener {
    void didFetchDetailedRecipe(DetailedRecipeAPIResponse detailedRecipe, String message);
    void didError(String message);
}