package com.example.localiser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localiser.domains.Parent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button login;
    TextView register;
    EditText email, password;
    FirebaseAuth auth;
    DatabaseReference reference;
    Intent homeIntent , map;
    private static final String EMAIL = "";
    private static final String PASSWORD = "";
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.loginButton);
        register = findViewById(R.id.regLog);
        email = findViewById(R.id.mailEdit);
        password = findViewById(R.id.passEdit);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        if (savedInstanceState != null)
        {
            email.setText(savedInstanceState.getString(EMAIL));
            password.setText(savedInstanceState.getString(PASSWORD));

        }
        stopService(new Intent(this, GpsTracker.class));
        stopService(new Intent(this, SecouerService.class));
        stopService(new Intent(this, TService.class));
        stopService(new Intent(this, ParlerService.class));

            PicJobService.stopJobService(this);
            VideoJobService.stopJobService(this);
            stopService(new Intent(this , PicJobService.class));
            stopService(new Intent(this , VideoJobService.class));
        
        homeIntent = new Intent(this, Home.class);
        map = new Intent(this, MapsActivity.class);
        checkLogin();
        login.setOnClickListener(v -> {
           // startActivity(homeIntent);
            userLogin();
        });
        Intent regIntent = new Intent(this, Registration.class);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(regIntent);
            }
        });
    }

    private void checkLogin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
        {
           reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   ((Parent) MainActivity.this.getApplication()).setParentId(snapshot.child("deviceId").getValue(String.class));
               System.out.println    ("awah naza mor ");
               System.out.println    ("awah naza mor " +((Parent) MainActivity.this.getApplication()).getParentId());

                   startActivity(homeIntent);
               }

               @Override
               public void onCancelled(@NonNull DatabaseError error) {

               }
           });

        /*    else
                startActivity(map);*/

        } else {
            auth.signOut();
        }
    }


    private void userLogin() {
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();
        if (email.isEmpty()) {
            this.email.setError("Écrivez votre email");
            this.email.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            this.password.setError("Écrivez votre password");
            this.password.requestFocus();
            return;
        }
        if (password.length() < 6) {
            this.password.setError("Votre mot de pass doit être plus que 6");
            this.password.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            this.email.setError("Votre email n'est pas correct");
            this.email.requestFocus();
        }
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            ((Parent) MainActivity.this.getApplication()).setParentId(snapshot.child("deviceId").getValue(String.class));
                            System.out.println    ("awah naza mor ");
                            System.out.println    ("awah naza mor " +((Parent) MainActivity.this.getApplication()).getParentId());

                            startActivity(homeIntent);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else
                    Toast.makeText(MainActivity.this, "Votre email et password ne sont pas correctes", Toast.LENGTH_LONG).show();

            }
        });
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EMAIL , email.getText().toString());
        outState.putString(PASSWORD , password.getText().toString());
    }

    @Override
    public void onBackPressed() {

    }
}


