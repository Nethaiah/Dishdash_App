package com.example.dishdash.Listeners;

import com.example.dishdash.GenerateDailyMealPlannerClass.GenerateMealAPIResponse;

public interface GenerateMealPlanResponseListener {
    void didFetchGenerateMealPlan(GenerateMealAPIResponse response, String message);
    void didError(String message);
}
