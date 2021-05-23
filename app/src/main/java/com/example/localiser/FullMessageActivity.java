package com.example.localiser;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FullMessageActivity extends AppCompatActivity {
    private TextView title , body;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_full_screen_activity);
        title = findViewById(R.id.title_message_full);
        body = findViewById(R.id.body_message_full);

        title.setText(getIntent().getStringExtra("title"));
        body.setText(getIntent().getStringExtra("body"));


    }
}
