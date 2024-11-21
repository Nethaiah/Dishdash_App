package com.example.dishdash.Listeners;

import com.example.dishdash.GenerateRecipeCardClass.GenerateRecipeCardAPIResponse;

public interface GenerateRecipeCardResponseListener {
    void didFetchRecipeCard(GenerateRecipeCardAPIResponse response, String message);
    void didError(String message);
}
