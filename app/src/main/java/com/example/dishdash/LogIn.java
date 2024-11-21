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

import com.example.dishdash.databinding.ActivityLogInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

public class LogIn extends AppCompatActivity {
    ActivityLogInBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference reference;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityLogInBinding.inflate(getLayoutInflater());
        InsetsUtil.applyInsets(binding.getRoot());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Users");

        binding.register.setOnClickListener(v -> {
            startActivity(new Intent(this, Register.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });

        binding.loginForgotPass.setOnClickListener(v -> {
            startActivity(new Intent(this, forgotPassword.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        });

        binding.logInButton.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.loginEmailAddress.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.loginPassword.getText()).toString().trim();

            if (isInputValid(email, password)){
                signInUser(email, password);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void signInUser(String email, String password){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user = auth.getCurrentUser();

                if (user != null){
                    reference.child(user.getUid()).get().addOnCompleteListener(dbTask -> {
                        if (dbTask.isSuccessful() && dbTask.getResult().exists()){
                            startActivity(new Intent(LogIn.this, home.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            finish();
                        }
                    });
                }
            } else {
                displayErrorWithTimer("Invalid Email or Password");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private boolean isInputValid(String email, String password){
        if (email.isEmpty() || password.isEmpty()){
            displayErrorWithTimer("Please fill in all fields");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            displayErrorWithTimer("Invalid Email");
            return false;
        }

        return true;
    }

    private void displayErrorWithTimer(String message) {
        binding.logInError.setText(message);
        binding.logInError.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.logInError.setVisibility(View.GONE);
        }, 2000); // 2000 milliseconds = 2 seconds
    }
}