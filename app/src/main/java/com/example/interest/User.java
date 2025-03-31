package com.example.interest;

public class User {
    public String email;
    public String name;
    public String bio;
    public String profileImage;  // Nuovo campo per l'immagine in Base64

    public User() {
        // Costruttore vuoto richiesto da Firebase
    }

    // Costruttore con tutti i campi
    public User(String email, String name, String bio, String profileImage) {
        this.email = email;
        this.name = name;
        this.bio = bio;
        this.profileImage = profileImage;  // Inizializza il campo dell'immagine
    }

    public User(String email, String bio, String name) {
        this.email = email;
        this.bio = bio;
        this.name = name;
    }

    // Getter e setter per tutti i campi
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
