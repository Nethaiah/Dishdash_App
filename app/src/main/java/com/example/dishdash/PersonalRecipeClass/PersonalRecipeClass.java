package com.example.dishdash.PersonalRecipeClass;

import android.net.Uri;

import java.util.List;

public class PersonalRecipeClass {
    String id;
    String image;
    String recipeName;
    List<String> ingredients;
    List<String> instructions;

    public PersonalRecipeClass() {
    }

    public PersonalRecipeClass(String id, String image, String recipeName, List<String> ingredients, List<String> instructions) {
        this.id = id;
        this.image = image;
        this.recipeName = recipeName;
        this.ingredients = ingredients;
        this.instructions = instructions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }
}
