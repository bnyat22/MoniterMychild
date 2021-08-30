package com.example.localiserwear;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.localiserwear.databinding.ActivityDialogNewChildBinding;
import com.example.localiserwear.domains.Parent;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.io.IOException;

public class DialogNewChild extends Activity {

    private EditText input;
    private Button ok,upload;

    private DatabaseReference reference;
    private StorageReference firebaseStorage;
    public static final int GET_FROM_GALLERY = 10;
    private Bitmap bitmap;
    private Uri selectedImage;
    String auth;
    private ActivityDialogNewChildBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDialogNewChildBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        input = binding.inputDialog;
        ok = binding.okDialogNewChild;
        upload = binding.uploadPhoto;
        auth = this.getIntent().getStringExtra("userId");
        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        //  referenceDevices = FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        upload.setOnClickListener((t) -> {
            startActivityForResult(new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
        });
        ok.setOnClickListener((t) -> {
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("id")
                    .setValue(getIntent().getStringExtra("deviceId"));
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities").child("appels").setValue("false");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities").child("messages").setValue("false");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities").child("images").setValue("false");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities").child("videos").setValue("false");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities").child("tracer").setValue("false");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities").child("restrictions").setValue("false");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("authorities").child("parler").setValue("false");
            reference.child(auth)
                    .child("children").child(input.getText().toString()).child("network").setValue("true");
            Parent.setChildName(input.getText().toString());


            if (selectedImage !=null)
            firebaseStorage.child("profilePictures").child(input.getText()
                    .toString()).putFile(selectedImage).addOnSuccessListener((uri) -> uri.getStorage().getDownloadUrl().addOnSuccessListener((uri1 -> {
                reference.
                        child(auth)
                        .child("children").child(input.getText().toString()).child("picture").setValue(uri1.toString());
            })));

            startActivity(new Intent(this , Home.class));

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
}