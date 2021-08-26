package com.example.localiser;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.localiser.domains.Parent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ParlerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
private FirebaseAuth auth;
private DatabaseReference reference , childRef;
private ImageView parler,ecouter,arreter , arreterPalrer;
    private String actuelId;
    private String parentId;
    private List<String> childList;
    private Spinner dropdown;
    private ArrayAdapter arrayAdapter;
    private String childName;

    @SuppressLint("HardwareIds")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parler_activity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.topAppBar);
        navigationView.bringToFront();

        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START); });
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        childRef = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("parler").child("which");

        parler = findViewById(R.id.parler);
        ecouter = findViewById(R.id.ecouter);
        arreter = findViewById(R.id.arreter);
        arreterPalrer = findViewById(R.id.arreterParler);
        parentId = ((Parent) this.getApplication()).getParentId();
        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);

        childList = new ArrayList<>();
        childList.add("Choisissez un enfant");
        getChildNames();
        arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1 , childList);
        dropdown = findViewById(R.id.spinner_speaker);
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





    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homemenuItem:
                openHome();
                break;
            case R.id.appelmenuItem:
                openAppel();
                break;
            case R.id.meesagemenuItem:
                openMeassages();
                break;
            case R.id.parlemenuItem:
                openParler();
                break;
            case R.id.imagesItem:
                openImages();
                break;
            case R.id.videosItem:
                openVidoes();
                break;
            case R.id.tracemenuItem:
                openTrace();
                break;
            case R.id.polygonItem:
                openRestricion();
                break;
            case R.id.browserItem:
                openBrowser();
                break;
            case R.id.logoutmenuItem:
                logout();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openVidoes() {
        Intent intent = new Intent(this , VideoActivity.class);
        startActivity(intent);
    }

    private void openImages() {
        Intent intent = new Intent(this , ImageActivity.class);
        startActivity(intent);
    }


    private void openHome() {
        Intent intent = new Intent(this , Home.class);
        startActivity(intent);
    }
    private void openAppel() {
        Intent intent = new Intent(this , AppelActivity.class);
        startActivity(intent);
    }

    private void openParler() {
        Intent intent = new Intent(this , ParlerActivity.class);
        startActivity(intent);
    }

    private void openMeassages() {
        Intent intent = new Intent(this , MessagesActivity.class);
        startActivity(intent);
    }
    private void openTrace() {
        Intent intent = new Intent(this , TraceActivity.class);
        startActivity(intent);
    }
    private void openRestricion() {
        startActivity(new Intent(this , MapsActivity.class));
    }
    private void openBrowser() {
        startActivity(new Intent(this , BrowserHistoryActivity.class));
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);

    }
}

