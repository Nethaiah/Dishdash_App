package com.example.dishdash.Listeners;

import android.graphics.Bitmap;

public interface NutritionRecipeResponseListener {
    void didFetchRecipeNutritionImage(Bitmap nutritionImage);
    void didFetchRecipeNutritionImageUrl(String imageUrl);  // New method for URL
    void didError(String message);
}
