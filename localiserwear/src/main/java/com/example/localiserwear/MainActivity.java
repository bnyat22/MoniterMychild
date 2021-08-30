package com.example.localiserwear;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.localiserwear.databinding.ActivityMainBinding;
import com.example.localiserwear.domains.Parent;
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
checkLogin();
login.setOnClickListener((t) -> userLogin());


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
                    Parent
                            .setParentId(snapshot.child("deviceId").getValue(String.class));
                    Parent
                            .setParentTelNum(snapshot.child("userDetails").child("num").getValue(String.class));

                    String parentId = Parent.getParentId();
                    System.out.println("lezgin" + parentId);
                    if (!deviceId.equals(parentId)) {
                        boolean exist = false;
                        String childName = null;
                        for (DataSnapshot ds : snapshot.child("children").getChildren()) {

                            if (deviceId.equals(ds.child("id").getValue())) {
                                childName = ds.getKey();
                                exist = true;
                                Parent.setChildName(ds.getKey());
                                Parent.setParentId(parentId);
                                reference.child(auth.getCurrentUser().getUid()).child("children")
                                        .child(ds.getKey()).child("network").setValue("true");
                                startActivity(new Intent(MainActivity.this , Home.class));
                                break;
                            }
                        }
                        if (!exist) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

                            alert.setTitle("Un nouveau enfant?");
                            alert.setMessage("Mettez son nom");


                            final EditText input = new EditText(MainActivity.this);
                            alert.setView(input);

                            alert.setPositiveButton("Ok", (dialog, whichButton) -> {


                                startActivityForResult(new Intent(Intent.ACTION_PICK,
                                        MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);

                                reference.child(auth.getCurrentUser().getUid())
                                        .child("children").child(input.getText().toString()).child("id").setValue(deviceId);
                                reference.child(auth.getCurrentUser().getUid())
                                        .child("children").child(input.getText().toString()).child("picture").setValue(bitmap);
                                reference.child(auth.getCurrentUser().getUid())
                                        .child("children").child(input.getText().toString()).child("network").setValue("true");
                                Parent.setChildName(input.getText().toString());
                                startActivity(homeIntent);
                            });

                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                }
                            });

                            alert.show();

                        }
                           /*else {
                               reference.child(auth.getCurrentUser().getUid())
                                       .child("children").child(childName).child("network").setValue("true");
                               ((Parent) MainActivity.this.getApplication()).setParentId(deviceId);
                               startActivity(homeIntent);
                           }*/



                        System.out.println("awah naza mor ");
                        System.out.println("awah naza mor " + Parent.getParentId());

                    } else
                        startActivity(new Intent(MainActivity.this , Home.class));
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
                        Parent.setParentId(snapshot.child("deviceId").getValue(String.class));
                        Parent.setParentTelNum(snapshot
                                .child("userDetails").child("num").getValue(String.class));

                        boolean exist = false;

                        @SuppressLint("HardwareIds")
                        String  deviceId = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                                Settings.Secure.ANDROID_ID);
                        parentId = Parent.getParentId();

                        if (!parentId.equals(deviceId)) {
                            String childName = null;
                            for (DataSnapshot ds : snapshot.child("children").getChildren()) {
                                if (deviceId.equals(ds.child("id").getValue())) {
                                    exist = true;
                                    childName = ds.getKey();
                                    break;
                                }
                            }
                            if (!exist) {
                                startActivity(new Intent(MainActivity.this,DialogNewChild.class)
                                .putExtra("userId" , auth.getCurrentUser().getUid())
                                        .putExtra("deviceId" ,deviceId));
                            } else {
                                reference.child(auth.getCurrentUser().getUid())
                                        .child("children").child(childName).child("network").setValue("true");
                                Parent.setParentId(parentId);
                                Parent.setChildName(childName);
                                startActivity(new Intent(MainActivity.this , Home.class));
                            }
                        } else {
                            Parent.setParentId(deviceId);
                            startActivity(new Intent(MainActivity.this , Home.class));
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