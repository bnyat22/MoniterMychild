package com.example.localiser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.localiser.domains.MyEditTextDatePicker;
import com.example.localiser.domains.MyEditTextHourPicker;
import com.example.localiser.domains.MyLocation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.Locale;

public class TraceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
, OnMapReadyCallback {
    //Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    EditText dateFrom , dateTo , hourFrom , hourTo;
    Button trace , clear;
    Date dateT , dateF , hourF , hourT;
    //firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference , polyLineRef , refAuth;
    private String actuelId , parentId;
    //map elements
    private GoogleMap mMap;
    private LocationManager manager;
    private final int MIN_TIME = 1000; //une seconde
    private final int MIN_DIS = 1; //un mêtre
    private Marker myMarker;
    private List<LatLng> latLngs = new ArrayList<>();
    private Polyline polyline;
    private final double degreesPerRadian = 180.0 / Math.PI;
    @SuppressLint({"SimpleDateFormat", "HardwareIds"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trace_activity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.topAppBar);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START); });
        trace = findViewById(R.id.btn_trace);
        clear = findViewById(R.id.btn_clearTrace);
        dateFrom = findViewById(R.id.traceDatefrom);
        dateTo = findViewById(R.id.traceDateto);
        hourFrom = findViewById(R.id.traceHourfrom);
        hourTo = findViewById(R.id.traceHourto);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("locations");
        polyLineRef = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("polyline");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
clear.setOnClickListener(v -> {
    System.out.println("poly line akal daka");
    for (int i = 0; i < latLngs.size() - 1; i++) {
        LatLng src = latLngs.get(i);
        LatLng dest = latLngs.get(i + 1);
        System.out.println("poly line akay daka");
        // mMap is the Map Object
        polyline= mMap.addPolyline(
                new PolylineOptions().add(
                        new LatLng(src.latitude, src.longitude),
                        new LatLng(dest.latitude,dest.longitude)
                )
        );
        polyline.setEndCap(
                new CustomCap(bitmapDescriptorFromVector(this,R.drawable.ic_baseline_arrow_upward_24),
                        100));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : latLngs) {
            builder.include(latLng);
        }
        final LatLngBounds bounds = builder.build();
        //BOUND_PADDING is an int to specify padding of bound.. try 100.
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,100);
        mMap.animateCamera(cu);
    //   DrawArrowHead(mMap , src , dest);
    }
});
trace.setOnClickListener(v -> {
    Query query1 = polyLineRef;
    try {
        dateF = new SimpleDateFormat("dd/MM/yyyy" , Locale.FRANCE).parse(dateFrom.getText().toString().trim());
        dateT = new SimpleDateFormat("dd/MM/yyyy" ,Locale.FRANCE).parse(dateTo.getText().toString().trim());
        hourF = new SimpleDateFormat("HH:mm" , Locale.FRANCE).parse(hourFrom.getText().toString().trim());
        hourT = new SimpleDateFormat("HH:mm" , Locale.FRANCE).parse(hourTo.getText().toString().trim());
    } catch (ParseException e) {
        e.printStackTrace();
    }
    query1.addListenerForSingleValueEvent(new ValueEventListener() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
           snapshot.getChildren().forEach( ds -> {
                try {
                    String myDate = ds.getKey();
                String rightDate = myDate.replaceAll("\\-", "/");
System.out.println(rightDate + "here is my date");
                    @SuppressLint("SimpleDateFormat") Date date = new SimpleDateFormat("dd/MM/yyyy").parse(rightDate);
                    if (date.compareTo(dateF) == 0 || date.compareTo(dateT) ==0 ||
                            date.after(dateF) && date.before(dateT))
                    {
                        ds.getChildren().forEach( h -> {
                                    try {
                                        MyLocation location;
                                        @SuppressLint("SimpleDateFormat")
                                      Date  dateHour = new SimpleDateFormat("HH:mm").parse(h.getKey());
                                        if (dateHour.compareTo(hourF) == 0 || dateHour.compareTo(hourT) ==0
                                                || dateHour.after(hourF) && dateHour.before(hourT))
                                        {
                                            location = h.getValue(MyLocation.class);
                                            LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
                                            System.out.println("deta eranakana " + h.child("latitude").getValue(Double.class).toString());
                                          latLngs.add(latLng);
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
        }
    });
    System.out.println("poly line akat daka");
});
      MyEditTextDatePicker myEditTextDatePickerFrom = new MyEditTextDatePicker(this , R.id.traceDatefrom);
      MyEditTextDatePicker myEditTextDatePickerTo = new MyEditTextDatePicker(this , R.id.traceDateto);
     MyEditTextHourPicker myEditTextHourPickerFrom = new MyEditTextHourPicker(this , R.id.traceHourfrom);
     MyEditTextHourPicker myEditTextHourPickerTo = new MyEditTextHourPicker(this , R.id.traceHourto);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
    private void DrawArrowHead(GoogleMap mMap, LatLng from, LatLng to){
        // obtain the bearing between the last two points
        double bearing = GetBearing(from, to);
        // round it to a multiple of 3 and cast out 120s
        double adjBearing = Math.round(bearing / 3) * 3;
        while (adjBearing >= 120) {
            adjBearing -= 120;
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        // Get the corresponding triangle marker from Google
        URL url;
        Bitmap image = null;
        try {
            url = new URL("http://www.google.com/intl/en_ALL/mapfiles/dir_" + String.valueOf((int)adjBearing) + ".png");
            try {
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (image != null){
            // Anchor is ratio in range [0..1] so value of 0.5 on x and y will center the marker image on the lat/long
            float anchorX = 0.5f;
            float anchorY = 0.5f;
            int offsetX = 0;
            int offsetY = 0;
            //315 range -- 22.5 either side of 315
            if (bearing >= 292.5 && bearing < 335.5){
                offsetX = 24;
                offsetY = 24;
            }
            //270 range
            else if (bearing >= 247.5 && bearing < 292.5){
                offsetX = 24;
                offsetY = 12;
            }
            //225 range
            else if (bearing >= 202.5 && bearing < 247.5){
                offsetX = 24;
                offsetY = 0;
            }
            //180 range
            else if (bearing >= 157.5 && bearing < 202.5){
                offsetX = 12;
                offsetY = 0;
            }
            //135 range
            else if (bearing >= 112.5 && bearing < 157.5){
                offsetX = 0;
                offsetY = 0;
            }
            //90 range
            else if (bearing >= 67.5 && bearing < 112.5){
                offsetX = 0;
                offsetY = 12;
            }
            //45 range
            else if (bearing >= 22.5 && bearing < 67.5){
                offsetX = 0;
                offsetY = 24;
            }
            //0 range - 335.5 - 22.5
            else {
                offsetX = 12;
                offsetY = 24;
            }
            Bitmap wideBmp;
            Canvas wideBmpCanvas;
            Rect src, dest;
            // Create larger bitmap 4 times the size of arrow head image
            wideBmp = Bitmap.createBitmap(image.getWidth() * 2, image.getHeight() * 2, image.getConfig());
            wideBmpCanvas = new Canvas(wideBmp);
            src = new Rect(0, 0, image.getWidth(), image.getHeight());
            dest = new Rect(src);
            dest.offset(offsetX, offsetY);
            wideBmpCanvas.drawBitmap(image, src, dest, null);
            mMap.addMarker(new MarkerOptions()
                    .position(to)
                    .icon(BitmapDescriptorFactory.fromBitmap(wideBmp))
                    .anchor(anchorX, anchorY));
        }
    }
    private double GetBearing(LatLng from, LatLng to){
        double lat1 = from.latitude * Math.PI / 180.0;
        double lon1 = from.longitude * Math.PI / 180.0;
        double lat2 = to.latitude * Math.PI / 180.0;
        double lon2 = to.longitude * Math.PI / 180.0;
        // Compute the angle.
        double angle = - Math.atan2( Math.sin( lon1 - lon2 ) * Math.cos( lat2 ),
                Math.cos( lat1 ) * Math.sin( lat2 ) - Math.sin( lat1 )
                            * Math.cos( lat2 ) * Math.cos(  lon1 - lon2 ) );
        if (angle < 0.0)
            angle += Math.PI * 2.0;
        // And convert result to degrees.
        angle = angle * degreesPerRadian;
        return angle;
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
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, @DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(context, R.drawable.ic_baseline_arrow_upward_24);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}