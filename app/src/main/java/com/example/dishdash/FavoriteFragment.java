package com.example.dishdash;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dishdash.Adapters.FavoritesRecipeAdapter;
import com.example.dishdash.FavoriteRecipeClass.FavoriteRecipeClass;
import com.example.dishdash.Listeners.RecipeClickResponseListener;
import com.example.dishdash.databinding.FragmentFavoriteBinding;
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
 * Use the {@link FavoriteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FavoriteFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FavoriteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoriteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoriteFragment newInstance(String param1, String param2) {
        FavoriteFragment fragment = new FavoriteFragment();
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

    FragmentFavoriteBinding binding;
    DatabaseReference favoriteReference;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser user;
    FavoritesRecipeAdapter favoritesRecipeAdapter;
    List<FavoriteRecipeClass> favoriteRecipeClassList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Inflate the layout for this fragment
        binding = FragmentFavoriteBinding.inflate(inflater, container, false);
        InsetsUtil.applyInsets(binding.getRoot());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.favoriteRecipeRecyclerView.setHasFixedSize(true);
        binding.favoriteRecipeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        favoritesRecipeAdapter = new FavoritesRecipeAdapter(requireContext(), favoriteRecipeClassList, recipeClickResponseListener);
        binding.favoriteRecipeRecyclerView.setAdapter(favoritesRecipeAdapter);

        fetchFavoriteRecipes();
    }

    private void fetchFavoriteRecipes(){
        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        favoriteReference = db.getReference("Users").child(user.getUid()).child("Favorites");

        favoriteReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                favoriteRecipeClassList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    FavoriteRecipeClass favoriteRecipeClass = dataSnapshot.getValue(FavoriteRecipeClass.class);
                    if (favoriteRecipeClass != null){
                        favoriteRecipeClassList.add(favoriteRecipeClass);
                    }
                }
                favoritesRecipeAdapter.updateRecipes(favoriteRecipeClassList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private final RecipeClickResponseListener recipeClickResponseListener = new RecipeClickResponseListener() {
        @Override
        public void onRecipeClick(String recipeId) {
            DetailedRecipeDialog detailedRecipeDialog = DetailedRecipeDialog.newInstance(Integer.parseInt(recipeId));
            detailedRecipeDialog.show(getParentFragmentManager(), "DetailedRecipeDialog");
        }
    };
}