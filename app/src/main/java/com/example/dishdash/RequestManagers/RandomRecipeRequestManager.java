package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dishdash.Listeners.RandomRecipeResponseListener;
import com.example.dishdash.R;
import com.example.dishdash.RandomRecipeClass.RandomRecipeAPIResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class RandomRecipeRequestManager {
    Context context;
    APIKeyManager apiKeyManager;

    Retrofit retroFit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public RandomRecipeRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void getRandomRecipes(RandomRecipeResponseListener listener, String tags){
        CallRandomRecipes callRandomRecipes = retroFit.create(CallRandomRecipes.class);
        String apiKey = apiKeyManager.getCurrentApiKey();
        Call<RandomRecipeAPIResponse> call = callRandomRecipes.callRandomRecipe(apiKey, "10", tags);

        call.enqueue(new Callback<RandomRecipeAPIResponse>() {
            @Override
            public void onResponse(@NonNull Call<RandomRecipeAPIResponse> call, @NonNull Response<RandomRecipeAPIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logQuotaUsage(response);
                    listener.didFetchRandomRecipe(response.body(), response.message());
                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()) {
                        getRandomRecipes(listener, tags);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                } else {
                    listener.didError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RandomRecipeAPIResponse> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private void logQuotaUsage(Response<RandomRecipeAPIResponse> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallRandomRecipes{
        @GET("recipes/random")
        Call<RandomRecipeAPIResponse> callRandomRecipe(
            @Query("apiKey") String apiKey,
            @Query("number") String number,
            @Query("tags") String tags
        );
    }
}
