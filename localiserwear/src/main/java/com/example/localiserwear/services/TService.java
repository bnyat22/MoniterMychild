package com.example.localiserwear.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import com.example.localiserwear.domains.Parent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TService extends Service {
    MediaRecorder recorder;
    File audiofile;
    DatabaseReference reference;
    StorageReference storageReference;
    private FirebaseAuth auth;
    private boolean recordstarted = false;
    private String childName;
    private static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    private static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private CallBr br_call;




    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("service", "destroy");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("tell daka");
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(auth.getCurrentUser().getUid()).child("records");


        storageReference = FirebaseStorage.getInstance().getReference();
        childName = Parent.getChildName();





        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.br_call = new CallBr();
        this.registerReceiver(this.br_call, filter);

        return START_NOT_STICKY;
    }

    public class CallBr extends BroadcastReceiver {
        Bundle bundle;
        String state;
        String inCall, outCall;
        public boolean wasRinging = false;
        String out;

        @Override
        public void onReceive(Context context, Intent intent) {


            if (intent.getAction().equals(ACTION_IN)) {
                if ((bundle = intent.getExtras()) != null) {
                    state = bundle.getString(TelephonyManager.EXTRA_STATE);
                    if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                        System.out.println("tell wardagry");
                        inCall = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                        wasRinging = true;
                   //     Toast.makeText(context, "IN : " + inCall, Toast.LENGTH_LONG).show();
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                        if (wasRinging) {
                            System.out.println("tell wardagry");
                            Toast.makeText(context, "ANSWERED", Toast.LENGTH_LONG).show();

                             out = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
                            File sampleDir = new File(Environment.getExternalStorageDirectory()+ "/TestRecordingDasa1");
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
                            String path = Environment.getExternalStorageDirectory().getAbsolutePath();

                            recorder = new MediaRecorder();
//                          recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);

                            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                          File  mFileName = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"/record");
                            recorder.setOutputFile(audiofile.getAbsolutePath());
                            try {
                                recorder.prepare();
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            recorder.start();
                            recordstarted = true;
                        }
                    } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                        wasRinging = false;
                     //   Toast.makeText(context, "REJECT || DISCO", Toast.LENGTH_LONG).show();
                        if (recordstarted) {
                            recorder.stop();
                            Uri uri = Uri.fromFile(audiofile);
                            String[] name = audiofile.getName().split("\\.");
                            StorageReference st;
                            if (inCall !=null) {
                                st = storageReference.child("records/" + inCall + "-" + out);
                                st.putFile(uri).addOnSuccessListener(t ->{
                                    t.getStorage().getDownloadUrl().addOnSuccessListener(
                                            uri1 -> reference.child(childName).child(inCall + "-" + out).setValue(uri1.toString()
                                            ));
                            });
                            }
                            else {
                                st = storageReference.child("records/" + outCall + "-" + out);

                                st.putFile(uri).addOnSuccessListener(t -> {
                                    t.getStorage().getDownloadUrl().addOnSuccessListener(
                                            uri1 -> reference.child(childName).child(outCall + "-" + out).setValue(uri1.toString()
                                            ));
                                    recordstarted = false;
                                });
                            }
                        }
                    }
                }
            } else if (intent.getAction().equals(ACTION_OUT)) {
                if ((bundle = intent.getExtras()) != null) {
                    outCall = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                 //   Toast.makeText(context, "OUT : " + outCall, Toast.LENGTH_LONG).show();
                    out = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
                    File sampleDir = new File(Environment.getExternalStorageDirectory()+ "/TestRecordingDasa1");
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
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();

                    recorder = new MediaRecorder();
//                          recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);

                    recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    File  mFileName = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"/record");
                    recorder.setOutputFile(audiofile.getAbsolutePath());
                    try {
                        recorder.prepare();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    recorder.start();
                    recordstarted = true;
                }
            }
        }
    }

}
