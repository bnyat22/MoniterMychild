package com.example.localiser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localiser.domains.Parent;
import com.example.localiser.domains.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity {

    private Button incription , retour;
    private EditText nom , prenom , email , password;
    private FirebaseAuth mAuth;
    private DatabaseReference reference;
    private String deviceId;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_activity);

        incription = findViewById(R.id.inscBut);
        nom = findViewById(R.id.nomEdit);
        prenom = findViewById(R.id.prenomEdit);
        email = findViewById(R.id.emailEditReg);
        password = findViewById(R.id.passEditReg);
        retour = findViewById(R.id.backLog);

        retour.setOnClickListener(v ->{
            Intent intent = new Intent(this , MainActivity.class);
            startActivity(intent);
        });
        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        incription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });


    }



    @SuppressLint("HardwareIds")
    private void registerUser() {
        reference.setValue("hey");
        String email = this.email.getText().toString().trim();
        String nom = this.nom.getText().toString().trim();
        String prenom = this.prenom.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        if (nom.isEmpty()){
            this.nom.setError("Écrivez votre nom");
            this.nom.requestFocus();
            return;
        }
        if (prenom.isEmpty()){
            this.prenom.setError("Écrivez votre prenom");
            this.prenom.requestFocus();
            return;
        }

        if (email.isEmpty()){
            this.email.setError("Écrivez votre email");
            this.email.requestFocus();
            return;
        }
        if (password.isEmpty()){
            this.password.setError("Écrivez votre password");
            this.password.requestFocus();
return;
        }
        if (password.length() <6)
        {
            this.password.setError("Votre mot de pass doit être plus que 6");
            this.password.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            this.email.setError("Votre email n'est pas correct");
            this.email.requestFocus();
        }
        mAuth.createUserWithEmailAndPassword(email , password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("shaw");
                       deviceId = Settings.Secure.getString(this.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        User user = new User(nom, prenom, email, password);
                        reference
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("userDetails")
                                .setValue(user);
                        System.out.println("naroto");
                        reference.setValue("heyo");
                        reference
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("userDetails")
                                .setValue(user).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful())
                                    {
                                        System.out.println("darvo");
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                                                child("deviceId").setValue(deviceId);
                                        ((Parent) Registration.this.getApplication()).setParentId(deviceId);
                                        Toast.makeText(Registration.this,"Vous êtes enregistré!" , Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(Registration.this , MainActivity.class));
                                    } else
                                    {
                                        System.out.println("darvi");
                                        Toast.makeText(Registration.this , "Le démarche n'est pas réussi veuillez essayer encore" , Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else
                        Toast.makeText(Registration.this , "Le démarche n'est pas réussi veuillez essayer encore" , Toast.LENGTH_LONG).show();
                });
    }
}
