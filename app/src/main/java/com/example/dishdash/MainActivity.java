package com.example.dishdash;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.dishdash.ViewModels.ProfileAndEmailViewModel;
import com.example.dishdash.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    ProfileAndEmailViewModel profileAndEmailViewModel;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        InsetsUtil.applyInsets(binding.getRoot());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        deleteOutdatedMealPlan();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, Register.class));
                finish();
            }
        }, 1000);
    }

    private void deleteOutdatedMealPlan() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot mealPlanSnapshot = userSnapshot.child("mealPlan");
                    String startDateString = mealPlanSnapshot.child("startDate").getValue(String.class);
                    if (startDateString != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        try {
                            Date startDate = dateFormat.parse(startDateString);
                            if (startDate != null && startDate.before(today.getTime())) {
                                mealPlanSnapshot.getRef().removeValue();
                                Log.d("MealPlanCleanup", "Deleted meal plan with start date: " + startDateString);
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MealPlanCleanup", "Failed to read data.", error.toException());
            }
        });
    }
}