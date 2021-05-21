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
import android.database.ContentObserver;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;


import com.example.localiser.domains.Parent;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class BrowserService extends Service {

    //  private final Context mContext;

    //
    //private GoogleApi googleApi;
    StorageReference storageReference;
    File audiofile;
    MediaRecorder recorder;
    private FirebaseDatabase database;
    private DatabaseReference refAuth , reference, polyLineRef;
    private FirebaseAuth auth;
    private String actuelId;
    private String parentId;

    private static String CHROME_BOOKMARKS_URI =
            "content://com.android.chrome.browser/bookmarks";


    // Declaring a Location Manager
    protected LocationManager locationManager;
    private Notification manque ;



    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        System.out.println("xo deya era bram?");
        database = FirebaseDatabase.getInstance();
        parentId = ((Parent) this.getApplication()).getParentId();
        //parentId = refAuth.child("deviceId")
        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);
        reference = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("parler");

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ChromeOberver observer = new ChromeOberver(new Handler());
        getContentResolver().registerContentObserver(Uri.parse(CHROME_BOOKMARKS_URI), true, observer);
        super.onStartCommand(intent, flags, startId);
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

    class ChromeOberver extends ContentObserver {

        public ChromeOberver(Handler handler) {
            super(handler);
            System.out.println("dana");

        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange);
            Log.d("e", "onChange: " + selfChange);
            System.out.println("hat hat");
            @SuppressLint("Recycle") Cursor cursor = BrowserService.this.getContentResolver()
                    .query(Uri.parse(CHROME_BOOKMARKS_URI),new String[]{"title" , "uri"},
                            "bookmark = 0", null, null);
            // process cursor results
        }

    }



}


