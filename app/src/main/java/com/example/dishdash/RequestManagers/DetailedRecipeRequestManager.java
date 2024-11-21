package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dishdash.DetailedRecipeClass.DetailedRecipeAPIResponse;
import com.example.dishdash.Listeners.DetailedRecipeResponseListener;
import com.example.dishdash.Listeners.RandomRecipeResponseListener;
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

public class DetailedRecipeRequestManager {
    Context context;
    APIKeyManager apiKeyManager;

    Retrofit retroFit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public DetailedRecipeRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void getDetailedRecipe(DetailedRecipeResponseListener listener, int recipeId){
        CallDetailedRecipe callDetailedRecipe = retroFit.create(CallDetailedRecipe.class);
        String apiKey = apiKeyManager.getCurrentApiKey();
        Call<DetailedRecipeAPIResponse> call = callDetailedRecipe.callDetailedRecipe(recipeId, apiKey);

        call.enqueue(new Callback<DetailedRecipeAPIResponse>() {
            @Override
            public void onResponse(@NonNull Call<DetailedRecipeAPIResponse> call, @NonNull Response<DetailedRecipeAPIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logQuotaUsage(response);
                    listener.didFetchDetailedRecipe(response.body(), response.message());
                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()) {
                        getDetailedRecipe(listener, recipeId);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                } else {
                    listener.didError(response.message());
                    return;
                }
            }

            @Override
            public void onFailure(@NonNull Call<DetailedRecipeAPIResponse> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private void logQuotaUsage(Response<DetailedRecipeAPIResponse> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallDetailedRecipe{
        @GET("recipes/{id}/information")
        Call<DetailedRecipeAPIResponse> callDetailedRecipe(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }
}
