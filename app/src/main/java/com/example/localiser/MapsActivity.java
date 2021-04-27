package com.example.localiser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.example.localiser.domains.MyLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
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

public class MapsActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener, SeekBar.OnSeekBarChangeListener {
    //Drawer elements
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference ,refAuth;
    private DatabaseReference specificReference;
    private String parentId , actuelId;

    //Polygon elements
    private Button draw , clear;
    private SeekBar redSeek , greenSeek , yellowSeek;
    private EditText endroitText , from , to;
    private CheckBox polygonCheckbox , endroitCheckBox;
    Polygon polygon = null;
    List<LatLng> latLngList = new ArrayList<>();
    List<Marker> markerList = new ArrayList<>();
    int red = 0,blue = 0,green = 0;

    //map elements
    private GoogleMap mMap;
    private LocationManager manager;
    private Marker myMarker;
    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //inisializer polygon elements
        draw = findViewById(R.id.btn_draw);
        clear = findViewById(R.id.btn_clear);
        redSeek = findViewById(R.id.seekRed);
        yellowSeek = findViewById(R.id.seekYellow);
        greenSeek = findViewById(R.id.seekGreen);
        endroitText = findViewById(R.id.restrictionText);
        from = findViewById(R.id.fromTime);
        to = findViewById(R.id.toTime);

        polygonCheckbox = findViewById(R.id.checkPoly);
        endroitCheckBox = findViewById(R.id.specificEndroidCheck);
        endroitCheckBox.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (isChecked) {
                endroitText.setVisibility(View.VISIBLE);
                from.setVisibility(View.VISIBLE);
                to.setVisibility(View.VISIBLE);
            }
        }));
        polygonCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
            {
                if (polygon == null)
                    return;
                polygon.setFillColor(Color.rgb(red , blue , green));

            } else
            {
                polygon.setFillColor(Color.TRANSPARENT);
            }
        });
        draw.setOnClickListener(v -> {
            String text = endroitText.getText().toString();
            String fromText = from.getText().toString();
            String toText = to.getText().toString();

            if (endroitCheckBox.isChecked())
            {
            if (!text.equals("") && !fromText.equals("") && !toText.equals(""))
            {
                if (polygon != null) polygon.remove();
                PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
                polygon = mMap.addPolygon(polygonOptions);

                polygon.setStrokeColor(Color.rgb(red , green , blue));
                if (polygonCheckbox.isChecked()) polygon.setFillColor(Color.rgb(red, green , blue));
                specificReference.child(text).child("polygon").setValue(polygon);
                specificReference.child(text).child("from").setValue(fromText);
                specificReference.child(text).child("to").setValue(toText);

            }} else {
                if (polygon != null) polygon.remove();
                PolygonOptions polygonOptions = new PolygonOptions().addAll(latLngList).clickable(true);
                polygon = mMap.addPolygon(polygonOptions);

                polygon.setStrokeColor(Color.rgb(red, green, blue));
                if (polygonCheckbox.isChecked()) polygon.setFillColor(Color.rgb(red, green, blue));
                reference.push().setValue(polygon);
            } });
        clear.setOnClickListener(v -> {
reference.removeValue();
specificReference.removeValue();

        });
        redSeek.setOnSeekBarChangeListener( this);
        greenSeek.setOnSeekBarChangeListener(this);
        yellowSeek.setOnSeekBarChangeListener(this);
        //  initialiser drawer elements
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.topAppBar);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START); });


//initialiser firebase elements
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        reference = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("polygons");
        specificReference = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("specific");

        //initialiser map elements
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(latLng -> {
            MarkerOptions markerOptions = new MarkerOptions().position(latLng);
            Marker marker = mMap.addMarker(markerOptions);
            latLngList.add(latLng);
            markerList.add(marker);
        });
        //    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId())
        {
            case R.id.seekRed:
                red = progress;
                break;
            case R.id.seekGreen:
                green = progress;
                break;
            case R.id.seekYellow:
                blue = progress;
                break;


        }
        if (polygon != null) {
            polygon.setStrokeColor(Color.rgb(red, green, blue));
            if (polygonCheckbox.isChecked()) polygon.setFillColor(Color.rgb(red, green, blue));
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

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

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);

    }
}

