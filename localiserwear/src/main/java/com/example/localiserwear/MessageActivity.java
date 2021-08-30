package com.example.localiserwear;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.wear.widget.drawer.WearableActionDrawerView;
import androidx.wear.widget.drawer.WearableDrawerLayout;

import com.example.localiserwear.adapters.MessageAdapter;
import com.example.localiserwear.databinding.ActivityHomeBinding;
import com.example.localiserwear.databinding.ActivityMessageBinding;
import com.example.localiserwear.domains.MyMessage;
import com.example.localiserwear.extraActivities.FullMessageActivity;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends Activity{

    private FirebaseAuth auth;
    private ListView listView;
    private MessageAdapter adapter;
    private String childName;
    private List<String> childList;
    private ArrayAdapter<String> arrayAdapter;
    private Spinner dropdown;
    private DatabaseReference reference , refChild;
    private ActivityMessageBinding binding;

    //drawer
    private WearableDrawerLayout drawerLayout;
    private WearableActionDrawerView actionDrawerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Drawer part
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
        listView =binding.messagesList;
        childList = new ArrayList<>();
        childList.add("Choisissez un enfant");
        arrayAdapter = new ArrayAdapter<>(this , android.R.layout.simple_list_item_1 , childList);
        dropdown = binding.spinnerSearch;
        dropdown.setAdapter(arrayAdapter);
        reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(auth.getCurrentUser().getUid()).child("messages");
        refChild = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(auth.getCurrentUser().getUid()).child("children");
        getChildNames();
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                childName = childList.get(position);
                if (!childName.equals("Choisissez un enfant"))
                    getChildMessages(childName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
   /*     AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, this.getTaskId(), getPackageName());
        boolean granted = (mode == AppOpsManager.MODE_ALLOWED);*/
        //    System.out.println("place " + granted);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS},230);
        }
    }

    private void getChildMessages(String childName) {

        List<MyMessage> messages = new ArrayList<>();
        Query query = reference;
        query.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren())
                {
                    if (ds.getKey().equals(childName))
                        ds.getChildren().forEach((dt)->
                        {
                            MyMessage myMessage = new MyMessage(dt.getKey(), dt.getValue(String.class));
                            messages.add(myMessage);

                        });

                }
                adapter = new MessageAdapter(MessageActivity.this,messages);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            MyMessage message = (MyMessage) listView.getItemAtPosition(position);
            Intent intent = new Intent(this, FullMessageActivity.class);
            intent.putExtra("title",message.getTitle());
            intent.putExtra("body",message.getBody());
            startActivity(intent);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}