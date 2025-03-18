package com.example.interest;

public class User {
    public String email;
    public String name;
    public String bio;

    public User() {
        // Costruttore vuoto richiesto da Firebase
    }

    public User(String email, String name, String bio) {
        this.email = email;
        this.name = name;
        this.bio = bio;
    }
}