package com.example.ellioc.hearhere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int LEFT_CALIBRATION = 14;
    private int RIGHT_CALIBRATION = -17;
    final int PERMISSIONS_RECORD_AUDIO = 1;
    final int PERMISSIONS_WRITE_STORAGE = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if(!hasWriteExternalStoragePermission())
            requestWriteExternalStoragePermission();
        if(!hasRecordAudioPermission())
            requestRecordAudioPermission();
        else{
            bindButtons();
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
                    bindButtons();
                }
            }
            case PERMISSIONS_WRITE_STORAGE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //TODO implement logging functionality to make logging a settable option
                }
            }

        }
    }


    private void bindButtons(){
        Button calibration = (Button) findViewById(R.id.calibrate);
        Button startAudioEngine = (Button) findViewById(R.id.startAudioEngine);

        assert calibration != null;
        assert startAudioEngine != null;

//        startAudioEngine.setVisibility(View.INVISIBLE);
        startAudioEngine.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, GameActivity.class);
                        myIntent.putExtra("left_calibration", LEFT_CALIBRATION);
                        myIntent.putExtra("right_calibration", RIGHT_CALIBRATION);
                        MainActivity.this.startActivity(myIntent);                    }
                }
        );
        calibration.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, CalibrationActivity.class);
//                        myIntent.putExtra("key", value); //Optional parameters
                        MainActivity.this.startActivity(myIntent);
                    }
                }
        );
    }

}
