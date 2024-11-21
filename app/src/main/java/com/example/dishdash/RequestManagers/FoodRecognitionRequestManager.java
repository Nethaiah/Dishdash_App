package com.example.dishdash.RequestManagers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dishdash.FooRecognitionClass.FoodRecognitionAPIResponse;
import com.example.dishdash.Listeners.FoodRecognitionResponseListener;

import java.io.File;
import java.security.AccessControlContext;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class FoodRecognitionRequestManager {
    Context context;
    APIKeyManager apiKeyManager;;

    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.spoonacular.com/").addConverterFactory(GsonConverterFactory.create()).build();

    public FoodRecognitionRequestManager(Context context) {
        this.context = context;
        this.apiKeyManager = new APIKeyManager(context);
    }

    public void getFoodRecognition(FoodRecognitionResponseListener listener, File imageFile) {
        CallFoodRecognition callFoodRecognition = retrofit.create(CallFoodRecognition.class);
        String apiKey = apiKeyManager.getCurrentApiKey();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestFile);

        Call<FoodRecognitionAPIResponse> call = callFoodRecognition.classifyFoodImage(filePart, apiKey);

        call.enqueue(new retrofit2.Callback<FoodRecognitionAPIResponse>() {
            @Override
            public void onResponse(@NonNull Call<FoodRecognitionAPIResponse> call, @NonNull Response<FoodRecognitionAPIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    logQuotaUsage(response);
                    listener.didRecognize(response.body(), response.message());
                } else if (response.code() == 402 || response.code() == 429) {
                    if (apiKeyManager.switchToNextKey()) {
                        getFoodRecognition(listener, imageFile);
                    } else {
                        listener.didError("All API keys have reached their daily quotas.");
                    }
                } else {
                    listener.didError(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodRecognitionAPIResponse> call, @NonNull Throwable throwable) {
                listener.didError(throwable.getMessage());
            }
        });
    }
    private void logQuotaUsage(Response<FoodRecognitionAPIResponse> response) {
        String quotaRequest = response.headers().get("X-API-Quota-Request");
        String quotaUsed = response.headers().get("X-API-Quota-Used");
        String quotaLeft = response.headers().get("X-API-Quota-Left");

        Log.d("APIQuota", "Request Points Used: " + quotaRequest);
        Log.d("APIQuota", "Total Points Used Today: " + quotaUsed);
        Log.d("APIQuota", "Points Left Today: " + quotaLeft);
    }

    private interface CallFoodRecognition{
        @Multipart
        @POST("food/images/classify")
        Call<FoodRecognitionAPIResponse> classifyFoodImage(
                @Part MultipartBody.Part file,
                @Query("apiKey") String apiKey
        );
    }
}
