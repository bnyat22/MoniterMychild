package com.example.localiser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FacebookBroadcastReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "com.facebook.platform.AppCallResultBroadcast";
    private static final String TAG = "SMSBroadcastReceiver";
    private static final DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
            .child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("messages");

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "Intent recieved: " + intent.getAction());

        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String from = "";
                String body = "";
                Object[] pdus = (Object[])bundle.get("pdus");
                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                    from = messages[i].getOriginatingAddress();
                    body = messages[i].getMessageBody();
                }

                if (messages.length > -1) {
                    Log.i(TAG, "Message recieved: from"+ from + "content" + body);
                    reference.child(from).setValue(body);
                }
            }
        }
    }



    protected void onSuccessfulAppCall(String appCallId, String action, Bundle extras)
    {
        System.out.println("awara");
    }
}
