package com.example.dishdash;

import android.app.AlarmManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.dishdash.ViewModels.ProfileAndEmailViewModel;
import com.example.dishdash.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class home extends AppCompatActivity {
    ActivityHomeBinding binding;
    ProfileAndEmailViewModel profileAndEmailViewModel;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        InsetsUtil.applyInsets(binding.getRoot());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        profileAndEmailViewModel = new ViewModelProvider(this).get(ProfileAndEmailViewModel.class);
        displayProfilePictureAndEmail();

        if (getIntent().getBooleanExtra("showMealPlanner", false)) {
            binding.bottomNavView.setSelectedItemId(R.id.navMealPlanner);
            loadFragment(new MealPlannerFragment());
        } else {
            binding.bottomNavView.setSelectedItemId(R.id.navHome);
            loadFragment(new HomeFragment());
        }

        String target = getIntent().getStringExtra("NAV_TARGET");

        if ("PROFILE".equals(target)) {
            binding.bottomNavView.setSelectedItemId(R.id.navProfile);
            loadFragment(new ProfileFragment());
        }

        binding.bottomNavView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.navHome) {
                selectedFragment = new HomeFragment();

            } else if (item.getItemId() == R.id.navPersonalRecipe) {
                selectedFragment = new PersonalRecipeFragment();

            } else if (item.getItemId() == R.id.navFavorites) {
                selectedFragment = new FavoriteFragment();

            } else if (item.getItemId() == R.id.navMealPlanner) {
                selectedFragment = new MealPlannerFragment();

            } else if (item.getItemId() == R.id.navProfile) {
                selectedFragment = new ProfileFragment();
            }
            // Load the selected fragment
            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }

            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.bottomNavFrameLayout, fragment).commit();
    }

    private void displayProfilePictureAndEmail() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference();
        user = auth.getCurrentUser();

        String userID = user.getUid();
        String signInProvider = user.getProviderData().get(1).getProviderId();

        if (signInProvider.equals("password")) {
            String email = user.getEmail();
            String nickname = extractUsernameFromEmail(email);

            profileAndEmailViewModel.setEmail(nickname);

            databaseReference.child("Users").child(userID).child("profilePicture").get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String base64Image = dataSnapshot.getValue(String.class);
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    if (bitmap != null) {
                        profileAndEmailViewModel.setProfilePicture(bitmap);
                    }
                }

            }).addOnFailureListener(e -> {
                Log.e("Profile", "Failed to fetch profile picture", e);
            });
        } else if (signInProvider.equals("google.com")) {
            String nickname = user.getDisplayName();
            String profileImageUrl = user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null;

            profileAndEmailViewModel.setEmail(nickname);

            if (profileImageUrl != null) {
                Glide.with(this).asBitmap().load(profileImageUrl).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        profileAndEmailViewModel.setProfilePicture(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
            }
        }

        databaseReference.child("Users").child(userID).child("profilePicture").get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String base64Image = dataSnapshot.getValue(String.class);
                Bitmap bitmap = decodeBase64ToBitmap(base64Image);

                if (bitmap != null) {
                    profileAndEmailViewModel.setProfilePicture(bitmap);
                } else {
                    Toast.makeText(this, "Failed to decode image", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(failToFetch -> {
            Toast.makeText(this, "Failed to fetch profile picture.", Toast.LENGTH_SHORT).show();
        });
    }

    private String extractUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            Log.e("Nickname", "Email is null or does not contain '@'");
            return "Unknown";
        }
        return email.split("@")[0];
    }

    private Bitmap decodeBase64ToBitmap(String base64Image){
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (IllegalArgumentException e) {
            Log.e("Profile", "Error decoding Base64 image", e);
            return null;
        }
    }
}