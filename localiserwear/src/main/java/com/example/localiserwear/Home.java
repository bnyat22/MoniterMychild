package com.example.localiserwear;

import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.widget.drawer.WearableActionDrawerView;
import androidx.wear.widget.drawer.WearableDrawerLayout;
import androidx.wear.widget.drawer.WearableNavigationDrawerView;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.localiserwear.databinding.ActivityHomeBinding;
import com.google.android.material.navigation.NavigationView;

public class Home extends FragmentActivity implements OnMapReadyCallback , WearableNavigationDrawerView.OnItemSelectedListener {

    private GoogleMap mMap;
    private ActivityHomeBinding binding;
    private WearableDrawerLayout drawerLayout;
    private WearableActionDrawerView actionDrawerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        drawerLayout = binding.drawerLayout;

        actionDrawerView = binding.actionDrawer;




actionDrawerView.setPeekOnScrollDownEnabled(true);

actionDrawerView.bringToFront();

drawerLayout.setForegroundGravity(GravityCompat.START);
actionDrawerView.getController().peekDrawer();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    @Override
    public void onItemSelected(int pos) {

    }
}