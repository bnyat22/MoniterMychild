package com.example.localiserwear.extraActivities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.localiserwear.databinding.ActivityFullMessageBinding;


public class FullMessageActivity extends Activity {

    private TextView title , body ;
    private ImageView back;
    private ActivityFullMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFullMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        title = binding.titleMessageFull;
        body = binding.bodyMessageFull;
        back = binding.backMessage;


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
