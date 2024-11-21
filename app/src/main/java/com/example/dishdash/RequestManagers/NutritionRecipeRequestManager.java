package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.dishdash.GenerateDailyMealPlannerClass.GenerateMealAPIResponse;
import com.example.dishdash.Listeners.NutritionRecipeResponseListener;
import com.example.dishdash.R;

import java.io.InputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class NutritionRecipeRequestManager {
    Context context;
    APIKeyManager apiKeyManager;

    Retrofit retroFit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public NutritionRecipeRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void getNutritionImage(int recipeId, NutritionRecipeResponseListener listener) {
        CallNutritionImage callNutritionImage = retroFit.create(CallNutritionImage.class);
        String apiKey = apiKeyManager.getCurrentApiKey();
        Call<ResponseBody> call = callNutritionImage.callNutritionImage(recipeId, apiKey);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logQuotaUsage(response);
                    InputStream inputStream = response.body().byteStream();
                    Bitmap nutritionImage = BitmapFactory.decodeStream(inputStream);
                    listener.didFetchRecipeNutritionImage(nutritionImage);
                    getImageUrl(recipeId, listener);
                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()) {
                        getNutritionImage(recipeId, listener);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                } else {
                    listener.didError("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }

    private void getImageUrl(int recipeId, NutritionRecipeResponseListener listener) {
        String apiKey = apiKeyManager.getCurrentApiKey();
        String imageUrl = "https://api.spoonacular.com/recipes/" + recipeId + "/nutritionLabel.png?apiKey=" + apiKey;

        listener.didFetchRecipeNutritionImageUrl(imageUrl);
    }

    private void logQuotaUsage(Response<ResponseBody> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallNutritionImage {
        @GET("recipes/{id}/nutritionLabel.png")
        Call<ResponseBody> callNutritionImage(
                @Path("id") int recipeId,
                @Query("apiKey") String apiKey
        );
    }
}
