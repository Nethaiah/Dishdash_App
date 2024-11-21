package com.example.dishdash.UserClass;

public class UserClass {
    String email, password, username, profileImage;

    public UserClass(String email, String password, String username, String profileImage) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.profileImage = profileImage;
    }

    public UserClass() {
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
