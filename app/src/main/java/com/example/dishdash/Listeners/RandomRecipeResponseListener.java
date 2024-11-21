package com.example.dishdash.Listeners;

import com.example.dishdash.RandomRecipeClass.RandomRecipeAPIResponse;

public interface RandomRecipeResponseListener {
    void didFetchRandomRecipe(RandomRecipeAPIResponse randomRecipe, String message);
    void didError(String message);
}
