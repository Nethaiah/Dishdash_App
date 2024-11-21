package com.example.dishdash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dishdash.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class forgotPassword extends AppCompatActivity {
    ActivityForgotPasswordBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        InsetsUtil.applyInsets(binding.getRoot());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Users");

        binding.resetPasswordBttn.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.forgotPasswordEmail.getText()).toString().trim();

            if (isInputValid(email)){
                sendPasswordResetEmail(email);
            }
        });

        binding.backButton.setOnClickListener(view -> {
            finish();
        });
    }

    @SuppressLint("SetTextI18n")
    private boolean isInputValid(String email){
        if (email.isEmpty()){
            displayErrorWithTimer("Please fill in all fields");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            displayErrorWithTimer("Invalid Email");
            return false;
        }

        return true;
    }

    @SuppressLint("SetTextI18n")
    private void sendPasswordResetEmail(String email) {
        auth = FirebaseAuth.getInstance();

        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                binding.forgotPasswordError.setText("");
                binding.forgotPasswordSuccess.setText("Password reset email sent");
                startActivity(new Intent(forgotPassword.this, LogIn.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            } else {
                displayErrorWithTimer("Invalid Email");
            }
        });
    }

    private void displayErrorWithTimer(String message) {
        binding.forgotPasswordError.setText(message);
        binding.forgotPasswordError.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.forgotPasswordError.setVisibility(View.GONE);
        }, 2000); // 2000 milliseconds = 2 seconds
    }
}