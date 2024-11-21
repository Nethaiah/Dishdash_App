package com.example.dishdash.Listeners;

import com.example.dishdash.SearchRecipeClass.SearchRecipeAPIResponse;

import java.util.ArrayList;

public interface SearchRecipeResponseListener {
    void didFetchSearchRecipes(ArrayList<SearchRecipeAPIResponse> response, String message);
    void didError(String message);
}
