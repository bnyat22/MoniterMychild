package com.example.localiser;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class FullMessageActivity extends AppCompatActivity  {

    private TextView title , body ;
       private ImageView back;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_full_screen_activity);
        title = findViewById(R.id.title_message_full);
        body = findViewById(R.id.body_message_full);
        back = findViewById(R.id.back_message);


        System.out.println("darvin " +getIntent().getStringExtra("title"));
        System.out.println("roman " +getIntent().getStringExtra("title"));
        title.setText(getIntent().getStringExtra("title"));
        body.setText(getIntent().getStringExtra("body"));

        back.setOnClickListener(v -> {
            finish();
            overridePendingTransition(0,0);
        });
    }




}

