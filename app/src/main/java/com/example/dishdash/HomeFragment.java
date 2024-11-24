package com.example.dishdash;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.dishdash.Adapters.PersonalRecipeIngredientAdapter;
import com.example.dishdash.Adapters.PersonalRecipeInstructionAdapter;
import com.example.dishdash.Adapters.RandomRecipeAdapter;
import com.example.dishdash.Adapters.SearchRecipeAdapter;
import com.example.dishdash.FooRecognitionClass.FoodRecognitionAPIResponse;
import com.example.dishdash.Listeners.FoodRecognitionCallback;
import com.example.dishdash.Listeners.FoodRecognitionResponseListener;
import com.example.dishdash.Listeners.RandomRecipeResponseListener;
import com.example.dishdash.Listeners.RecipeClickResponseListener;
import com.example.dishdash.Listeners.SearchRecipeResponseListener;
import com.example.dishdash.PersonalRecipeClass.PersonalRecipeClass;
import com.example.dishdash.PersonalRecipeClass.RecipeIngredientClass;
import com.example.dishdash.PersonalRecipeClass.RecipeInstructionClass;
import com.example.dishdash.RandomRecipeClass.RandomRecipeAPIResponse;
import com.example.dishdash.RequestManagers.FoodRecognitionRequestManager;
import com.example.dishdash.RequestManagers.RandomRecipeRequestManager;
import com.example.dishdash.RequestManagers.SearchRecipeRequestManager;
import com.example.dishdash.SearchRecipeClass.SearchRecipeAPIResponse;
import com.example.dishdash.ViewModels.RandomRecipeViewModel;
import com.example.dishdash.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    FragmentHomeBinding binding;
    RandomRecipeAdapter randomRecipeAdapter;
    SearchRecipeAdapter searchRecipeAdapter;
    RandomRecipeRequestManager randomRecipeManager;
    SearchRecipeRequestManager searchRecipeManager;
    FoodRecognitionRequestManager foodRecognitionManager;
    RandomRecipeViewModel randomRecipeViewModel;
    ArrayList<String> tags = new ArrayList<>();

    AlertDialog addRecipeDialog, discardRecipeDialog, confirmRecipeDialog;
    View addRecipeDialogView, discardRecipeDialogView, confirmRecipeDialogView;
    Button addIngredients, addInstructions, addPersonalRecipe, okay, cancel, edit, proceed;
    ImageButton backButton;
    ImageView personalRecipeImage;
    EditText personalRecipeName;
    RecyclerView personalRecipeIngredientsRecyclerView, personalRecipeInstructionRecyclerView;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri selectedPersonalRecipeImage;
    PersonalRecipeIngredientAdapter ingredientAdapter;
    PersonalRecipeInstructionAdapter instructionAdapter;
    boolean isRecipeConfirmed = false;

    String base64PersonalRecipeImage;
    String recipeName;
    List<RecipeIngredientClass> ingredientList = new ArrayList<>();
    List<String> ingredients = new ArrayList<>();
    List<RecipeInstructionClass> instructionList = new ArrayList<>();
    List<String> instructions = new ArrayList<>();
    String recipeId;
    PersonalRecipeClass personalRecipeClass;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    FirebaseDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        InsetsUtil.applyInsets(binding.getRoot());

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null){
                selectedPersonalRecipeImage = result.getData().getData();
                personalRecipeImage.setImageURI(selectedPersonalRecipeImage);
            }
        });

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.homeRandomRecipeRecyclerView.setHasFixedSize(true);
        binding.homeRandomRecipeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        randomRecipeManager = new RandomRecipeRequestManager(getContext());

        randomRecipeViewModel = new ViewModelProvider(requireActivity()).get(RandomRecipeViewModel.class);

        randomRecipeViewModel.getRandomRecipes().observe(getViewLifecycleOwner(), recipes -> {
            if (recipes != null && !recipes.isEmpty()) {
                randomRecipeAdapter = new RandomRecipeAdapter(getContext(), recipes, recipeClickResponseListener);
                binding.homeRandomRecipeRecyclerView.setAdapter(randomRecipeAdapter);
            }
        });

        // Fetch only if data isn't loaded yet
        if (!randomRecipeViewModel.isDataLoaded()) {
            getRandomRecipes();
        }

        binding.addRecipe.setOnClickListener(addRecipe -> {
            if (!isRecipeConfirmed) {
                addRecipeDialogView = getLayoutInflater().inflate(R.layout.add_personal_recipe_dialog, null);
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                builder.setView(addRecipeDialogView);
                addRecipeDialog = builder.create();
                addRecipeDialog.show();

                addRecipeDialog.setOnDismissListener(dialog -> {
                    discardRecipeDialogView = getLayoutInflater().inflate(R.layout.discard_add_recipe, null);
                    android.app.AlertDialog.Builder discardRecipeBuilder = new android.app.AlertDialog.Builder(getContext());
                    discardRecipeBuilder.setView(discardRecipeDialogView);
                    discardRecipeDialog = discardRecipeBuilder.create();
                    discardRecipeDialog.show();

                    okay = discardRecipeDialogView.findViewById(R.id.okay);
                    cancel = discardRecipeDialogView.findViewById(R.id.cancel);

                    okay.setOnClickListener(v -> {
                        ingredientList.clear();
                        instructionList.clear();

                        ingredientAdapter.notifyDataSetChanged();
                        instructionAdapter.notifyDataSetChanged();

                        personalRecipeName.setText("");
                        selectedPersonalRecipeImage = null;

                        if (discardRecipeDialog.isShowing()) {
                            discardRecipeDialog.dismiss();
                        }

                        if (addRecipeDialog.isShowing()) {
                            addRecipeDialog.dismiss();
                        }
                    });

                    cancel.setOnClickListener(v -> {
                        discardRecipeDialog.dismiss();
                        addRecipeDialog.show();
                    });
                });
            }

            // back button
            backButton = addRecipeDialogView.findViewById(R.id.backButton);

            backButton.setOnClickListener(back -> {
                addRecipeDialog.dismiss();
            });

            // add recipe image
            personalRecipeImage = addRecipeDialogView.findViewById(R.id.personalRecipeImage);

            personalRecipeImage.setImageResource(R.drawable.recipe_place_holder);

            personalRecipeImage.setOnClickListener(recipeImage -> {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                imagePickerLauncher.launch(intent);
            });

            // add ingredients
            addIngredients = addRecipeDialogView.findViewById(R.id.personalRecipeAddIngredients);
            personalRecipeIngredientsRecyclerView = addRecipeDialogView.findViewById(R.id.personalRecipeIngredientsRecyclerView);
            personalRecipeIngredientsRecyclerView.setHasFixedSize(true);
            personalRecipeIngredientsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            ingredientAdapter = new PersonalRecipeIngredientAdapter(getContext(), ingredientList);
            personalRecipeIngredientsRecyclerView.setAdapter(ingredientAdapter);

            addIngredients.setOnClickListener(addIngredients ->{
                ingredientList.add(new RecipeIngredientClass(""));
                ingredientAdapter.notifyDataSetChanged();
            });

            // add instructions
            addInstructions = addRecipeDialogView.findViewById(R.id.personalRecipeAddInstructions);
            personalRecipeInstructionRecyclerView = addRecipeDialogView.findViewById(R.id.personalRecipeInstructionRecyclerView);
            personalRecipeInstructionRecyclerView.setHasFixedSize(true);
            personalRecipeInstructionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            instructionAdapter = new PersonalRecipeInstructionAdapter(getContext(), instructionList);
            personalRecipeInstructionRecyclerView.setAdapter(instructionAdapter);

            addInstructions.setOnClickListener(addInstructions -> {
                instructionList.add(new RecipeInstructionClass(""));
                instructionAdapter.notifyDataSetChanged();
            });

            // add recipe
            addPersonalRecipe = addRecipeDialogView.findViewById(R.id.addPersonalRecipe);
            addPersonalRecipe.setText("Add Recipe");

            personalRecipeName = addRecipeDialogView.findViewById(R.id.personalRecipeName);

            addPersonalRecipe.setOnClickListener(addPersonalRecipe -> {
                if (selectedPersonalRecipeImage == null) {
                    Toast.makeText(getContext(), "Please add an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                recognizeImage(selectedPersonalRecipeImage, (isFood, message) -> {
                    if (!isFood) {
                        Toast.makeText(getContext(), "The uploaded image does not appear to be food. Please upload a valid food image.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (personalRecipeName.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getContext(), "Please add a recipe name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (ingredientList.isEmpty()) {
                        Toast.makeText(getContext(), "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (instructionList.isEmpty()) {
                        Toast.makeText(getContext(), "Please add at least one instruction", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (RecipeIngredientClass ingredient : ingredientList) {
                        if (ingredient.getIngredient().trim().isEmpty()) {
                            Toast.makeText(getContext(), "Please fill all ingredient fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    for (RecipeInstructionClass instruction : instructionList) {
                        if (instruction.getInstruction().trim().isEmpty()) {
                            Toast.makeText(getContext(), "Please fill all instruction fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    confirmRecipeDialogView = getLayoutInflater().inflate(R.layout.add_recipe_confirmation, null);

                    if (confirmRecipeDialogView.getParent() != null) {
                        ((ViewGroup) confirmRecipeDialogView.getParent()).removeView(confirmRecipeDialogView);
                    }

                    android.app.AlertDialog.Builder confirmRecipeBuilder = new android.app.AlertDialog.Builder(getContext());
                    confirmRecipeBuilder.setView(confirmRecipeDialogView);
                    confirmRecipeDialog = confirmRecipeBuilder.create();
                    confirmRecipeDialog.show();

                    edit = confirmRecipeDialogView.findViewById(R.id.edit);
                    proceed = confirmRecipeDialogView.findViewById(R.id.procced);

                    edit.setOnClickListener(edit -> {
                        confirmRecipeDialog.dismiss();
                        addRecipeDialog.show();
                    });

                    proceed.setOnClickListener(proceed -> {
                        try {
                            Bitmap personalRecipeImageBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedPersonalRecipeImage);
                            base64PersonalRecipeImage = encodeImageToBase64(personalRecipeImageBitmap);
                            recipeName = personalRecipeName.getText().toString().trim();

                            for (RecipeIngredientClass ingredient : ingredientList) {
                                ingredients.add(ingredient.getIngredient());
                            }

                            for (RecipeInstructionClass instruction : instructionList) {
                                instructions.add(instruction.getInstruction());
                            }

                            // Initialize Firebase
                            db = FirebaseDatabase.getInstance();
                            auth = FirebaseAuth.getInstance();
                            user = auth.getCurrentUser();
                            databaseReference = db.getReference("Users").child(user.getUid()).child("Personal Recipes");

                            recipeId = databaseReference.push().getKey();

                            personalRecipeClass = new PersonalRecipeClass(recipeId, base64PersonalRecipeImage, recipeName, ingredients, instructions);

                            databaseReference.child(recipeId).setValue(personalRecipeClass).addOnCompleteListener(task -> {
                                ingredientList.clear();
                                instructionList.clear();
                                ingredientAdapter.notifyDataSetChanged();
                                instructionAdapter.notifyDataSetChanged();
                                personalRecipeName.setText("");
                                selectedPersonalRecipeImage = null;

                                addRecipeDialog.setOnDismissListener(null);

                                if (confirmRecipeDialog.isShowing()) {
                                    confirmRecipeDialog.dismiss();
                                }

                                if (addRecipeDialog.isShowing()) {
                                    addRecipeDialog.dismiss();
                                }

                                Toast.makeText(getContext(), "Recipe added successfully", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to add recipe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                });
            });
        });

        setupFilters();
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void recognizeImage(Uri imageUri, FoodRecognitionCallback callback){
        try {
            // Convert Uri to a File
            File imageFile = new File(getPathFromUri(imageUri));

            foodRecognitionManager = new FoodRecognitionRequestManager(getContext());

            foodRecognitionManager.getFoodRecognition(new FoodRecognitionResponseListener() {
                @Override
                public void didRecognize(FoodRecognitionAPIResponse response, String message) {
                    double threshold = 0.5;
                    Log.d("FoodRecognition", "API Response Probability: " + response.getProbability());
                    boolean isFood = response.getCategory() != null && response.getProbability() >= threshold;
                    callback.onResult(isFood, "Image classification completed.");
                }

                @Override
                public void didError(String error) {
                    Toast.makeText(getContext(), "Error during image classification: " + error, Toast.LENGTH_SHORT).show();
                    callback.onResult(false, error);
                }
            }, imageFile);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error processing the image file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            callback.onResult(false, e.getMessage());
        }
    }

    private String getPathFromUri(Uri uri) {
        String path = null;
        Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            if (index >= 0) {
                path = cursor.getString(index);
            } else {
                int docIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                String docId = cursor.getString(docIdIndex);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(docId));
                path = contentUri.getPath();
            }
            cursor.close();
        }
        return path;
    }

    private void setupFilters() {
        binding.radioMealTypes.setOnCheckedChangeListener((group, checkedId) -> {
            tags.clear();
            String selectedMealType = getTagForMealType(group.findViewById(checkedId).getId());
            if (selectedMealType != null) {
                tags.add(selectedMealType);
            }
            getRandomRecipes();
        });

        binding.radioDietTypes.setOnCheckedChangeListener((group, checkedId) -> {
            tags.clear();
            String selectedDietType = getTagForDietType(group.findViewById(checkedId).getId());
            if (selectedDietType != null) {
                tags.add(selectedDietType);
            }
            getRandomRecipes();
        });

        binding.radioCuisineTypes.setOnCheckedChangeListener((group, checkedId) -> {
            tags.clear();
            String selectedCuisineType = getTagForCuisineType(group.findViewById(checkedId).getId());
            if (selectedCuisineType != null) {
                tags.add(selectedCuisineType);
            }
            getRandomRecipes();
        });

        binding.homeSearchByIngredients.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tags.clear();
                searchRecipeManager = new SearchRecipeRequestManager(getContext());
                getSearchRecipes(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private String getTagForMealType(int id) {
        if (id == R.id.radioMainCourse) return "main course";
        else if (id == R.id.radioSideDish) return "side dish";
        else if (id == R.id.radioDessert) return "dessert";
        else if (id == R.id.radioAppetizer) return "appetizer";
        else if (id == R.id.radioSalad) return "salad";
        else if (id == R.id.radioBread) return "bread";
        else if (id == R.id.radioBreakfast) return "breakfast";
        else if (id == R.id.radioSoup) return "soup";
        else if (id == R.id.radioBeverage) return "beverage";
        else if (id == R.id.radioSauce) return "sauce";
        else if (id == R.id.radioMarinade) return "marinade";
        else if (id == R.id.radioFingerFood) return "fingerfood";
        else if (id == R.id.radioSnack) return "snack";
        else if (id == R.id.radioDrink) return "drink";
        else if (id == R.id.radioAnyMeal) return "";
        else return null;
    }

    private String getTagForDietType(int id) {
        if (id == R.id.radioGlutenFree) return "gluten-free";
        else if (id == R.id.radioKetogenic) return "ketogenic";
        else if (id == R.id.radioVegetarian) return "vegetarian";
        else if (id == R.id.radioLactoVegetarian) return "lacto-vegetarian";
        else if (id == R.id.radioOvoVegetarian) return "ovo-vegetarian";
        else if (id == R.id.radioVegan) return "vegan";
        else if (id == R.id.radioPescetarian) return "pescetarian";
        else if (id == R.id.radioPalio) return "palio";
        else if (id == R.id.radioLowFodmap) return "low fodmap";
        else if (id == R.id.radioWhole30) return "whole30";
        else if (id == R.id.radioAnyDiet) return "";
        else return null;
    }

    private String getTagForCuisineType(int id) {
        if (id == R.id.radioAfrican) return "african";
        else if (id == R.id.radioAsian) return "asian";
        else if (id == R.id.radioAmerican) return "american";
        else if (id == R.id.radioBritish) return "british";
        else if (id == R.id.radioCajun) return "cajun";
        else if (id == R.id.radioCaribbean) return "caribbean";
        else if (id == R.id.radioChinese) return "chinese";
        else if (id == R.id.radioEasternEuropean) return "eastern european";
        else if (id == R.id.radioEuropean) return "european";
        else if (id == R.id.radioFrench) return "french";
        else if (id == R.id.radioGerman) return "german";
        else if (id == R.id.radioGreek) return "greek";
        else if (id == R.id.radioIndian) return "indian";
        else if (id == R.id.radioIrish) return "irish";
        else if (id == R.id.radioItalian) return "italian";
        else if (id == R.id.radioJapanese) return "japanese";
        else if (id == R.id.radioJewish) return "jewish";
        else if (id == R.id.radioKorean) return "korean";
        else if (id == R.id.radioLatinAmerican) return "latin american";
        else if (id == R.id.radioMediterranean) return "mediterranean";
        else if (id == R.id.radioMexican) return "mexican";
        else if (id == R.id.radioMiddleEastern) return "middle eastern";
        else if (id == R.id.radioNordic) return "nordic";
        else if (id == R.id.radioSouthern) return "southern";
        else if (id == R.id.radioSpanish) return "spanish";
        else if (id == R.id.radioThai) return "thai";
        else if (id == R.id.radioVietnamese) return "vietnamese";
        else if (id == R.id.radioAnyCuisine) return "";
        else return null;
    }

    private void getRandomRecipes() {
        String tagsString = String.join(",", tags);

        if (!isNetworkAvailable()) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        if (randomRecipeViewModel.shouldFetch(tagsString)) {
            randomRecipeManager.getRandomRecipes(new RandomRecipeResponseListener() {
                @Override
                public void didFetchRandomRecipe(RandomRecipeAPIResponse response, String message) {
                    if (response != null && response.recipes != null) {
                        randomRecipeViewModel.setRandomRecipes(response.recipes);  // Save data to ViewModel
                    }
                }

                @Override
                public void didError(String message) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("RandomRecipe", "Cannot show Toast. Fragment is not attached. Error: " + message);
                    }
                }
            }, tagsString);
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    private void getSearchRecipes(String ingredients) {
        searchRecipeManager.getSearchRecipes(new SearchRecipeResponseListener() {
            @Override
            public void didFetchSearchRecipes(ArrayList<SearchRecipeAPIResponse> response, String message) {
                if(response != null && !response.isEmpty()){
                    searchRecipeAdapter = new SearchRecipeAdapter(getContext(), response, recipeClickResponseListener);
                    binding.homeRandomRecipeRecyclerView.setAdapter(searchRecipeAdapter);
                }
            }

            @Override
            public void didError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        }, ingredients);
    }

    private final RecipeClickResponseListener recipeClickResponseListener = new RecipeClickResponseListener() {
        @Override
        public void onRecipeClick(String recipeId) {
            DetailedRecipeDialog detailedRecipeDialog = DetailedRecipeDialog.newInstance(Integer.parseInt(recipeId));
            detailedRecipeDialog.show(getParentFragmentManager(), "DetailedRecipeDialog");
        }
    };
}