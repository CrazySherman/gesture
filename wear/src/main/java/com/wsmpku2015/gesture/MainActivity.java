package com.wsmpku2015.gesture;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.hardware.SensorEventListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;

import java.util.ArrayList;
public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener {

    GoogleApiClient googleClient;
    private static final float NS2MS = 1.0f / 1000000.0f;
    private TextView text;
    private Button button;
    private Button send;
    private boolean start;
    private ArrayList acl_data;
    private ArrayList gy_data;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private SensorManager sm;
    private Drawable bg;
    private boolean ready;
    private long timestamp;
    //private SendToDataLayerThread thread = new SendToDataLayerThread("/message_path");
    //public TextView sampleRate;
    //private long count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //chores initialize
        acl_data = new ArrayList();
        gy_data = new ArrayList();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        start = true;
        ready = false;
        //register sensor
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
        //count = 0;

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                text = (TextView) stub.findViewById(R.id.text);
                button = (Button)stub.findViewById(R.id.button);
                send = (Button)stub.findViewById(R.id.send);
                text.setText("Hello world");
                //sampleRate = (TextView)stub.findViewById(R.id.srate);
                //sampleRate.setText(accelerometer.getMinDelay() + "us");
                button.setText("Start");
                button.setOnClickListener(new View.OnClickListener(){

                    public synchronized void onClick(View v) {
                        if (start) {
                            acl_data.clear();
                            gy_data.clear();
                            button.setText("Stop");
                            start = false;
                            //count = 0;
                            bg = button.getBackground();
                            button.setBackgroundColor(Color.RED);
                            text.setText("Hello World");
                            timestamp = 0;
                        }
                        else {
                            button.setText("Start");
                            button.setBackground(bg);
                            text.setText(acl_data.size() + "/" + gy_data.size() + ": ready");
                            start = true;
                        }
                    }
                });

                send.setOnClickListener(new View.OnClickListener(){
                    public synchronized void onClick(View v) {
                        if (acl_data.isEmpty() || !start) {
                            text.setText("no data to send");
                            return;
                        }
                        ready = true;
                        //new SendToDataLayerThread("/message_path", json.toString()).start();
                        text.setText("data sended");
                    }

                });

            }
        });
        //google api message exchange
        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (!googleClient.isConnected())
            googleClient.connect(); //temporarily add it here
    }
    @Override
    protected void onStop() {
        if (null != googleClient && googleClient.isConnected()) {
            googleClient.disconnect();
        }
        super.onStop();
    }
    @Override
    public void onConnected(Bundle bundle) {
        String msg = "connection initiated";
        //Requires a new thread to avoid blocking the UI
        new SendToDataLayerThread("/message_path").start();
        //thread.start();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("myTag", "connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("myTag","failed to connect");
    }
    @Override

    public synchronized void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        //text.setText("X: " + event.values[0] + "\nY: " + event.values[1] + "\nZ: " + event.values[2]);
        if (!start) {
            //count++;
            Sensor sensor = event.sensor;
            double delta = 0;
            if (timestamp == 0) {
                timestamp = event.timestamp;
            }
            else {
                delta = (event.timestamp - timestamp) * NS2MS;
            }

            if (sensor.equals(accelerometer)) {
                acl_data.add(delta);
                acl_data.add(event.values[0]);
                acl_data.add(event.values[1]);
                acl_data.add(event.values[2]);
            }
            else if (sensor.equals(gyroscope)) {
                gy_data.add(delta);
                gy_data.add(event.values[0]);
                gy_data.add(event.values[1]);
                gy_data.add(event.values[2]);
            }
        }
        /*if (start) {
            X[count] = event.values[0];
            Y[count] = event.values[1];
            Z[count] = event.values[2];
            count++;
            if (count >= max) {
                start = false;
                bt.setText("Start");
                saveCount = count;
                count = 0;
            }
            tv.setText("X: " + X + "\nY: " + Y + "\nZ: " + Z);
            message.add(event.values[0]);
            message.add(event.values[1]);
            message
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        Log.v("mySensor", "sensor accuracy changes!");
    }

    class SendToDataLayerThread extends Thread {
        String path;
        boolean first;            //is it initiate or not
        String message;
        // Constructor to send a message to the data layer
        SendToDataLayerThread(String p) {
            path = p;
            first = true;
        }
       /* public void setMsg(String msg) {
            message = msg;
        }*/
        @Override
        public void run() {

            while (googleClient.isConnected()) {
                if (first || ready) {
                    if (first) {
                        first = false;
                        message = "connection initiated";
                    } else if (ready && !acl_data.isEmpty()) {
                        Log.v("myTag", "here");
                        ready = false;
                        JSONArray json = new JSONArray();
                        json.put(acl_data);
                        json.put(gy_data);
                        message = json.toString();
                        acl_data.clear();
                        gy_data.clear();
                    }
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
                    for (Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
                        if (result.getStatus().isSuccess()) {
                            Log.v("myTag", "Message " + message.substring(0,10) + " sent to: " + node.getDisplayName());

                        } else {
                            // Log an error
                            Log.v("myTag", "ERROR: failed to send Message");
                        }
                    }
                }
                try {
                    sleep(200L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
