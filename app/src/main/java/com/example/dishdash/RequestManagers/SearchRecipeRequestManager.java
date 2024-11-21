package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dishdash.Listeners.SearchRecipeResponseListener;
import com.example.dishdash.R;
import com.example.dishdash.RecipeInstructionClass.RecipeInstructionAPIResponse;
import com.example.dishdash.SearchRecipeClass.SearchRecipeAPIResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class SearchRecipeRequestManager {
    Context context;
    APIKeyManager apiKeyManager;

    Retrofit retroFit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public SearchRecipeRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void getSearchRecipes(SearchRecipeResponseListener listener, String ingredients) {
        CallSearchRecipes callSearchRecipes = retroFit.create(CallSearchRecipes.class);
        String apiKey = apiKeyManager.getCurrentApiKey();
        Call<ArrayList<SearchRecipeAPIResponse>> call = callSearchRecipes.callSearchRecipes(apiKey, "10", ingredients);
        call.enqueue(new Callback<ArrayList<SearchRecipeAPIResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<SearchRecipeAPIResponse>> call, @NonNull Response<ArrayList<SearchRecipeAPIResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logQuotaUsage(response);
                    listener.didFetchSearchRecipes(response.body(), response.message());
                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()) {
                        getSearchRecipes(listener, ingredients);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                } else {
                    listener.didError(response.message());
                    return;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<SearchRecipeAPIResponse>> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private void logQuotaUsage(Response<ArrayList<SearchRecipeAPIResponse>> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallSearchRecipes{
        @GET("recipes/findByIngredients")
        Call<ArrayList<SearchRecipeAPIResponse>> callSearchRecipes(
            @Query("apiKey") String apiKey,
            @Query("number") String number,
            @Query("ingredients") String ingredients
        );
    }
}
