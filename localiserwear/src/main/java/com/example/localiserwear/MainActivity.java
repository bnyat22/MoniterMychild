package com.example.localiserwear;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.localiserwear.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MainActivity extends Activity {

    Button login;
    TextView register;
    EditText email, password;
    FirebaseAuth auth;
    DatabaseReference reference;
    DatabaseReference referenceDevices;
    StorageReference firebaseStorage;
    Intent homeIntent , map;

    private String parentId;
    private static final String EMAIL = "";
    private static final String PASSWORD = "";
    public static final int GET_FROM_GALLERY = 10;
    private Bitmap bitmap;
    private Uri selectedImage;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        login = binding.loginButton;

        email = binding.mailEdit;
        password = binding.passEdit;
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        referenceDevices = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseStorage = FirebaseStorage.getInstance().getReference();
login.setOnClickListener((t) -> startActivity(new Intent(this,Home.class)));


    }
}