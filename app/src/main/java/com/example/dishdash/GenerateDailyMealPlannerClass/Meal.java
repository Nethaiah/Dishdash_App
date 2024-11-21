package com.example.dishdash.GenerateDailyMealPlannerClass;

public class Meal {
    public int id;
    public String imageType;
    public String title;
    public int readyInMinutes;
    public int servings;
    public String sourceUrl;

    public Meal() {
    }

    public Meal(int id, String imageType, String title, int readyInMinutes, int servings, String sourceUrl) {
        this.id = id;
        this.imageType = imageType;
        this.title = title;
        this.readyInMinutes = readyInMinutes;
        this.servings = servings;
        this.sourceUrl = sourceUrl;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public void setReadyInMinutes(int readyInMinutes) {
        this.readyInMinutes = readyInMinutes;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }
}
