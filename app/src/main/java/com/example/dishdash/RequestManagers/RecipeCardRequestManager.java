package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dishdash.GenerateRecipeCardClass.GenerateRecipeCardAPIResponse;
import com.example.dishdash.Listeners.GenerateRecipeCardResponseListener;
import com.example.dishdash.R;
import com.example.dishdash.RandomRecipeClass.RandomRecipeAPIResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RecipeCardRequestManager {
    Context context;
    APIKeyManager apiKeyManager;

    Retrofit retroFit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public RecipeCardRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void generateRecipeCard(GenerateRecipeCardResponseListener listener, int recipeId, String mask, String backgroundColor, String fontColor){
        CallGenerateRecipeCard callGenerateRecipeCard = retroFit.create(CallGenerateRecipeCard.class);
        String apiKey = apiKeyManager.getCurrentApiKey();
        Call<GenerateRecipeCardAPIResponse> call = callGenerateRecipeCard.callRecipeCard(recipeId, apiKey, mask, backgroundColor, fontColor);

        call.enqueue(new Callback<GenerateRecipeCardAPIResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenerateRecipeCardAPIResponse> call, @NonNull Response<GenerateRecipeCardAPIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logQuotaUsage(response);
                    listener.didFetchRecipeCard(response.body(), response.message());
                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()) {
                        generateRecipeCard(listener, recipeId, mask, backgroundColor, fontColor);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                } else {
                    listener.didError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenerateRecipeCardAPIResponse> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private void logQuotaUsage(Response<GenerateRecipeCardAPIResponse> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallGenerateRecipeCard {
        @GET("recipes/{id}/card")
        Call<GenerateRecipeCardAPIResponse> callRecipeCard(
                @Path("id") int id,
                @Query("apiKey") String apiKey,
                @Query("mask") String mask,
                @Query("backgroundColor") String backgroundColor,
                @Query("fontColor") String fontColor
        );
    }
}

