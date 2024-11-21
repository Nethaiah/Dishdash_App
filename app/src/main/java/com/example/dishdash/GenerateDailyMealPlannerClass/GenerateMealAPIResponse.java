package com.example.dishdash.GenerateDailyMealPlannerClass;

import java.util.ArrayList;

public class GenerateMealAPIResponse {
    public ArrayList<Meal> meals;
    public Nutrients nutrients;

    public GenerateMealAPIResponse() {
    }

    public GenerateMealAPIResponse(ArrayList<Meal> meals, Nutrients nutrients) {
        this.meals = meals;
        this.nutrients = nutrients;
    }

    public Nutrients getNutrients() {
        return nutrients;
    }

    public void setNutrients(Nutrients nutrients) {
        this.nutrients = nutrients;
    }

    public ArrayList<Meal> getMeals() {
        return meals;
    }

    public void setMeals(ArrayList<Meal> meals) {
        this.meals = meals;
    }
}
