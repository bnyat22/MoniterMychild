package com.example.localiser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localiser.domains.Parent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

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
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        login = findViewById(R.id.loginButton);
        register = findViewById(R.id.regLog);
        email = findViewById(R.id.mailEdit);
        password = findViewById(R.id.passEdit);
         auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        referenceDevices = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseStorage = FirebaseStorage.getInstance().getReference();

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
         @SuppressLint("HardwareIds") String  deviceId =
                 Settings.Secure.getString(this.getContentResolver(),
                 Settings.Secure.ANDROID_ID);
           reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                   ((Parent) MainActivity.this.getApplication())
                           .setParentId(snapshot.child("deviceId").getValue(String.class));

                   String parentId = ((Parent) MainActivity.this.getApplication()).getParentId();
                   System.out.println("lezgin" + parentId);
                   if (!deviceId.equals(parentId)) {
                       for (DataSnapshot ds : snapshot.child("children").getChildren()) {
                           boolean exist = false;
                           if (ds.child("id").getValue().equals(deviceId)) {
                               exist = true;
                               ((Parent) MainActivity.this.getApplication())
                                       .setChildName(ds.getKey());
                               break;
                           }
                           if (!exist) {
                               AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                               alert.setTitle("Un nouveau enfant?");
                               alert.setMessage("Mettez son nom");


                               final EditText input = new EditText(MainActivity.this);
                               alert.setView(input);

                               alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int whichButton) {


                                       startActivityForResult(new Intent(Intent.ACTION_PICK,
                                               android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

                                       reference.child(auth.getCurrentUser().getUid())
                                               .child("children").child(input.getText().toString()).child("id").setValue(deviceId);
                                       reference.child(auth.getCurrentUser().getUid())
                                               .child("children").child(input.getText().toString()).child("picture").setValue(bitmap);
                                       ((Parent) MainActivity.this.getApplication()).setChildName(input.getText().toString());
                                       startActivity(homeIntent);
                                   }
                               });

                               alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int whichButton) {
                                       // Canceled.
                                   }
                               });

                               alert.show();

                           } else {
                               ((Parent) MainActivity.this.getApplication()).setParentId(deviceId);
                               startActivity(homeIntent);
                           }

                       }

                       System.out.println("awah naza mor ");
                       System.out.println("awah naza mor " + ((Parent) MainActivity.this.getApplication()).getParentId());

                   } else
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
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ((Parent) MainActivity.this.getApplication()).setParentId(snapshot.child("deviceId").getValue(String.class));
                        System.out.println    ("awah naza mor ");
                        System.out.println    ("awah naza mor " +((Parent) MainActivity.this.getApplication()).getParentId());

                        boolean exist = false;

                        @SuppressLint("HardwareIds")
                        String  deviceId = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        parentId = ((Parent) MainActivity.this.getApplication()).getParentId();

                        if (!parentId.equals(deviceId)) {
                            for (DataSnapshot ds : snapshot.child("children").getChildren()) {
                                if (deviceId.equals(ds.child("id").getValue())) {
                                    exist = true;
                                    break;
                                }
                            }
                            if (!exist) {
                                @SuppressLint("ResourceType") AlertDialog.Builder alert =
                                        new AlertDialog.Builder(MainActivity.this);
                                alert.setCancelable(true);
                                alert.setTitle("Un nouvaeu enfant?");
                                alert.setMessage("Ajoutez-le");

                                final EditText input = new EditText(MainActivity.this);
                                input.setHint("Mettez son nom");
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                alert.setView(input);
                                alert.setNeutralButton("Télécharger sa photo",((dialog, which) -> {}));
                                alert.setPositiveButton("ok", (dialog, which) -> {
                                });
                                AlertDialog dialog = alert.create();
                                dialog.show();
                                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener((t)->{
                                    startActivityForResult(new Intent(Intent.ACTION_PICK,
                                            MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

                                });
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((t)->{
                                    reference.child(auth.getCurrentUser().getUid())
                                            .child("children").child(input.getText().toString()).child("id").setValue(deviceId);
                                    firebaseStorage.child("profilePictures").child(input.getText()
                                            .toString()).putFile(selectedImage).addOnSuccessListener((uri) -> {
                                        uri.getStorage().getDownloadUrl().addOnSuccessListener((uri1 -> {

                                            reference.
                                            child(auth.getCurrentUser().getUid())
                                                    .child("children").child(input.getText().toString()).child("picture").setValue(uri1.toString());
                                            ((Parent) MainActivity.this.getApplication()).setChildName(input.getText().toString());
                                            startActivity(homeIntent);
                                            dialog.dismiss();
                                        }));
                                    });


                                });




                            } else {
                                ((Parent) MainActivity.this.getApplication()).setParentId(deviceId);
                                startActivity(homeIntent);
                            }
                        } else {
                            ((Parent) MainActivity.this.getApplication()).setParentId(deviceId);
                            startActivity(homeIntent);
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else
                Toast.makeText(MainActivity.this, "Votre email et password ne sont pas correctes", Toast.LENGTH_LONG).show();

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
             selectedImage = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
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


