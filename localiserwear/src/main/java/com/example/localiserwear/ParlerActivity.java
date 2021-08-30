package com.example.localiserwear;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.wear.widget.drawer.WearableActionDrawerView;
import androidx.wear.widget.drawer.WearableDrawerLayout;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

import com.example.localiserwear.databinding.ActivityParlerBinding;
import com.example.localiserwear.domains.Parent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParlerActivity extends Activity {

    //Drawer
    private WearableDrawerLayout drawerLayout;
    private WearableActionDrawerView actionDrawerView;



    //Firebase
    private FirebaseAuth auth;
    private DatabaseReference reference , childRef;
    private ImageView parler,ecouter,arreter , arreterPalrer;
    private String actuelId;
    private String parentId;
    private List<String> childList;
    private Spinner dropdown;
    private ArrayAdapter arrayAdapter;
    private String childName;
    private ActivityParlerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityParlerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Drawer implementation
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
    //Speaking anf listening functionality implementation
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        childRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("parler").child("which");

        parler = binding.parler;
        ecouter = binding.ecouter;
        arreter = binding.arreter;
        arreterPalrer = binding.arreterParler;
        parentId = Parent.getParentId();
        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);

        childList = new ArrayList<>();
        childList.add("Choisissez un enfant");
        getChildNames();
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1 , childList);
        dropdown = binding.spinnerSpeaker;
        dropdown.setAdapter(arrayAdapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                childName = childList.get(position);
                if (!childName.equals("Choisissez un enfant")){
                    reference.child(childName).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            childRef.setValue(snapshot.getValue());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        parler.setOnClickListener(t -> {
            reference.child("parler").child("etat").setValue("2");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                parler.setBackgroundColor(Color.YELLOW);
                parler.getBackground().setAlpha(140);
                ecouter.setBackgroundColor(0x00000000);
                arreter.setBackgroundColor(0x00000000);
                arreterPalrer.setBackgroundColor(0x00000000);
            }
        });
        ecouter.setOnClickListener(t -> {
            reference.child("parler").child("etat").setValue("1");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ecouter.setBackgroundColor(Color.YELLOW);
                ecouter.getBackground().setAlpha(140);
                parler.setBackgroundColor(0x00000000);
                arreter.setBackgroundColor(0x00000000);
                arreterPalrer.setBackgroundColor(0x00000000);
            }
        });
        arreter.setOnClickListener(t -> {
            reference.child("parler").child("etat").setValue("3");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                arreter.setBackgroundColor(Color.YELLOW);
                arreter.getBackground().setAlpha(140);
                ecouter.setBackgroundColor(0x00000000);
                parler.setBackgroundColor(0x00000000);
                arreterPalrer.setBackgroundColor(0x00000000);
            }
        });
        arreterPalrer.setOnClickListener(t -> {
            reference.child("parler").child("etat").setValue("4");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                arreterPalrer.setBackgroundColor(Color.YELLOW);
                arreterPalrer.getBackground().setAlpha(140);
                ecouter.setBackgroundColor(0x00000000);
                parler.setBackgroundColor(0x00000000);
                arreter.setBackgroundColor(0x00000000);
            }
        });

    }
    private void getChildNames() {
        reference.child("children").addListenerForSingleValueEvent(new ValueEventListener() {
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