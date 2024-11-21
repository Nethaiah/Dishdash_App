package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dishdash.DetailedRecipeClass.DetailedRecipeAPIResponse;
import com.example.dishdash.GenerateDailyMealPlannerClass.GenerateMealAPIResponse;
import com.example.dishdash.Listeners.GenerateMealPlanResponseListener;
import com.example.dishdash.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class GenerateMealPlanRequestManager {
    Context context;
    APIKeyManager apiKeyManager;

    Retrofit retroFit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public GenerateMealPlanRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void getGenerateMealPlan(GenerateMealPlanResponseListener listener, String targetCalories, String diet, String exclude) {
        CallGenerateMealPlan callGenerateMealPlan = retroFit.create(CallGenerateMealPlan.class);
        String apiKey = apiKeyManager.getCurrentApiKey();
        Call<GenerateMealAPIResponse> call = callGenerateMealPlan.callMealPlan("day", targetCalories, diet, exclude, apiKey);

        call.enqueue(new Callback<GenerateMealAPIResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenerateMealAPIResponse> call, @NonNull Response<GenerateMealAPIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logQuotaUsage(response);
                    listener.didFetchGenerateMealPlan(response.body(), response.message());

                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()){
                        getGenerateMealPlan(listener, targetCalories, diet, exclude);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                } else {
                    listener.didError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenerateMealAPIResponse> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private void logQuotaUsage(Response<GenerateMealAPIResponse> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallGenerateMealPlan {
        @GET("mealplanner/generate")
        Call<GenerateMealAPIResponse> callMealPlan(
                @Query("timeFrame") String timeFrame,
                @Query("targetCalories") String targetCalories,
                @Query("diet") String diet,
                @Query("exclude") String exclude,
                @Query("apiKey") String apiKey
        );
    }
}
