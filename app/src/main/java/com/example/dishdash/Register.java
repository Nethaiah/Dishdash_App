package com.example.dishdash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.dishdash.UserClass.UserClass;
import com.example.dishdash.databinding.ActivityRegisterBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

public class Register extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference reference;
    GoogleSignInClient googleSignInClient;
    ActivityResultLauncher<Intent> googleSignInLauncher;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        InsetsUtil.applyInsets(binding.getRoot());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        user = auth.getCurrentUser();

        if (user != null){
            startActivity(new Intent(this, home.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))  // Use your actual client ID here
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Launch Google Sign-In
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult o) {
                        Intent data = o.getData();
                        if (data != null){
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(o.getData());
                            try {
                                GoogleSignInAccount account = task.getResult(Exception.class);
                                if (account != null) {
                                    firebaseAuthWithGoogle(account);
                                }
                            } catch (Exception e) {
                                Log.e("Error", Objects.requireNonNull(e.getMessage()));
                            }
                        }
                    }
                });

        binding.signIn.setOnClickListener(v -> {
            startActivity(new Intent(this, LogIn.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        });

        binding.registerButton.setOnClickListener(view -> {
            String email = Objects.requireNonNull(binding.registerEmailAddress.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.registerPassword.getText()).toString().trim();
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

            if (isInputValid(email, password)){
                String username = extractUsernameFromEmail(email);

                UserClass newUser = new UserClass(email, hashedPassword, username, null);
                createUser(email, password, newUser);
            }
        });

        binding.registerWithGoogle.setOnClickListener(v ->
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent())
        );
    }

    @SuppressLint("SetTextI18n")
    private void createUser(String email, String password, UserClass newUser){
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                user = auth.getCurrentUser();
                reference = db.getReference("Users");

                reference.child(user.getUid()).setValue(newUser).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        startActivity(new Intent(Register.this, LogIn.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                });
            } else {
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthUserCollisionException e){
                    displayErrorWithTimer("Email is already in use");
                } catch (Exception e){
                    Log.e("Error", Objects.requireNonNull(e.getMessage()));
                }
            }
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                user = auth.getCurrentUser();
                if (user != null) {
                    reference = db.getReference("Users");

                    reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // User data already exists, so no need to override
                                Toast.makeText(Register.this, "Account is already registered with Google. Please sign in.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Register.this, home.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            } else {
                                // Get user details from Google account
                                String email = user.getEmail();
                                String displayName = account.getDisplayName();
                                String photoUrl = account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : null;

                                // Use email username if display name is null
                                if (displayName == null && email != null) {
                                    displayName = extractUsernameFromEmail(email);
                                }

                                UserClass newUser = new UserClass(email, null, displayName, photoUrl);
                                reference.child(user.getUid()).setValue(newUser)
                                        .addOnCompleteListener(dbTask -> {
                                            if (dbTask.isSuccessful()) {
                                                startActivity(new Intent(Register.this, home.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                                finish();
                                            } else {
                                                Toast.makeText(Register.this, "Database update failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("DatabaseError", "Failed to check if user exists: " + error.getMessage());
                            Toast.makeText(Register.this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Log.e("Google Sign-In", "Firebase Authentication failed.");
                Toast.makeText(Register.this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
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

        if (password.length() < 8){
            displayErrorWithTimer("Password must be at least 8 characters");
            return false;
        }

        return true;
    }

    private String extractUsernameFromEmail(String email) {
        return email.contains("@") ? email.split("@")[0] : email;
    }

    private void displayErrorWithTimer(String message) {
        binding.registerError.setText(message);
        binding.registerError.setVisibility(View.VISIBLE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            binding.registerError.setVisibility(View.GONE);
        }, 2000); // 2000 milliseconds = 2 seconds
    }
}