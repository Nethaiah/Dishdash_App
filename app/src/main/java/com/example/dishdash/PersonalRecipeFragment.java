package com.example.dishdash;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dishdash.Adapters.FavoritesRecipeAdapter;
import com.example.dishdash.Adapters.PersonalRecipeAdapter;
import com.example.dishdash.FavoriteRecipeClass.FavoriteRecipeClass;
import com.example.dishdash.Listeners.PersonalRecipeSelectImageCallback;
import com.example.dishdash.PersonalRecipeClass.PersonalRecipeClass;
import com.example.dishdash.databinding.FragmentPersonalRecipeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonalRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonalRecipeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PersonalRecipeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalRecipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PersonalRecipeFragment newInstance(String param1, String param2) {
        PersonalRecipeFragment fragment = new PersonalRecipeFragment();
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

    FragmentPersonalRecipeBinding binding;
    DatabaseReference personalRecipeReference;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser user;
    PersonalRecipeAdapter personalRecipeAdapter;
    List<PersonalRecipeClass> personalRecipeClassList = new ArrayList<>();
    ActivityResultLauncher<Intent> imagePickerLauncher;
    PersonalRecipeSelectImageCallback adapterCallback;
    Uri selectedImageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Inflate the layout for this fragment
        binding = FragmentPersonalRecipeBinding.inflate(inflater, container, false);
        InsetsUtil.applyInsets(binding.getRoot());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (personalRecipeAdapter != null && selectedImageUri != null) {
                            personalRecipeAdapter.onImageSelected(selectedImageUri);
                        }
                    }
                }
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.personalRecipeRecyclerView.setHasFixedSize(true);
        binding.personalRecipeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        personalRecipeAdapter = new PersonalRecipeAdapter(requireContext(), personalRecipeClassList, new PersonalRecipeSelectImageCallback() {
            @Override
            public void onImageSelected(Uri selectedImageUri) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                imagePickerLauncher.launch(intent);
            }
        });

        binding.personalRecipeRecyclerView.setAdapter(personalRecipeAdapter);

        fetchPersonalRecipes();
    }

    private void fetchPersonalRecipes(){
        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        personalRecipeReference = db.getReference("Users").child(user.getUid()).child("Personal Recipes");

        personalRecipeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                personalRecipeClassList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PersonalRecipeClass personalRecipeClass = dataSnapshot.getValue(PersonalRecipeClass.class);
                    if (personalRecipeClass != null) {
                        personalRecipeClassList.add(personalRecipeClass);
                    }
                }
                personalRecipeAdapter.updateRecipes(personalRecipeClassList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}