package com.example.dishdash.FavoriteRecipeClass;

public class FavoriteRecipeClass {
    String recipeName, recipeImage, readyIn, servings;
    int recipeId;

    public FavoriteRecipeClass() {
    }

    public FavoriteRecipeClass(String recipeName, String recipeImage, String readyIn, String servings, int recipeId) {
        this.recipeName = recipeName;
        this.recipeImage = recipeImage;
        this.readyIn = readyIn;
        this.servings = servings;
        this.recipeId = recipeId;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public String getReadyIn() {
        return readyIn;
    }

    public void setReadyIn(String readyIn) {
        this.readyIn = readyIn;
    }

    public String getServings() {
        return servings;
    }

    public void setServings(String servings) {
        this.servings = servings;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }
}
