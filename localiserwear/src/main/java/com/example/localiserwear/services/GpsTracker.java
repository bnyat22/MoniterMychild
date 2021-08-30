package com.example.localiserwear.services;


import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import com.example.localiserwear.Home;
import com.example.localiserwear.R;
import com.example.localiserwear.domains.Parent;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class GpsTracker extends Service {


    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private FirebaseDatabase database;
    private DatabaseReference refChild , reference, polyLineRef;
    private FirebaseAuth auth;
    private String actuelId;
    private String parentId;
    private String childName;
    private String parentTelNumber;
    private boolean sendMeesage;
  //  private Map<String , > locationList;

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {

auth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        parentId = Parent.getParentId();
        childName = Parent.getChildName();
        parentTelNumber = Parent.getParentTelNum();
        sendMeesage = false;


        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);

        if (auth.getCurrentUser().getUid() != null) {
            if (!parentId.equals(actuelId)) {
                reference = database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                        .child("children").child(childName).child("locations");
                refChild = database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                        .child("children").child(childName);

                polyLineRef = database.getReference().child("Users")
                        .child(auth.getCurrentUser().getUid())
                        .child("children").child(childName).child("polyline");
            }
        }


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
                Parent.setLat(locationResult.getLastLocation().getLatitude());
                Parent.setLongt(locationResult.getLastLocation().getLongitude());
                if (!checkNetworkConnectivity())
                {


                    if (!sendMeesage) {
                        Intent intent = new Intent(getApplicationContext(), GpsTracker.class);
                        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                        SmsManager smsManager = SmsManager.getDefault();


                        smsManager.sendTextMessage(parentTelNumber, null,
                                childName + "," + "Je n'ai plus de réseaux", pi, null);
                        sendMeesage = true;
                    }


                } else {
                    sendMeesage = false;
                    refChild.child("network").setValue("true");
                    reference.setValue(locationResult.getLastLocation());
                }
                @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HH:mm")
                        .format(Calendar.getInstance().getTime());
                String[] dates = timeStamp.split("_");
                polyLineRef.child(dates[0]).child(dates[1]).setValue(locationResult.getLastLocation());
            }
        };
    }
    public GpsTracker() {
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        Intent notificationIntent = new Intent(this, Home.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this,"12")
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("Mon Service")
                .setContentText("En train de travailler...")
                .setContentIntent(pendingIntent).build();
        startForeground(1337, notification);
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++)
        {
            Log.d("Executed app", "Application executed : " +recentTasks.get(i).baseActivity.toShortString()+ "\t\t ID: "+recentTasks.get(i).id+"");
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
     //   Toast.makeText(getApplicationContext(), "getLocation", Toast.LENGTH_LONG).show();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //request the last location and add a listener to get the response. then update the UI.
            fusedLocationProviderClient.requestLocationUpdates(locationRequest , locationCallback , Looper.myLooper());
        } else {
        //    Toast.makeText(getApplicationContext(), "getLocation ERROR", Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("12", "service", importance);

            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private boolean  checkNetworkConnectivity()
    {


        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
           return true;
        }
        else
         return    false;
    }


}

