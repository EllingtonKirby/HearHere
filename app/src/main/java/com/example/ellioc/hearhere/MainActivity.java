package com.example.ellioc.hearhere;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private AudioEngine audioEngine = null;
    final int PERMISSIONS_RECORD_AUDIO = 1;
    final int PERMISSIONS_WRITE_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!hasRecordAudioPermission()){
            requestRecordAudioPermission();
        }
        else if(!hasWriteExternalStoragePermission())
            requestWriteExternalStoragePermission();

        bindAudioRecord();

    }

    protected void onPause(){
        super.onPause();
        if(audioEngine != null) {
            audioEngine.stop_engine();
            audioEngine = null;
        }
    }

    protected void onResume(){
        super.onResume();
        if(audioEngine == null){
            bindAudioRecord();
        }
    }


    private boolean hasRecordAudioPermission(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);

        Log.i("Main Activity", "Has RECORD_AUDIO permission? " + hasPermission);
        return hasPermission;
    }

    private boolean hasWriteExternalStoragePermission(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        Log.i("Main Activity", "Has WRITE_EXTERNAL_STORAGE permission? " + hasPermission);
        return hasPermission;
    }

    private void requestRecordAudioPermission(){

        String requiredPermission = Manifest.permission.RECORD_AUDIO;

        // If the user previously denied this permission then show a message explaining why
        // this permission is needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(getApplicationContext(), "This app needs to record audio through the microphone....",
                    Toast.LENGTH_SHORT).show();
        }

        // request the permission.
        ActivityCompat.requestPermissions(this, new String[]{requiredPermission}, PERMISSIONS_RECORD_AUDIO);
    }

    private void requestWriteExternalStoragePermission(){
        String requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        // If the user previously denied this permission then show a message explaining why
        // this permission is needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getApplicationContext(), "This app needs to write to external storage....",
                    Toast.LENGTH_SHORT).show();
        }

        // request the permission.
        ActivityCompat.requestPermissions(this, new String[]{requiredPermission}, PERMISSIONS_WRITE_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {

        // This method is called when the user responds to the permissions dialog
        switch(requestCode){
            case PERMISSIONS_RECORD_AUDIO:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    bindAudioRecord();
                }
            }
            case PERMISSIONS_WRITE_STORAGE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
            }

        }
    }

    public void bindAudioRecord(){
        audioEngine = new AudioEngine();
        final Button button1 = (Button) findViewById(R.id.button1);
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setVisibility(View.INVISIBLE);
        button1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        audioEngine.start_engine();
                        button1.setVisibility(View.INVISIBLE);
                        button2.setVisibility(View.VISIBLE);
                    }
                }
        );
        button2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        audioEngine.stop_engine();
                        button1.setVisibility(View.VISIBLE);
                        button2.setVisibility(View.INVISIBLE);
                    }
                }
        );
    }
}
