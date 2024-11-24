package com.example.dishdash;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dishdash.ViewModels.ProfileAndEmailViewModel;
import com.example.dishdash.databinding.FragmentProfileBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    FragmentProfileBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference databaseReference;
    FirebaseDatabase db;
    Button noButton, yesButton;
    ImageView addProfilePicture;
    EditText editNickname;
    View logoutDialogView, deleteAccountDialogView, uploadProfilePictureDialogView;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    Uri selectedImageUri;
    ProfileAndEmailViewModel profileAndEmailViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        profileAndEmailViewModel = new ViewModelProvider(requireActivity()).get(ProfileAndEmailViewModel.class);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null){
                selectedImageUri = result.getData().getData();
                addProfilePicture.setImageURI(selectedImageUri);
            }
        });

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        InsetsUtil.applyInsets(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Observe the email (nickname) and profile picture
        profileAndEmailViewModel.getEmail().observe(getViewLifecycleOwner(), nickname -> {
            if (nickname != null) {
                binding.userEmail.setText(nickname); // Update nickname in UI
            }
        });

        profileAndEmailViewModel.getProfilePicture().observe(getViewLifecycleOwner(), bitmap -> {
            if (bitmap != null) {
                binding.profilePicture.setImageBitmap(bitmap);
            } else {
                binding.profilePicture.setImageResource(R.drawable.add_friend);
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        String signInProvider = auth.getCurrentUser().getProviderData().get(1).getProviderId();

        if (signInProvider.equals("google.com")) {
            binding.editAccount.setOnClickListener(profilePicture -> {
                Toast.makeText(getContext(), "Personal information cannot be edited for Google accounts.", Toast.LENGTH_SHORT).show();
            });
        } else {
            binding.editAccount.setOnClickListener(v -> {
                uploadProfilePictureDialogView = LayoutInflater.from(getContext()).inflate(R.layout.add_profile_picture, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(uploadProfilePictureDialogView);
                AlertDialog dialog = builder.create();
                dialog.show();

                noButton = uploadProfilePictureDialogView.findViewById(R.id.no);
                yesButton = uploadProfilePictureDialogView.findViewById(R.id.yes);
                addProfilePicture = uploadProfilePictureDialogView.findViewById(R.id.addProfilePicture);
                editNickname = uploadProfilePictureDialogView.findViewById(R.id.personalInformationNickname);

                noButton.setOnClickListener(cancelView -> dialog.dismiss());

                addProfilePicture.setOnClickListener(pickProfile -> {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    imagePickerLauncher.launch(intent);
                });

                yesButton.setOnClickListener(uploadProfilePictureView -> {
                    if (selectedImageUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImageUri);
                            String base64Image = encodeImageToBase64(bitmap);

                            auth = FirebaseAuth.getInstance();
                            db = FirebaseDatabase.getInstance();
                            databaseReference = db.getReference();

                            String editedNickname = editNickname.getText().toString().trim();
                            String userId = auth.getCurrentUser().getUid();

                            databaseReference.child("Users").child(userId).child("profilePicture").setValue(base64Image)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Profile picture uploaded successfully.", Toast.LENGTH_SHORT).show();
                                        profileAndEmailViewModel.setProfilePicture(bitmap);
                                        binding.profilePicture.setImageBitmap(bitmap);

                                        if (!editedNickname.isEmpty()) {
                                            databaseReference.child("Users").child(userId).child("username").setValue(editedNickname).addOnSuccessListener(newUsername -> {
                                                Toast.makeText(getContext(), "Username updated successfully.", Toast.LENGTH_SHORT).show();
                                                profileAndEmailViewModel.setEmail(editedNickname);
                                                binding.userEmail.setText(editedNickname);
                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(getContext(), "Failed to update nickname.", Toast.LENGTH_SHORT).show();
                                                Log.e("Profile", "Failed to update nickname", e);
                                            });
                                        }

                                        dialog.dismiss();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to upload profile picture.", Toast.LENGTH_SHORT).show();
                                        Log.e("Profile", "Failed to upload profile picture", e);
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getContext(), "Please select an image.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);


        binding.profileForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), forgotPassword.class));
        });

        binding.termsAndConditions.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), TermsAndConditions.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
            this.getActivity().finish();
        });

        binding.privacyPolicy.setOnClickListener(v -> {
           startActivity(new Intent(getActivity(), PrivacyPolicy.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
           this.getActivity().finish();
        });

        binding.profileLogOut.setOnClickListener(v -> {
            logoutDialogView = LayoutInflater.from(getContext()).inflate(R.layout.log_out_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(logoutDialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            noButton = logoutDialogView.findViewById(R.id.no);
            yesButton = logoutDialogView.findViewById(R.id.yes);

            noButton.setOnClickListener(logOutView -> dialog.dismiss());

            yesButton.setOnClickListener(logOutView -> {
                auth = FirebaseAuth.getInstance();

                if (!isConnectedToInternet(getContext())) {
                    Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                user = auth.getCurrentUser();

                String providerId = user.getProviderData().get(1).getProviderId();

                if (providerId.equals("password")) {
                    auth.signOut();
                    startActivity(new Intent(getActivity(), Register.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    requireActivity().finish();
                    dialog.dismiss();
                } else if (providerId.equals("google.com")) {
                    auth.signOut();
                    googleSignInClient.revokeAccess().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(getActivity(), Register.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            requireActivity().finish();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "Error logging out. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });


        binding.deleteAccount.setOnClickListener(v -> {
            deleteAccountDialogView = LayoutInflater.from(getContext()).inflate(R.layout.delete_account_dialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(deleteAccountDialogView);
            AlertDialog dialog = builder.create();
            dialog.show();

            noButton = deleteAccountDialogView.findViewById(R.id.no);
            yesButton = deleteAccountDialogView.findViewById(R.id.yes);

            noButton.setOnClickListener(logOutView -> dialog.dismiss());

            yesButton.setOnClickListener(deleteAccountView -> {
                auth = FirebaseAuth.getInstance();

                if (!isConnectedToInternet(getContext())) {
                    Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                db = FirebaseDatabase.getInstance();
                databaseReference = db.getReference("Users");
                String userId = auth.getCurrentUser().getUid();

                databaseReference.child(userId).removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        auth.getCurrentUser().delete().addOnCompleteListener(deleteTask -> {
                            if (deleteTask.isSuccessful()) {
                                googleSignInClient.signOut().addOnCompleteListener(deleteAccountTask -> {
                                    startActivity(new Intent(getActivity(), Register.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    this.getActivity().finish();
                                    dialog.dismiss();
                                });
                            } else {
                                Toast.makeText(getContext(), "Failed to delete account from authentication.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to delete account data from database.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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