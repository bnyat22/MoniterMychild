package com.example.localiser;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.example.localiser.domains.MyLocation;
import com.example.localiser.domains.Parent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class Home extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    //Drawer elements
    private DrawerLayout drawerLayout;
   private NavigationView navigationView;
   private Toolbar toolbar;

   //firebase
private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference reference , polygonRef , specificRef , refAuth;
    private String actuelId , parentId;

    //notifications
    NotificationCompat.Builder dangereux;
    NotificationCompat.Builder manque;
    NotificationManagerCompat notificationManagerCompat;

   //map elements
    private GoogleMap mMap;
    private LocationManager manager;
    private final int MIN_TIME = 1000; //une seconde
    private final int MIN_DIS = 1; //un mêtre
    private Marker myMarker;
    private static final  int REQUEST_CODE_PERMISSION =2;
    String mPermission = android.Manifest.permission.ACCESS_FINE_LOCATION;
    String cPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
    String bPermission = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
   private LatLng startLatlng;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_acivity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.topAppBar);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START); });


        //notifications
        createNotificationChannel();
        Intent intent = new Intent(this, ParlerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        dangereux = new NotificationCompat.Builder(this , "1")
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("Attention")
                .setContentText("Votre enfant est à un endroit dangreux")
        .setPriority(NotificationCompat.PRIORITY_HIGH);

        manque = new NotificationCompat.Builder(this , "1")
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("Attention")
                .setContentText("Votre enfant n'est pas à l'endroit supposé")
        .setPriority(NotificationCompat.PRIORITY_HIGH);
         notificationManagerCompat = NotificationManagerCompat.from(this);

        //getting permissions
        try{
            if (ActivityCompat.checkSelfPermission(this,mPermission)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,cPermission)!= PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,bPermission)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{mPermission , cPermission , bPermission},REQUEST_CODE_PERMISSION);

            } else
            {
                startForegroundService(new Intent(this, GpsTracker.class));

            }
        }catch (Exception e){
            e.printStackTrace();
        }


//initialiser firebase elements
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        parentId = ((Parent) this.getApplication()).getParentId();
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);
        reference = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("locations");
        polygonRef = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("polygons");
        specificRef = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("specific");



        //   getLocationGpsTracker();
      //  getLocationUpdate();

        startService(new Intent(this, GpsTracker.class));
onReadChanges();


    }


    @Override
    protected void onPause() {
        super.onPause();
        startForegroundService(new Intent(this, GpsTracker.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, GpsTracker.class));
    }

    private void onReadChanges() {
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyLocation location;
                if (snapshot.exists()) {
                    try {
                        location = snapshot.getValue(MyLocation.class);
                        System.out.println("erukana" + location.getLatitude());


                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 25.0f));


                        myMarker.setPosition(new LatLng(location.getLatitude() , location.getLongitude()));
                    } catch (Exception e)
                    {
                      //  Toast.makeText(Home.this , "7aji", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

  /*  private void getLocationUpdate() {
        if (manager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIS, (LocationListener) this);
                    System.out.println("gaishta era 3");
                } else if (manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                {
                    System.out.println("gaishta era 4");
                    manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER , MIN_TIME,MIN_DIS , (LocationListener) this);
                } else {
                    Toast.makeText(Home.this , "Les services de gps et network ne sont pas disponible" , Toast.LENGTH_LONG).show();
                }

            } else
                ActivityCompat.requestPermissions(this , new String []{Manifest.permission.ACCESS_FINE_LOCATION},103);

        }
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startForegroundService(new Intent(this, GpsTracker.class));
        } else
        {
            Toast.makeText(this , "Permission demandé" , Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        getLastLocation();

        /*   myMarker = mMap.addMarker(new MarkerOptions().position(startLatlng).title("Marker in Montpellier"));
           mMap.getUiSettings().setAllGesturesEnabled(true);
           mMap.getUiSettings().setZoomControlsEnabled(true);*/

        Query query = polygonRef;
        Query specifiQuery = specificRef;
       queryNormalPolygon(query);
        querySpecific(specifiQuery);

    //   mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatlng));
    }

    private void getLastLocation() {
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyLocation location;
                if (snapshot.exists())
                {
                    location = snapshot.getValue(MyLocation.class);
                    System.out.println("e bram da wara era " + location.getLatitude());
                    startLatlng = new LatLng(location.getLatitude() , location.getLongitude());
                    myMarker = mMap.addMarker(new MarkerOptions().position(startLatlng).title("Marker in Montpellier"));
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void querySpecific(Query query)
    {

        ValueEventListener queryValueListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Polygon polygon;
                    //get latitute and longitude
                    PolygonOptions polygonOptions = new PolygonOptions();
                    List<Object> points = (List<Object>) ds.child("polygon").child("points").getValue();
                    for (int i = 0; i < points.size(); i++) {
                        HashMap<String, Double> data = (HashMap<String, Double>) points.get(i);
                        double latitude = data.get("latitude");
                        double longitude = data.get("longitude");
                        LatLng latLng = new LatLng(latitude , longitude);
                        polygonOptions.add(latLng);
                    }
                    polygon =    mMap.addPolygon(polygonOptions);
                    polygon.setFillColor(ds.child("polygon").child("fillColor").getValue(Integer.class));
                    polygon.setStrokeColor(ds.child("polygon").child("strokeColor").getValue(Integer.class));
                    String from = ds.child("from").getValue(String.class);
                    String to = ds.child("to").getValue(String.class);
                    specificDataChange(polygon , from ,to);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(queryValueListener);
    }
    private void queryNormalPolygon(Query query)
    {
        ValueEventListener queryValueListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    Polygon polygon;
                    //get latitute and longitude
                    PolygonOptions polygonOptions = new PolygonOptions();
                    List<Object> points = (List<Object>) ds.child("points").getValue();
                    for (int i = 0; i < points.size(); i++) {
                        HashMap<String, Double> data = (HashMap<String, Double>) points.get(i);
                        double latitude = data.get("latitude");
                        double longitude = data.get("longitude");
                        LatLng latLng = new LatLng(latitude , longitude);
                        polygonOptions.add(latLng);
                    }
                    polygon =    mMap.addPolygon(polygonOptions);
                    polygon.setFillColor(ds.child("fillColor").getValue(Integer.class));
                    polygon.setStrokeColor(ds.child("strokeColor").getValue(Integer.class));

                    referenceDataChange(polygon);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addListenerForSingleValueEvent(queryValueListener);
    }
    private void specificDataChange(Polygon polygon , String from, String to)
    {

        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyLocation location;

                if (snapshot.exists()) {
                    try {

                        location = snapshot.getValue(MyLocation.class);
                        LatLng latLng= new LatLng(location.getLatitude() , location.getLongitude());
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            builder.include(latLng);
                        final LatLngBounds bounds = builder.build();

                        //BOUND_PADDING is an int to specify padding of bound.. try 100.
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,100);
                        mMap.animateCamera(cu);
                        System.out.println("rasta ");
                        System.out.println("rasta p" + pointInPolygon(latLng, polygon));

                        if (pointInPolygon(latLng , polygon) && isTimeBetween(from , to))
                            System.out.println("laweya");
                        else if (pointInPolygon(latLng , polygon) && !isTimeBetween(from , to))
                            System.out.println("laweya");
                        else if (!pointInPolygon(latLng , polygon) && isTimeBetween(from , to))
                            notificationManagerCompat.notify(2 , manque.build());

                    } catch (Exception e)
                    {
                        Toast.makeText(Home.this , e.getMessage() , Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private boolean isTimeBetween(String from , String to)
    {
        try {

            @SuppressLint("SimpleDateFormat") Date time1 = new SimpleDateFormat("HH:mm").parse(from);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);
            calendar1.add(Calendar.DATE, 1);

System.out.println("waxt");
System.out.println("waxt  " + from);
            @SuppressLint("SimpleDateFormat") Date time2 = new SimpleDateFormat("HH:mm").parse(to);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
            calendar2.add(Calendar.DATE, 1);


            String[] time =    LocalDateTime.now().toString().split("T");
            String [] timePrecis = time[1].split("\\.");
            @SuppressLint("SimpleDateFormat") Date date =  new SimpleDateFormat("HH:mm").parse(timePrecis[0]);
            Calendar calendar3 = Calendar.getInstance();
            calendar3.setTime(date);
            calendar3.add(Calendar.DATE, 1);

            Date x = calendar3.getTime();
            System.out.println(x.getTime());
            //  String d = new SimpleDateFormat("HH:mm");

            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime())) {
                //checkes whether the current time is between 14:49:00 and 20:11:13.
              return true;
            } else
                return false;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
    private void referenceDataChange(Polygon polygon)
    {
        reference.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                MyLocation location;

                if (snapshot.exists()) {
                    try {
                        location = snapshot.getValue(MyLocation.class);
                        LatLng latLng= new LatLng(location.getLatitude() , location.getLongitude());
                        if (pointInPolygon(latLng , polygon))
                            notificationManagerCompat.notify(1,dangereux.build());
                        else
                            System.out.println("asaeya");
                    } catch (Exception e)
                    {
                        Toast.makeText(Home.this , e.getMessage() , Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public boolean pointInPolygon(LatLng point, Polygon polygon) {
        // ray casting alogrithm http://rosettacode.org/wiki/Ray-casting_algorithm
        int crossings = 0;
        List<LatLng> path = polygon.getPoints();
        path.remove(path.size()-1); //remove the last point that is added automatically by getPoints()

        // for each edge
        for (int i=0; i < path.size(); i++) {
            LatLng a = path.get(i);
            int j = i + 1;
            //to close the last edge, you have to take the first point of your polygon
            if (j >= path.size()) {
                j = 0;
            }
            LatLng b = path.get(j);
            if (rayCrossesSegment(point, a, b)) {
                crossings++;
            }
        }

        // odd number of crossings?
        return (crossings % 2 == 1);
    }

    public boolean rayCrossesSegment(LatLng point, LatLng a,LatLng b) {
        // Ray Casting algorithm checks, for each segment, if the point is 1) to the left of the segment and 2) not above nor below the segment. If these two conditions are met, it returns true
        double px = point.longitude,
                py = point.latitude,
                ax = a.longitude,
                ay = a.latitude,
                bx = b.longitude,
                by = b.latitude;
        if (ay > by) {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }
        // alter longitude to cater for 180 degree crossings
        if (px < 0 || ax <0 || bx <0) { px += 360; ax+=360; bx+=360; }
        // if the point has the same latitude as a or b, increase slightly py
        if (py == ay || py == by) py += 0.00000001;


        // if the point is above, below or to the right of the segment, it returns false
        if ((py > by || py < ay) || (px > Math.max(ax, bx))){
            return false;
        }
        // if the point is not above, below or to the right and is to the left, return true
        else if (px < Math.min(ax, bx)){
            return true;
        }
        // if the two above conditions are not met, you have to compare the slope of segment [a,b] (the red one here) and segment [a,p] (the blue one here) to see if your point is to the left of segment [a,b] or not
        else {
            double red = (ax != bx) ? ((by - ay) / (bx - ax)) : Double.POSITIVE_INFINITY;
            double blue = (ax != px) ? ((py - ay) / (px - ax)) : Double.POSITIVE_INFINITY;
            return (blue >= red);
        }

    }
    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }




    private void createNotificationChannel() {

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel("1", "xatar", importance);
        channel.setShowBadge(true);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.enableVibration(true);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        // notificationManager.createNotificationChannel(channel2);
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

