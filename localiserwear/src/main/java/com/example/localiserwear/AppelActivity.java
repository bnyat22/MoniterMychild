package com.example.localiserwear;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.wear.widget.drawer.WearableActionDrawerView;
import androidx.wear.widget.drawer.WearableDrawerLayout;

import com.example.localiserwear.adapters.AudioAdapter;
import com.example.localiserwear.databinding.ActivityAppelBinding;
import com.example.localiserwear.domains.MyAudio;
import com.example.localiserwear.extraActivities.FullAudioActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AppelActivity extends Activity {

    private AudioAdapter adapter;
    private ListView listView;
    private String childName;
    private List<String> childList;
    private ArrayAdapter<String> arrayAdapter;
    private Spinner dropdown;



    //Device
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference reference ,refChild;
    private WearableDrawerLayout drawerLayout;
    private WearableActionDrawerView actionDrawerView;
    private ActivityAppelBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAppelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        drawerLayout = binding.drawerLayout;

        actionDrawerView = binding.actionDrawer;




        actionDrawerView.setPeekOnScrollDownEnabled(true);

        actionDrawerView.bringToFront();

        drawerLayout.setForegroundGravity(GravityCompat.START);
        actionDrawerView.getController().peekDrawer();
        actionDrawerView.setOnMenuItemClickListener(item -> {
            switch (item.getItemId())
            {
                case R.id.appelmenuItem:
                    startActivity(new Intent(this,AppelActivity.class));
                    System.out.println("betam " + item.getItemId());
                    break;
                case R.id.homemenuItem:
                    startActivity(new Intent(this,Home.class));
                    break;
                case R.id.meesagemenuItem:
                    startActivity(new Intent(this,MessageActivity.class));
                    break;
                case R.id.parlemenuItem:
                    startActivity(new Intent(this,ParlerActivity.class));
                    break;

            }
            actionDrawerView.getController().closeDrawer();
            return true;
        });

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("records");
        refChild = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("children");
        listView = findViewById(R.id.records_list);
        childList = new ArrayList<>();
        childList.add("Choisissez un enfant");
        getChildNames();

        dropdown = binding.spinnerSearch;

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                childName = childList.get(position);
                if (!childName.equals("Choisissez un enfant"))
                    getChildRecords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }
    private void getChildRecords() {
        List<MyAudio> players = new ArrayList<>();
        Query query = reference.child(childName);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    System.out.println("biba lagal xot");
                    String from = ds.getKey();
                    String body = ds.getValue(String.class);
                    MyAudio myAudio = new MyAudio(from , body);

                    players.add(myAudio);

                }
                adapter = new AudioAdapter(AppelActivity.this , players);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        listView.setOnItemClickListener((AdapterView.OnItemClickListener) (parent, view, position, id) -> {
            MyAudio audio = (MyAudio) listView.getItemAtPosition(position);
            startActivity(new Intent(this, FullAudioActivity.class).putExtra("audio",audio.getUri()));
        });




    }

    private void getChildNames() {
        refChild.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    childList.add(ds.getKey());
                }
                arrayAdapter = new ArrayAdapter<>(AppelActivity.this , android.R.layout.simple_list_item_1,childList);
                dropdown.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}