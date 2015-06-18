package com.wsmpku2015.gesture;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.*;
import android.support.v4.content.*;

/**
 * Created by wsm on 4/14/15.
 */
public class ListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals("/message_path")) {
            final String message = new String(messageEvent.getData());
            Log.v("myTag", "Message path received on watch is: " + messageEvent.getPath());
            Log.v("myTag", "Message received on watch is: " + message);
            // Broadcast message to wearable activity for display
            Intent messageIntent = new Intent();
            messageIntent.setAction(Intent.ACTION_SEND);
            messageIntent.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(messageIntent);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}
