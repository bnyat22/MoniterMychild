package com.example.localiser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.ServiceCompat;

import com.example.localiser.domains.Parent;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GpsTracker extends Service {

    //  private final Context mContext;

    //
    //private GoogleApi googleApi;
    private NotificationManager notificationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private FirebaseDatabase database;
    private DatabaseReference refAuth , reference, polyLineRef;
    private FirebaseAuth auth;
    private String actuelId;
    private String parentId;




    // Declaring a Location Manager
    protected LocationManager locationManager;
   private Notification manque ;



    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {

auth = FirebaseAuth.getInstance();
        System.out.println("xo deya era bram?");
        database = FirebaseDatabase.getInstance();
        parentId = ((Parent) this.getApplication()).getParentId();
        //parentId = refAuth.child("deviceId")
        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);

        reference = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("locations");

        polyLineRef = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("polyline");



        //  getLocationUpdate();
        System.out.println("parent " + parentId);
        if (!actuelId.equals(parentId)) {
            System.out.println("Mandy ");
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            buildLocationRequest();
            buildLocationCallBack();
            getLocationFused();
        }
    }
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(3);
    }
    //Build the location callback object and obtain the location results //as demonstrated below:
    private void buildLocationCallBack() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                System.out.println("ddeya era bram?");
                reference.setValue(locationResult.getLastLocation());

                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH:mm").format(Calendar.getInstance().getTime());
                String[] dates = timeStamp.split("_");
                polyLineRef.child(dates[0]).child(dates[1]).setValue(locationResult.getLastLocation());
            }
        };
    }
    public GpsTracker() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        createNotificationChannel();
        Intent notificationIntent = new Intent(this, Home.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this,"12")
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("My Awesome App")
                .setContentText("Doing some work...")
                .setContentIntent(pendingIntent).build();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1337, notification);
        }
        super.onStartCommand(intent, flags, startId);
        System.out.println("esh daka");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }

    }

    public void getLocationFused() {
        Toast.makeText(getApplicationContext(), "getLocation", Toast.LENGTH_LONG).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //request the last location and add a listener to get the response. then update the UI.
            fusedLocationProviderClient.requestLocationUpdates(locationRequest , locationCallback , Looper.myLooper());
        } else {
            Toast.makeText(getApplicationContext(), "getLocation ERROR", Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("12", "service", importance);
            //  NotificationChannel channel2 = new NotificationChannel("2", "manque", importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            // notificationManager.createNotificationChannel(channel2);
        }
    }

}

