package com.example.dishdash.Listeners;

import com.example.dishdash.FooRecognitionClass.FoodRecognitionAPIResponse;

public interface FoodRecognitionResponseListener {
    void didRecognize(FoodRecognitionAPIResponse response, String message);
    void didError(String error);
}
