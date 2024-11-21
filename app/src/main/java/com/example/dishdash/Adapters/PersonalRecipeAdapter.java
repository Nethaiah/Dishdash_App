package com.example.dishdash.Adapters;

import static android.app.Activity.RESULT_OK;

import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;

import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishdash.DetailedRecipeClass.DetailedRecipeAPIResponse;
import com.example.dishdash.FooRecognitionClass.FoodRecognitionAPIResponse;
import com.example.dishdash.Listeners.FoodRecognitionCallback;
import com.example.dishdash.Listeners.FoodRecognitionResponseListener;
import com.example.dishdash.Listeners.PersonalRecipeSelectImageCallback;
import com.example.dishdash.PersonalRecipeClass.PersonalRecipeClass;
import com.example.dishdash.PersonalRecipeClass.RecipeIngredientClass;
import com.example.dishdash.PersonalRecipeClass.RecipeInstructionClass;
import com.example.dishdash.R;
import com.example.dishdash.RequestManagers.FoodRecognitionRequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PersonalRecipeAdapter extends RecyclerView.Adapter<PersonalRecipeViewHolder>{
    DatabaseReference personalRecipeReference;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser user;
    String personalRecipeId;
    int currentPosition;

    PersonalRecipeIngredientAdapter ingredientAdapter;
    PersonalRecipeInstructionAdapter instructionAdapter;
    Button addIngredients, addInstructions, savePersonalRecipe, edit, proceed;
    ImageButton back;
    ImageView personalRecipeImage;
    RecyclerView personalRecipeIngredientsRecyclerView, personalRecipeInstructionRecyclerView;
    Uri selectedPersonalRecipeImage;
    EditText personalRecipeName;
    List<RecipeIngredientClass> ingredientList = new ArrayList<>();
    List<String> ingredients = new ArrayList<>();
    List<RecipeInstructionClass> instructionList = new ArrayList<>();
    List<String> instructions = new ArrayList<>();
    String base64PersonalRecipeImage;
    String recipeName;

    View editDialogView, detailedView, confirmRecipeDialogView;
    AlertDialog editDialog, personalDetailedRecipeDialog, confirmRecipeDialog;

    Context context;
    List<PersonalRecipeClass> personalRecipeList;
    PersonalRecipeSelectImageCallback adapterCallback;

    public PersonalRecipeAdapter(Context context, List<PersonalRecipeClass> personalRecipeList, PersonalRecipeSelectImageCallback adapterCallback) {
        this.context = context;
        this.personalRecipeList = personalRecipeList;
        this.adapterCallback = adapterCallback;
    }

    @NonNull
    @Override
    public PersonalRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PersonalRecipeViewHolder(LayoutInflater.from(context).inflate(R.layout.list_personal_recipe, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull PersonalRecipeViewHolder holder, int position) {
        PersonalRecipeClass personalRecipeClass = personalRecipeList.get(position);

        byte[] decodedString = Base64.decode(personalRecipeClass.getImage(), Base64.DEFAULT);
        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.personalRecipeImage.setImageBitmap(decodedBitmap);

        holder.personalRecipeTitle.setText(personalRecipeClass.getRecipeName());

        holder.personalRecipeCardView.setOnClickListener(detailedRecipe -> {
            detailedView = LayoutInflater.from(context).inflate(R.layout.detailed_personal_recipe, null);
            AlertDialog.Builder detailedPersonalRecipeBuilder = new AlertDialog.Builder(context);
            detailedPersonalRecipeBuilder.setView(detailedView);
            personalDetailedRecipeDialog = detailedPersonalRecipeBuilder.create();
            personalDetailedRecipeDialog.show();

            ImageView recipeImage = detailedView.findViewById(R.id.personalRecipeImage);
            TextView recipeTitle = detailedView.findViewById(R.id.personalRecipeTitle);
            TextView recipeIngredients = detailedView.findViewById(R.id.personalRecipeIngredients);
            TextView recipeInstructions = detailedView.findViewById(R.id.personalRecipeInstructions);
            ImageButton backButton = detailedView.findViewById(R.id.backButton);
            Button editButton = detailedView.findViewById(R.id.personalRecipeEdit);

            recipeImage.setImageBitmap(decodedBitmap);
            recipeTitle.setText(personalRecipeClass.getRecipeName());

            StringBuilder ingredientsBuilder = new StringBuilder();
            if (personalRecipeClass.getIngredients() != null) {
                for (String ingredient : personalRecipeClass.getIngredients()) {
                    ingredientsBuilder.append("• ").append(ingredient).append("\n");
                }
            }
            recipeIngredients.setText(ingredientsBuilder.toString().trim());

            StringBuilder instructionsBuilder = new StringBuilder();
            int stepNumber = 1;
            if (personalRecipeClass.getInstructions() != null) {
                for (String instruction : personalRecipeClass.getInstructions()) {
                    instructionsBuilder.append(stepNumber++).append(". ").append(instruction).append("\n");
                }
            }
            recipeInstructions.setText(instructionsBuilder.toString().trim());

            backButton.setOnClickListener(view -> personalDetailedRecipeDialog.dismiss());

            editButton.setOnClickListener(editRecipe -> {
                if (personalDetailedRecipeDialog.isShowing()) {
                    personalDetailedRecipeDialog.dismiss();
                }

                editDialogView = LayoutInflater.from(context).inflate(R.layout.add_personal_recipe_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(editDialogView);
                editDialog = builder.create();
                editDialog.show();

                back = editDialogView.findViewById(R.id.backButton);
                personalRecipeImage = editDialogView.findViewById(R.id.personalRecipeImage);
                personalRecipeName = editDialogView.findViewById(R.id.personalRecipeName);
                personalRecipeIngredientsRecyclerView = editDialogView.findViewById(R.id.personalRecipeIngredientsRecyclerView);
                personalRecipeInstructionRecyclerView = editDialogView.findViewById(R.id.personalRecipeInstructionRecyclerView);
                addIngredients = editDialogView.findViewById(R.id.personalRecipeAddIngredients);
                addInstructions = editDialogView.findViewById(R.id.personalRecipeAddInstructions);
                savePersonalRecipe = editDialogView.findViewById(R.id.addPersonalRecipe);

                back.setOnClickListener(view -> {
                    editDialog.dismiss();
                    personalDetailedRecipeDialog.show();
                });

                savePersonalRecipe.setText("Save Changes");

                personalRecipeImage.setImageBitmap(decodedBitmap);
                personalRecipeName.setText(personalRecipeClass.getRecipeName());

                personalRecipeIngredientsRecyclerView.setHasFixedSize(true);
                personalRecipeIngredientsRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                if (personalRecipeClass.getIngredients() != null) {
                    ingredientList.clear();

                    ingredientAdapter = new PersonalRecipeIngredientAdapter(context, ingredientList);
                    personalRecipeIngredientsRecyclerView.setAdapter(ingredientAdapter);

                    for (String ingredient : personalRecipeClass.getIngredients()) {
                        ingredientList.add(new RecipeIngredientClass(ingredient));
                    }
                    ingredientAdapter.notifyDataSetChanged();
                }

                personalRecipeInstructionRecyclerView.setHasFixedSize(true);
                personalRecipeInstructionRecyclerView.setLayoutManager(new LinearLayoutManager(context));

                if (personalRecipeClass.getInstructions() != null) {
                    instructionList.clear();

                    instructionAdapter = new PersonalRecipeInstructionAdapter(context, instructionList);
                    personalRecipeInstructionRecyclerView.setAdapter(instructionAdapter);

                    for (String instruction : personalRecipeClass.getInstructions()) {
                        instructionList.add(new RecipeInstructionClass(instruction));
                    }
                    instructionAdapter.notifyDataSetChanged();
                }

                addIngredients.setOnClickListener(view -> {
                    ingredientList.add(new RecipeIngredientClass(""));
                    ingredientAdapter.notifyItemInserted(ingredientList.size() - 1);
                });

                addInstructions.setOnClickListener(view -> {
                    instructionList.add(new RecipeInstructionClass(""));
                    instructionAdapter.notifyItemInserted(instructionList.size() - 1);
                });

                personalRecipeImage.setOnClickListener(newImage -> {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    adapterCallback.onImageSelected(selectedPersonalRecipeImage);
                });

                savePersonalRecipe.setOnClickListener(save -> {
                    if (selectedPersonalRecipeImage == null && personalRecipeClass.getImage().isEmpty()) {
                        Toast.makeText(context, "Please add an image", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (personalRecipeName.getText().toString().trim().isEmpty()) {
                        Toast.makeText(context, "Please add a recipe name", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (ingredientList.isEmpty()) {
                        Toast.makeText(context, "Please add at least one ingredient", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (instructionList.isEmpty()) {
                        Toast.makeText(context, "Please add at least one instruction", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (RecipeIngredientClass ingredient : ingredientList) {
                        if (ingredient.getIngredient().trim().isEmpty()) {
                            Toast.makeText(context, "Please fill all ingredient fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    for (RecipeInstructionClass instruction : instructionList) {
                        if (instruction.getInstruction().trim().isEmpty()) {
                            Toast.makeText(context, "Please fill all instruction fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    confirmRecipeDialogView = LayoutInflater.from(context).inflate(R.layout.add_recipe_confirmation, null);

                    if (confirmRecipeDialogView.getParent() != null) {
                        ((ViewGroup) confirmRecipeDialogView.getParent()).removeView(confirmRecipeDialogView);
                    }

                    android.app.AlertDialog.Builder confirmRecipeBuilder = new android.app.AlertDialog.Builder(context);
                    confirmRecipeBuilder.setView(confirmRecipeDialogView);
                    confirmRecipeDialog = confirmRecipeBuilder.create();
                    confirmRecipeDialog.show();

                    edit = confirmRecipeDialogView.findViewById(R.id.edit);
                    proceed = confirmRecipeDialogView.findViewById(R.id.procced);

                    edit.setOnClickListener(edit -> {
                        confirmRecipeDialog.dismiss();
                        editDialog.show();
                    });

                    proceed.setOnClickListener(proceed -> {
                        try {
                            Bitmap personalRecipeImageBitmap;

                            if (selectedPersonalRecipeImage != null) {
                                personalRecipeImageBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedPersonalRecipeImage);
                            } else if (!personalRecipeClass.getImage().isEmpty()) {
                                byte[] decodedBase64String = Base64.decode(personalRecipeClass.getImage(), Base64.DEFAULT);
                                personalRecipeImageBitmap = BitmapFactory.decodeByteArray(decodedBase64String, 0, decodedBase64String.length);
                            } else {
                                Toast.makeText(context, "Please add an image", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            base64PersonalRecipeImage = encodeImageToBase64(personalRecipeImageBitmap);

                            recipeName = personalRecipeName.getText().toString().trim();

                            personalRecipeId = String.valueOf(personalRecipeClass.getId());

                            ingredients.clear();
                            for (RecipeIngredientClass ingredient : ingredientList) {
                                ingredients.add(ingredient.getIngredient());
                            }

                            instructions.clear();
                            for (RecipeInstructionClass instruction : instructionList) {
                                instructions.add(instruction.getInstruction());
                            }

                            db = FirebaseDatabase.getInstance();
                            auth = FirebaseAuth.getInstance();
                            user = auth.getCurrentUser();
                            personalRecipeReference = db.getReference("Users").child(user.getUid()).child("Personal Recipes");

                            PersonalRecipeClass updatedRecipe = new PersonalRecipeClass(
                                    personalRecipeId,
                                    base64PersonalRecipeImage,
                                    recipeName,
                                    ingredients,
                                    instructions
                            );

                            Log.d("PersonalRecipeAdapter", "Updating recipe with ID: " + personalRecipeId);
                            personalRecipeReference.child(personalRecipeId).setValue(updatedRecipe)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("PersonalRecipeAdapter", "Recipe updated successfully");
                                            Toast.makeText(context, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                                            cleanupAfterUpdate();
                                        } else {
                                            Log.e("PersonalRecipeAdapter", "Failed to update recipe: " + task.getException());
                                        }
                                    });

                        } catch (IOException e) {
                            Log.e("PersonalRecipeAdapter", "Error: " + e.getMessage(), e);
                            e.printStackTrace();
                        }
                    });

                });
            });
        });

        holder.deletePersonalRecipe.setOnClickListener(v -> {
            db = FirebaseDatabase.getInstance();
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();

            View personalRecipeDialogView = LayoutInflater.from(context).inflate(R.layout.delete_from_personal_recipe, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(personalRecipeDialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            Button cancelButton = personalRecipeDialogView.findViewById(R.id.cancel);
            Button deleteButton = personalRecipeDialogView.findViewById(R.id.delete);

            cancelButton.setOnClickListener(cancel -> dialog.dismiss());

            deleteButton.setOnClickListener(delete -> {
                currentPosition = holder.getAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < personalRecipeList.size()){
                    personalRecipeList.remove(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, personalRecipeList.size());

                    personalRecipeId = String.valueOf(personalRecipeClass.getId());
                    personalRecipeReference = db.getReference("Users").child(user.getUid()).child("Personal Recipes").child(personalRecipeId);

                    personalRecipeReference.removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Recipe removed from favorites", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to remove recipe from favorites", Toast.LENGTH_SHORT).show();
                    });

                    dialog.dismiss();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return personalRecipeList.size();
    }

    public void updateRecipes(List<PersonalRecipeClass> newRecipes) {
        this.personalRecipeList = newRecipes;
        notifyDataSetChanged();
    }

    // Method to update the image when selected
    public void onImageSelected(Uri selectedImageUri) {
        this.selectedPersonalRecipeImage = selectedImageUri;
        if (selectedPersonalRecipeImage != null) {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedPersonalRecipeImage);

                personalRecipeImage.setImageBitmap(bitmap);

                notifyItemChanged(currentPosition);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        notifyDataSetChanged();
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void cleanupAfterUpdate() {
        selectedPersonalRecipeImage = null;
        base64PersonalRecipeImage = null;

        recipeName = null;
        ingredients.clear();
        instructions.clear();
        ingredientList.clear();
        instructionList.clear();

        if (ingredientAdapter != null) {
            ingredientAdapter.notifyDataSetChanged();
        }
        if (instructionAdapter != null) {
            instructionAdapter.notifyDataSetChanged();
        }

        if (personalRecipeName != null) {
            personalRecipeName.setText("");
        }
        if (personalRecipeImage != null) {
            personalRecipeImage.setImageResource(R.drawable.personal_recipe_image);
        }

        if (confirmRecipeDialog.isShowing()) {
            confirmRecipeDialog.dismiss();
        }

        if (editDialog.isShowing()) {
            editDialog.dismiss();
        }

        notifyItemChanged(currentPosition);
    }


}
class PersonalRecipeViewHolder extends RecyclerView.ViewHolder{
    CardView personalRecipeCardView;
    ImageView personalRecipeImage;
    TextView personalRecipeTitle;
    Button deletePersonalRecipe;

    public PersonalRecipeViewHolder(@NonNull View itemView) {
        super(itemView);
        personalRecipeCardView = itemView.findViewById(R.id.personalRecipeCardView);
        personalRecipeImage = itemView.findViewById(R.id.personalRecipeImage);
        personalRecipeTitle = itemView.findViewById(R.id.personalRecipeTitle);
        deletePersonalRecipe = itemView.findViewById(R.id.deletePersonalRecipe);
    }
}
