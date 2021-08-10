package com.example.localiser;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import java.io.FileNotFoundException;
import java.io.IOException;




public class checkUserDialog extends Dialog  {
    private static final int GET_FROM_GALLERY = 9;
    EditText name;
    Button upload , ok;


    public checkUserDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.user_check_dialog);
        name = findViewById(R.id.childName);
        ok = findViewById(R.id.dialogButtonOk);
        upload = findViewById(R.id.buttonUpload);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        upload.setOnClickListener((t)->{
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ActivityCompat.startActivityForResult(null,takePicture, 0,savedInstanceState);
        });
    }




}
