package com.example.dishdash.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.dishdash.RandomRecipeClass.Recipe;

import java.util.List;

public class RandomRecipeViewModel extends ViewModel {
    MutableLiveData<List<Recipe>> randomRecipes = new MutableLiveData<>();
    boolean isDataLoaded = false;
    String currentTags = "";

    public LiveData<List<Recipe>> getRandomRecipes() {
        return randomRecipes;
    }

    public void setRandomRecipes(List<Recipe> recipes) {
        randomRecipes.setValue(recipes);
        isDataLoaded = true;
    }

    public boolean isDataLoaded() {
        return isDataLoaded;
    }

    public boolean shouldFetch(String newTags) {
        if (!isDataLoaded || !currentTags.equals(newTags)) {
            currentTags = newTags;
            return true;
        }
        return false;
    }
}
