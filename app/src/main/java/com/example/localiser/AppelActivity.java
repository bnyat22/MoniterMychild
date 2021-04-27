package com.example.localiser;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AppelActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appel_activity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.topAppBar);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
        auth = FirebaseAuth.getInstance();
//setSupportActionBar(toolbar);


//navigationView.setCheckedItem();

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
            case R.id.tracemenuItem:
                openTrace();
                break;
            case R.id.polygonItem:
                openRestricion();
                break;
            case R.id.logoutmenuItem:
                logout();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    private void openHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private void openAppel() {
        Intent intent = new Intent(this, AppelActivity.class);
        startActivity(intent);
    }

    private void openParler() {
        Intent intent = new Intent(this, ParlerActivity.class);
        startActivity(intent);
    }

    private void openMeassages() {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    private void openTrace() {
        Intent intent = new Intent(this, TraceActivity.class);
        startActivity(intent);
    }

    private void openRestricion() {
        startActivity(new Intent(this, MapsActivity.class));
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
}
