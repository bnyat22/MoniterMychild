package com.example.localiserwear.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


import com.example.localiserwear.ParlerActivity;
import com.example.localiserwear.R;
import com.example.localiserwear.domains.Parent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SecouerService extends Service implements SensorEventListener {
    private static final int SHAKE_THRESHOLD = 2500;
    SensorManager sensorManager;
    NotificationManagerCompat notificationManagerCompat;
    private String actuelId;
    private String parentId;
    private DatabaseReference reference;

    Sensor sensor;
    NotificationCompat.Builder dangereux;
    float x , y ,z , last_x ,last_y , last_z;
    long lastUpdate;

    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener( this,sensor,
                SensorManager.SENSOR_DELAY_GAME);
        parentId = Parent.getParentId();
        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("danger");
        reference.setValue("");
        createNotificationChannel();
        Intent intent = new Intent(this, ParlerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
     dangereux = new NotificationCompat.Builder(this , "16")
                .setSmallIcon(R.drawable.ic_baseline_add_alert_24)
                .setContentTitle("Au secours")
                .setContentText("Je suis en danger")
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManagerCompat = NotificationManagerCompat.from(this);
        super.onCreate();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            x = event.values[0];
            y = event.values[1];
            z = event.values[2];

            float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;


            if (speed > SHAKE_THRESHOLD && !actuelId.equals(parentId)) {
                reference.setValue("1");
            }
        }
        last_x = x;
        last_y = y;
        last_z = z;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.getValue(String.class).equals(""))
                    if (actuelId.equals(parentId)) {
                        notificationManagerCompat.notify(16, dangereux.build());
                        reference.setValue("");
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_MAX;
            @SuppressLint("WrongConstant")
            NotificationChannel channel = new NotificationChannel("16", "service", importance);
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            // notificationManager.createNotificationChannel(channel2);
        }
    }
}
