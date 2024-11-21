package com.example.dishdash;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.example.dishdash.Adapters.RecipeInstructionAdapter;
import com.example.dishdash.DetailedRecipeClass.DetailedRecipeAPIResponse;
import com.example.dishdash.DetailedRecipeClass.ExtendedIngredient;
import com.example.dishdash.FavoriteRecipeClass.FavoriteRecipeClass;
import com.example.dishdash.Listeners.DetailedRecipeResponseListener;
import com.example.dishdash.Listeners.NutritionRecipeResponseListener;
import com.example.dishdash.Listeners.RecipeInstructionResponseListener;
import com.example.dishdash.RecipeInstructionClass.RecipeInstructionAPIResponse;
import com.example.dishdash.RequestManagers.DetailedRecipeRequestManager;
import com.example.dishdash.RequestManagers.NutritionRecipeRequestManager;
import com.example.dishdash.RequestManagers.RecipeInstructionRequestManager;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class DetailedRecipeDialog extends DialogFragment {
    private static final String ARG_RECIPE_ID = "recipeId";
    String imageURL;
    ImageView recipeImage, nutritionImage;
    RecyclerView instructionContainerRecyclerView;
    TextView recipeTitle, readyIn, preparationTime, cookingTime, servings, ingredientsTextView;
    Button addToFavButton;
    ImageView backButton;
    View view;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference favoriteReference;

    ScrollView mainDetailedRecipe;
    ProgressBar progressBar;

    TextView detailedRecipeHeader;

    public static DetailedRecipeDialog newInstance(int recipeId) {
        DetailedRecipeDialog fragment = new DetailedRecipeDialog();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, String.valueOf(recipeId));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            window.setAttributes(layoutParams);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.detailed_recipe, container, false);

        recipeImage = view.findViewById(R.id.detailedRecipeImage);
        recipeTitle = view.findViewById(R.id.detailedRecipeTitle);
        readyIn = view.findViewById(R.id.detailedRecipeReadyIn);
        servings = view.findViewById(R.id.detailedRecipeServing);
        ingredientsTextView = view.findViewById(R.id.ingredients);
        addToFavButton = view.findViewById(R.id.detailedRecipeAddToFav);
        backButton = view.findViewById(R.id.backButton);
        instructionContainerRecyclerView = view.findViewById(R.id.instructionContainerRecyclerView);
        nutritionImage = view.findViewById(R.id.detailedRecipeNutritionImage);

        mainDetailedRecipe = view.findViewById(R.id.main_detailed_recipe);
        progressBar = view.findViewById(R.id.progressBar);
        detailedRecipeHeader = view.findViewById(R.id.detailedRecipeHeader);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();

        assert getArguments() != null;
        String recipeId = getArguments().getString(ARG_RECIPE_ID);

        assert recipeId != null;

        checkIfRecipeIsInFavorites(Integer.parseInt(recipeId));

        mainDetailedRecipe.setVisibility(View.GONE);
        detailedRecipeHeader.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        fetchRecipeDetails(Integer.parseInt(recipeId));

        fetchRecipeInstruction(Integer.parseInt(recipeId));

        fetchRecipeNutritionImage(Integer.parseInt(recipeId));

        addToFavButton.setOnClickListener(v -> {
            addToFavourites(Integer.parseInt(recipeId));
        });

        backButton.setOnClickListener(v -> {
            dismiss();
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Glide.with(this).clear(nutritionImage);
    }

    private void fetchRecipeDetails(int recipeId) {
        DetailedRecipeRequestManager requestManager = new DetailedRecipeRequestManager(getContext());
        requestManager.getDetailedRecipe(detailedRecipeResponseListener, recipeId);
    }

    private final DetailedRecipeResponseListener detailedRecipeResponseListener = new DetailedRecipeResponseListener() {
        @SuppressLint("SetTextI18n")
        @Override
        public void didFetchDetailedRecipe(DetailedRecipeAPIResponse detailedRecipe, String message) {
            if (isAdded() && getDialog() != null) {
                Glide.with(requireContext()).load(detailedRecipe.image).into(recipeImage);
                imageURL = detailedRecipe.image;
                recipeTitle.setText(detailedRecipe.title);
                readyIn.setText(detailedRecipe.readyInMinutes + " minutes");
                servings.setText(detailedRecipe.servings + " servings");

                StringBuilder ingredients = new StringBuilder();
                for (ExtendedIngredient ingredient : detailedRecipe.extendedIngredients) {
                    ingredients.append("• ").append(ingredient.original).append("\n");
                }
                ingredientsTextView.setText(ingredients.toString().trim());

                mainDetailedRecipe.setVisibility(View.VISIBLE);
                detailedRecipeHeader.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void didError(String message) {

        }
    };

    private void fetchRecipeInstruction(int recipeId) {
        RecipeInstructionRequestManager requestManager = new RecipeInstructionRequestManager(getContext());
        requestManager.getRecipeInstruction(recipeInstructionResponseListener, recipeId);
    }

    private final RecipeInstructionResponseListener recipeInstructionResponseListener = new RecipeInstructionResponseListener() {
        @Override
        public void didFetchRecipeInstruction(ArrayList<RecipeInstructionAPIResponse> recipeInstruction, String message) {
            instructionContainerRecyclerView.setHasFixedSize(true);
            instructionContainerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
            RecipeInstructionAdapter adapter = new RecipeInstructionAdapter(getContext(), recipeInstruction);
            instructionContainerRecyclerView.setAdapter(adapter);
        }

        @Override
        public void didError(String message) {

        }
    };

    private void fetchRecipeNutritionImage(int recipeId) {
        NutritionRecipeRequestManager requestManager = new NutritionRecipeRequestManager(getContext());

        requestManager.getNutritionImage(recipeId, new NutritionRecipeResponseListener() {

            @Override
            public void didFetchRecipeNutritionImage(Bitmap nutritionImage) {
                if (isAdded() && getDialog() != null) {
                    DetailedRecipeDialog.this.nutritionImage.setImageBitmap(nutritionImage);
                }
            }

            @Override
            public void didFetchRecipeNutritionImageUrl(String imageUrl) {
                /*if (isAdded() && getDialog() != null) {
                    Glide.with(requireContext())
                            .load(imageUrl)
                            .into(DetailedRecipeDialog.this.nutritionImage);
                }*/
            }

            @Override
            public void didError(String message) {
                Log.e("Error", message);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void addToFavourites(int recipeId) {
        if (user != null) {
            favoriteReference = db.getReference("Users").child(user.getUid()).child("Favorites").child(String.valueOf(recipeId));

            favoriteReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()){
                    addToFavButton.setVisibility(View.GONE);
                } else {
                    FavoriteRecipeClass favoriteRecipe = new FavoriteRecipeClass(
                            recipeTitle.getText().toString(),
                            imageURL,
                            readyIn.getText().toString(),
                            servings.getText().toString(),
                            recipeId
                    );

                    favoriteReference.setValue(favoriteRecipe).addOnCompleteListener(addTask -> {
                        if (addTask.isSuccessful()){
                            addToFavButton.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Recipe added to favorites", Toast.LENGTH_SHORT).show();
                            imageURL = null; // Reset image URL
                        } else {
                            Toast.makeText(getContext(), "Failed to add recipe to favorites", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to check if recipe is in favorites", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void checkIfRecipeIsInFavorites(int recipeId) {
        if (user != null) {
            favoriteReference = db.getReference("Users").child(user.getUid()).child("Favorites").child(String.valueOf(recipeId));

            favoriteReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    addToFavButton.setVisibility(View.GONE);
                } else {
                    addToFavButton.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to check favorites", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
