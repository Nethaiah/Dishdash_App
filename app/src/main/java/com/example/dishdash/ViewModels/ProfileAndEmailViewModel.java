package com.example.dishdash.ViewModels;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileAndEmailViewModel extends ViewModel {
    MutableLiveData<Bitmap> profileImage = new MutableLiveData<>();
    MutableLiveData<String> email = new MutableLiveData<>();

    public LiveData<Bitmap> getProfilePicture() {
        return profileImage;
    }

    public void setProfilePicture(Bitmap bitmap) {
        profileImage.setValue(bitmap);
    }

    public LiveData<String> getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }
}
