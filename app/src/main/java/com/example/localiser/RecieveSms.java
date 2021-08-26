package com.example.localiser;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.android.internal.net.VpnProfile;
import com.example.localiser.domains.MyLocation;
import com.example.localiser.domains.Parent;
import com.google.android.gms.common.internal.IResolveAccountCallbacks;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.mms.util_alt.SqliteWrapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RecieveSms extends BroadcastReceiver {
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String MMS_RECEIVED = "android.provider.Telephony.MMS_RECEIVED";
    private static final String TAG = "SMSBroadcastReceiver";
    private static final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("messages");
    private static final DatabaseReference referenceChild = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("children");
    private static MediaRecorder recorder;
    private static File audiofile;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Intent recieved: " + intent.getAction());
        System.out.println("daya");
        @SuppressLint("HardwareIds") String actuelId = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        String parentId = ((Parent) context.getApplicationContext()).getParentId();

        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                abortBroadcast();
                String from = "";
                String body = "";
                Object[] pdus = (Object[]) bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    from = messages[i].getOriginatingAddress();
                    body = messages[i].getMessageBody();
                }

                System.out.println("msher" + actuelId + " dilo" + parentId);
                if (!actuelId.equals(parentId)) {
                    if (body.contains("Envoie moi ta position")) {
                        Intent intenti = new Intent(context.getApplicationContext(), RecieveSms.class);
                        PendingIntent pi = PendingIntent.getActivity(context.getApplicationContext(), 0, intenti, 0);
                        SmsManager smsManager = SmsManager.getDefault();

                        String childName = ((Parent) context.getApplicationContext()).getChildName();
                        double lat = ((Parent) context.getApplicationContext()).getLat();
                        double longt = ((Parent) context.getApplicationContext()).getLongt();


                        abortBroadcast();
                        smsManager.sendTextMessage("0605668937", null,
                                childName + "," + "lat," + lat + ",long," + longt, pi, null);
                    } else if (body.contains("Le parent commence à écouter"))
                    {
                        System.out.println("gedagry");
                      startRecording();

                    } else if(body.contains("le parent s'arrête à ècouter")) {
                        try {
                            stopRecording(context);
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }

                    }
                    else {

                        String finalFrom = from;
                        String finalBody = body;
                        referenceChild.addListenerForSingleValueEvent(new ValueEventListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                System.out.println("wll" + snapshot.getKey());
                                snapshot.getChildren().forEach((ds) -> {
                                    if (actuelId.equals(ds.child("id").getValue())) {

                                        Log.i(TAG, "Message recieved: from" + finalFrom + "content" + finalBody);
                                        reference.child(ds.getKey()).child(finalFrom).setValue(finalBody);

                                    }

                                });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }


                        });
                    }
                } else {
                        if (messages.length > -1) {
                            Log.i(TAG, "Message recieved: from" + from + "content" + body);
                            if (body.contains("lat") && body.contains("long")) {
                                String[] contents = body.split(",");
                                String childName = contents[0];
                                String latitude = contents[2];
                                String longitude = contents[4];
                                Location location = new Location("fused");
                                location.setLatitude(Double.parseDouble(latitude));
                                location.setLongitude(Double.parseDouble(longitude));

                                referenceChild.child(childName).child("locations").setValue(location);

                            } else if (body.contains("Je n'ai plus de réseaux")) {
                                String[] contents = body.split(",");

                                referenceChild.child(contents[0]).child("network").setValue("false");
                            }

                        }
                    }

                }
            } else if (intent.getAction().equals(MMS_RECEIVED))
        {
            System.out.println("detn");
        }
        }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopRecording(Context context) throws MalformedURLException {
        recorder.stop();

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        sendIntent.setPackage("com.android.mms");
        sendIntent.putExtra("address", "0605668937");
        sendIntent.putExtra("sms_body", "voila");




       /* com.klinker.android.send_message.Settings settings =
                new com.klinker.android.send_message.Settings();

        settings.setUseSystemSending(true);
        Transaction transaction = new Transaction(context, settings);
        Message message = new Message("textToSend", "0605668937");
        message.setAudio(audiofile.getAbsolutePath().getBytes(StandardCharsets.UTF_8));
        message.setFromAddress(Utils.getMyPhoneNumber(context));
        message.setSave(false);
        Uri uri = Uri.parse("content://mms-sms/conversations/");
        message.setMessageUri(uri);

        transaction.sendNewMessage(message, 123);*/

        Uri uri = FileProvider.getUriForFile(
                context,
                "com.example.localiser.RecieveSms.provider",
                audiofile);

        Log.e("Path", "" + uri);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("audio/*");

        Intent intenti = new Intent(context.getApplicationContext(), RecieveSms.class);
        PendingIntent pi = PendingIntent.getActivity(context.getApplicationContext(), 0, intenti, 0);
      SmsManager smsManager = SmsManager.getDefault();

        context.startActivity(sendIntent);






    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void startRecording()
        {

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
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
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
    }

