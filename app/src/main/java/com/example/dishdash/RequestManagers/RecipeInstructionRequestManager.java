package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dishdash.GenerateRecipeCardClass.GenerateRecipeCardAPIResponse;
import com.example.dishdash.Listeners.RecipeInstructionResponseListener;
import com.example.dishdash.R;
import com.example.dishdash.RecipeInstructionClass.RecipeInstructionAPIResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RecipeInstructionRequestManager {
    Context context;
    APIKeyManager apiKeyManager;

    Retrofit retroFit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public RecipeInstructionRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void getRecipeInstruction(RecipeInstructionResponseListener listener, int id) {
        CallRecipeInstruction callRecipeInstruction = retroFit.create(CallRecipeInstruction.class);
        String apiKey = apiKeyManager.getCurrentApiKey();
        Call<ArrayList<RecipeInstructionAPIResponse>> call = callRecipeInstruction.callRecipeInstruction(id, apiKey);

        call.enqueue(new Callback<ArrayList<RecipeInstructionAPIResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<RecipeInstructionAPIResponse>> call, @NonNull Response<ArrayList<RecipeInstructionAPIResponse>> response) {
                if (response.isSuccessful() && response.body() != null){
                    logQuotaUsage(response);
                    listener.didFetchRecipeInstruction(response.body(), response.message());
                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()) {
                        getRecipeInstruction(listener, id);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                }else {
                    listener.didError(response.message());
                    return;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<RecipeInstructionAPIResponse>> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private void logQuotaUsage(Response<ArrayList<RecipeInstructionAPIResponse>> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallRecipeInstruction {
        @GET("recipes/{id}/analyzedInstructions")
        Call<ArrayList<RecipeInstructionAPIResponse>> callRecipeInstruction(
           @Path("id") int id,
           @Query("apiKey") String apiKey
        );
    }
}
