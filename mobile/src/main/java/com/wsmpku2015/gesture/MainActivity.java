package com.wsmpku2015.gesture;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.*;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends Activity {
    private TextView text;
    private TextView logText;
    private Button set;
    private Button delete;
    //private String message;
    private User curUser;
    private int curGest;
    private UserList ulist;
    private boolean ready;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (TextView)findViewById(R.id.bigView);
        logText = (TextView)findViewById(R.id.log);
        text.setText("Hello world!");
        set = (Button)findViewById(R.id.set);
        delete = (Button)findViewById(R.id.delete);
        ulist = new UserList();
        ready = false;
        //message = "";
        // Register the local broadcast receiver, defined in step 3.
        IntentFilter messageFilter = new IntentFilter(Intent.ACTION_SEND);
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, messageFilter);
        /*save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!message.equals("")) {
                    EditText ed = (EditText)findViewById(R.id.editText);
                    if (writeFile(ed.getText().toString()))
                        text.setText("file saved successfully");
                    else
                        text.setText("something wrong with saving");
                    message = "";
                }
                else
                    text.setText("there is not shit!");
            }
        });*/
        delete.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                int rep = curUser.getRep(curGest);
                if (curUser.decRep(curGest)) {
                    logText.setText("rep " + rep + "deleted\n" + curUser.getRep(curGest) + "rep saved");
                    logText.setTextSize(logText.getTextSize()/2);
                    String folder = "U" + curUser.getUid() + "/";
                    String file = "U" + curUser.getUid() + "_" + curGest + "-" + rep;
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File acl_root = new File(path, folder + file + "-acl.txt");
                    if (acl_root.exists())
                        acl_root.delete();
                    File gy_root = new File(path,folder +  file + "-gy.txt");
                    if (gy_root.exists())
                        gy_root.delete();
                }
                else
                    logText.setText("no shit to delete");
            }
        });
        set.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText ed = (EditText)findViewById(R.id.utext);
                String val = ed.getText().toString();
                if (val.equals("")) return;
                ed = (EditText)findViewById(R.id.gtext);
                String val2 = ed.getText().toString();
                if (val2.equals(""))   return;
                curGest = Integer.valueOf(val2);
                int id = Integer.valueOf(val);
                curUser = ulist.find(id);
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() +
                        "/U" + curUser.getUid() + "/";

                File file = new File(path);
                String[] files = file.list();
                if (files != null) {
                    int curReps = 0;
                    String match = "U" + curUser.getUid() + "_" + curGest;
                    Log.v("myTag", match);
                    Log.v("myTag", path);
                    for (String i : files) {
                        if (i.startsWith(match))
                            curReps++;
                    }
                    curUser.setRep(curGest, curReps / 2);
                }
                ready = true;
                text.setText("current user: " + curUser.getUid() + "\n current gesture: " + curGest);
                text.setTextSize(text.getTextSize()/2);
                logText.setText(curUser.getRep(curGest) + "rep saved");
            }

        });


    }
    public class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            // Display message in UI
            if (message.equals("connection initiated"))
                logText.setText(message);
            else {
                saveMessage(message);
                /*if (saveMessage(message))
                    text.setText("file saved successfully");
                else
                    text.setText("something wrong when saving files");*/
            }
        }
    }
    /**
     * User ID: must be integer, better in order
     * Gesture ID: must be >=1 integer, better in order
     * */
    public boolean saveMessage(String message) {
        if (!ready) {
            logText.setText("plz setup user info");
            return false;
        }
        //may need to need SD card
        String folder = "U" + curUser.getUid() + "/";
        if (curUser.getRep(curGest) == -1) {
            logText.setText("Gesture type is wrong");
            return false;
        }
        if (curUser.addRep(curGest))
            logText.setText(curUser.getRep(curGest) + "rep saved");
        else {
            logText.setText("Gesture type is wrong");
            return false;
        }
        /*if (curUser.getRep(curGest) > 10) {
            Log.v("myTag", "more than 10 rep happened");
            curUser.decRep(curGest);
            return false;
        }*/
        String file = "U" + curUser.getUid() + "_" + curGest + "-" +
                String.valueOf(curUser.getRep(curGest));

        //setup file saving environment
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.v("myTag", "media not available");
            return false;
        }
        //initialize external file path
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File acl_root = new File(path, folder + file + "-acl.txt");
        if (acl_root.exists())
            acl_root.delete();
        File gy_root = new File(path,folder +  file + "-gy.txt");
        if (gy_root.exists())
            gy_root.delete();
        Log.v("myTag", acl_root.getPath());
        Log.v("myTag", gy_root.getPath());
        //json text
        double t, x, y, z;
        //Log.v("myTag",message);
        try {
            JSONArray json = new JSONArray(message);
            if (json.length() != 2)
                Log.v("myErrorTag", "json text error!");
            JSONArray acl = new JSONArray(json.get(0).toString());
            JSONArray gy = new JSONArray(json.get(1).toString());
            path.mkdir();
            File parent = acl_root.getParentFile();
            if (!parent.exists() && !parent.mkdir())
                throw new IllegalStateException("Couldn't create dir: " + parent);

            PrintWriter acl_pw, gy_pw;
            acl_pw = new PrintWriter(acl_root);
            gy_pw = new PrintWriter(gy_root);
            for (int i = 0; i < acl.length(); i += 4) {
                t = acl.getDouble(i);
                x = acl.getDouble(i + 1);
                y = acl.getDouble(i + 2);
                z = acl.getDouble(i + 3);

                acl_pw.println(t + " " + x + " " + y + " " + z + "\n");
            }
            acl_pw.close();
            for (int i = 0; i < gy.length(); i += 4) {
                t = gy.getDouble(i);
                x = gy.getDouble(i + 1);
                y = gy.getDouble(i + 2);
                z = gy.getDouble(i + 3);
                gy_pw.println(t + " " + x + " " + y + " " + z + "\n");
            }
            gy_pw.close();
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

}




