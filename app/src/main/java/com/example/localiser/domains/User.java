package com.example.localiser.domains;

public class User {

    public String nom , prenom , email , password , number;

    public User() {
    }

    public User(String nom, String prenom, String email, String password , String number) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.number = number;

    }
}
