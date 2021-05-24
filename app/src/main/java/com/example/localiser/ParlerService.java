package com.example.localiser;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


import com.example.localiser.domains.Parent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ParlerService extends Service {

StorageReference storageReference;
    File audiofile;
    MediaRecorder recorder;
    private FirebaseDatabase database;
    private DatabaseReference refAudio , reference ,ischild;
    private FirebaseAuth auth;
    private String actuelId;
    private String parentId;
    private String audioUri;




    // Declaring a Location Manager
    protected LocationManager locationManager;
    private Notification manque ;



    @SuppressLint("HardwareIds")
    @Override
    public void onCreate() {
        storageReference = FirebaseStorage.getInstance().getReference();
        audioUri = "bnyad";
        auth = FirebaseAuth.getInstance();
        System.out.println("xo deya era bram?");
        database = FirebaseDatabase.getInstance();
        parentId = ((Parent) this.getApplication()).getParentId();
        //parentId = refAuth.child("deviceId")
        actuelId = Settings.Secure.getString(getContentResolver() , Settings.Secure.ANDROID_ID);

        reference = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("parler").child("etat");
        refAudio = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("parler").child("audio");
        ischild = database.getReference().child("Users").child(auth.getCurrentUser().getUid()).child("parler").child("isChild");

reference.setValue("7");
refAudio.setValue("bnyad");
ischild.setValue("");


    }

    private void stopRecording(String aTrue) {
        recorder.stop();
        Uri uri = Uri.fromFile(audiofile);
        String[] name = audiofile.getName().split("\\.");
        StorageReference st = storageReference.child("call/" + audiofile.getName());
        st.putFile(uri).addOnSuccessListener(t ->{
            t.getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> {
             refAudio.setValue(uri1.toString());
            reference.setValue("10");
            ischild.setValue(aTrue);
            });

    });}
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private void childRecord()
 {
     File sampleDir = new File(Environment.getExternalStorageDirectory()+ "/TestRecord");
     if (!sampleDir.exists()) {
         System.out.println("file aka nya");
         sampleDir.mkdir();
     }
     String file_name = "Record";
     try {
         audiofile = File.createTempFile(file_name, ".amr", sampleDir);
     } catch (IOException e) {
         e.printStackTrace();
     }
     recorder = new MediaRecorder();
//                          recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);

     recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
     recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
     recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
     //     File mFileName = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"/record");
     recorder.setOutputFile(audiofile.getAbsolutePath());
     try {
         recorder.prepare();
     } catch (IllegalStateException e) {
         e.printStackTrace();
     } catch (IOException e) {
         e.printStackTrace();
     }
     recorder.start();
 }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String result = snapshot.getValue(String.class);
                if (result.equals("1")) {
                    if (!actuelId.equals(parentId))
                        childRecord();
                } else if (result.equals("2"))
                {
                    if (actuelId.equals(parentId))
                        childRecord();
                } else if (result.equals("3")) {
                    if (!actuelId.equals(parentId)) {
                        stopRecording("false");

                    }
                }else if (result.equals("4")) {
                    if (actuelId.equals(parentId)) {
                        stopRecording("true");
                    }
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


refAudio.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.exists()) {
          audioUri = snapshot.getValue(String.class);

          }
        }


    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});
 ischild.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!audioUri.startsWith("bnyad")) {
                String isEnfant = snapshot.getValue(String.class);
              MediaPlayer  mediaPlayer = MediaPlayer.create(ParlerService.this, Uri.parse(audioUri));
                if (isEnfant.equals("false") && actuelId.equals(parentId)) {

                    System.out.println("7aji qsakay mn rasta " + parentId + " :::: " + actuelId);
                    mediaPlayer.start();
                    ischild.setValue("");
                } else if (isEnfant.equals("true") && !actuelId.equals(parentId)) {
                    System.out.println("wala boy demar " + parentId + " ::::" + actuelId);
                    mediaPlayer.start();
                    ischild.setValue("");
                }
            }
        }
        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
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





}


