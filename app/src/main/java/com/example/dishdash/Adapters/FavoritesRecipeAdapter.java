package com.example.dishdash.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.dishdash.FavoriteRecipeClass.FavoriteRecipeClass;
import com.example.dishdash.GenerateRecipeCardClass.GenerateRecipeCardAPIResponse;
import com.example.dishdash.Listeners.GenerateRecipeCardResponseListener;
import com.example.dishdash.Listeners.RecipeClickResponseListener;
import com.example.dishdash.R;
import com.example.dishdash.RequestManagers.RecipeCardRequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FavoritesRecipeAdapter extends RecyclerView.Adapter<FavoritesRecipeViewHolder> {
    DatabaseReference favoriteReference;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser user;
    String recipeId;
    int currentPosition;

    Context context;
    List<FavoriteRecipeClass> favoriteRecipe;
    RecipeClickResponseListener listener;

    public FavoritesRecipeAdapter(Context context, List<FavoriteRecipeClass> favoriteRecipe, RecipeClickResponseListener listener) {
        this.context = context;
        this.favoriteRecipe = favoriteRecipe;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoritesRecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FavoritesRecipeViewHolder(LayoutInflater.from(context).inflate(R.layout.list_favorite_recipe, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FavoritesRecipeViewHolder holder, int position) {
        FavoriteRecipeClass favoriteRecipeClass = favoriteRecipe.get(position);
        holder.favoriteRecipeTitle.setText(favoriteRecipeClass.getRecipeName());
        holder.favoriteRecipeReadyIn.setText(favoriteRecipeClass.getReadyIn());
        holder.favoriteRecipeServing.setText(favoriteRecipeClass.getServings());
        Glide.with(context).load(favoriteRecipeClass.getRecipeImage()).into(holder.favoriteRecipeImage);

        holder.favoriteRecipeCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onRecipeClick(String.valueOf(favoriteRecipeClass.getRecipeId()));
            }
        });

        holder.removeFromFavorites.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db = FirebaseDatabase.getInstance();
                auth = FirebaseAuth.getInstance();
                user = auth.getCurrentUser();

                View favoriteDialogView = LayoutInflater.from(context).inflate(R.layout.remove_from_fav_dialog, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(favoriteDialogView);
                AlertDialog dialog = builder.create();
                dialog.show();

                Button cancelButton = favoriteDialogView.findViewById(R.id.cancel);
                Button deleteButton = favoriteDialogView.findViewById(R.id.delete);

                cancelButton.setOnClickListener(v -> dialog.dismiss());

                deleteButton.setOnClickListener(v -> {
                    currentPosition = holder.getAdapterPosition();

                    if (currentPosition != RecyclerView.NO_POSITION && currentPosition < favoriteRecipe.size()){
                        favoriteRecipe.remove(currentPosition);
                        notifyItemRemoved(currentPosition);
                        notifyItemRangeChanged(currentPosition, favoriteRecipe.size());

                        recipeId = String.valueOf(favoriteRecipeClass.getRecipeId());
                        favoriteReference = db.getReference("Users").child(user.getUid()).child("Favorites").child(recipeId);

                        favoriteReference.removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "Recipe removed from favorites", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to remove recipe from favorites", Toast.LENGTH_SHORT).show();
                        });

                        dialog.dismiss();
                    }
                });
            }
        });

        // Generate recipe card
        holder.generateRecipeCard.setOnClickListener(v -> {
            View generateRecipeCardDialogView = LayoutInflater.from(context).inflate(R.layout.generate_recipe_card_link_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(generateRecipeCardDialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            TextView recipeCardLink = generateRecipeCardDialogView.findViewById(R.id.recipeCardLink);
            TextView recipeCardLinkHeader = generateRecipeCardDialogView.findViewById(R.id.recipeCardLinkHeader);
            ProgressBar progressBar = generateRecipeCardDialogView.findViewById(R.id.progressBar);

            recipeCardLinkHeader.setText("Generating Recipe Card...");
            recipeCardLink.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            RecipeCardRequestManager recipeCardRequestManager = new RecipeCardRequestManager(context);
            recipeCardRequestManager.generateRecipeCard(new GenerateRecipeCardResponseListener() {
                @Override
                public void didFetchRecipeCard(GenerateRecipeCardAPIResponse response, String message) {

                    progressBar.setVisibility(View.GONE);
                    recipeCardLinkHeader.setText("Your Recipe Card is Ready!");
                    recipeCardLink.setVisibility(View.VISIBLE);
                    recipeCardLink.setText(response.getUrl());

                    recipeCardLink.setPaintFlags(recipeCardLink.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                    recipeCardLink.setOnClickListener(linkView -> {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.getUrl()));
                        context.startActivity(browserIntent);
                        dialog.dismiss();
                    });
                }

                @Override
                public void didError(String message) {

                }
            }, favoriteRecipeClass.getRecipeId(), "heartMask", "FAFAFA", "333333");
        });
    }

    @Override
    public int getItemCount() {
        return favoriteRecipe.size();
    }

    public void updateRecipes(List<FavoriteRecipeClass> newFavoriteRecipe){
        favoriteRecipe = newFavoriteRecipe;
        notifyDataSetChanged();
    }
}
class FavoritesRecipeViewHolder extends RecyclerView.ViewHolder{
    CardView favoriteRecipeCardView;
    ImageView favoriteRecipeImage;
    TextView favoriteRecipeTitle, favoriteRecipeReadyIn, favoriteRecipeServing;
    Button removeFromFavorites;
    ImageView generateRecipeCard;
    ProgressBar progressBar;

    public FavoritesRecipeViewHolder(View itemView) {
        super(itemView);
        favoriteRecipeCardView = itemView.findViewById(R.id.favoriteRecipeCardView);
        favoriteRecipeImage = itemView.findViewById(R.id.favoriteRecipeImage);
        favoriteRecipeTitle = itemView.findViewById(R.id.favoriteRecipeTitle);
        favoriteRecipeReadyIn = itemView.findViewById(R.id.favoriteRecipeReadyIn);
        favoriteRecipeServing = itemView.findViewById(R.id.favoriteRecipeServing);
        removeFromFavorites = itemView.findViewById(R.id.removeFromFavorites);
        generateRecipeCard = itemView.findViewById(R.id.generateRecipeCard);
        progressBar = itemView.findViewById(R.id.progressBar);
    }
}
