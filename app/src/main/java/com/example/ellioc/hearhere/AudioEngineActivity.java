package com.example.ellioc.hearhere;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class AudioEngineActivity extends Activity {

    private AudioEngine audioEngine = null;
//    private MediaPlayer mPlayer = null;
    final int PERMISSIONS_RECORD_AUDIO = 1;
    final int PERMISSIONS_WRITE_STORAGE = 2;

    private static final int TOP_LEFT_MSG = 1;
    private static final int BOT_LEFT_MSG = 2;
    private static final int TOP_RIGHT_MSG = 3;
    private static final int BOT_RIGHT_MSG = 4;

    private int LEFT_DIVIDER = 0;
    private int RIGHT_DIVIDER = 0;

    private ImageView topRight = null;
    private ImageView botRight = null;
    private ImageView topLeft = null;
    private ImageView botLeft = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_engine);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle calibValues = getIntent().getExtras();
        if (calibValues != null){
            LEFT_DIVIDER = calibValues.getInt("left_calibration");
            RIGHT_DIVIDER = calibValues.getInt("right_calibration");
        }
        else {
            LEFT_DIVIDER = 14;
            RIGHT_DIVIDER = -17;
        }

        if(!hasWriteExternalStoragePermission())
            requestWriteExternalStoragePermission();
        if(!hasRecordAudioPermission())
            requestRecordAudioPermission();
        else{
            topRight = (ImageView) findViewById(R.id.topRight);
            botRight = (ImageView) findViewById(R.id.botRight);
            topLeft = (ImageView) findViewById(R.id.topLeft);
            botLeft = (ImageView) findViewById(R.id.botLeft);

            bindAudioRecord();
        }
    }


    /**
     * TODO Need to implement Handler to receive input to change UI

     **/
    public Handler mhandle = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case TOP_LEFT_MSG:
                    topLeft.setVisibility(View.INVISIBLE);
//                    mPlayer = MediaPlayer.create(AudioEngineActivity.this, R.raw.snare);

                    topLeft.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            topLeft.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                    break;
                case BOT_LEFT_MSG:
                    botLeft.setVisibility(View.INVISIBLE);
//                    mPlayer = MediaPlayer.create(AudioEngineActivity.this, R.raw.crash);
                    botLeft.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            botLeft.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                    break;
                case TOP_RIGHT_MSG:
                    topRight.setVisibility(View.INVISIBLE);
//                    mPlayer = MediaPlayer.create(AudioEngineActivity.this, R.raw.kick);
                    topRight.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            topRight.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                    break;
                case BOT_RIGHT_MSG:
                    botRight.setVisibility(View.INVISIBLE);
//                    mPlayer = MediaPlayer.create(AudioEngineActivity.this, R.raw.hat);
                    botRight.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            botRight.setVisibility(View.VISIBLE);
                        }
                    }, 500);
                    break;
                default :
                    break;
            }
//            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mPlayer.start();
            return true;
        }

    });

    @Override
    protected void onPause(){
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(audioEngine != null) {
            stopAudioEngine();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bindAudioRecord();

    }

    @Override
    protected void onStop(){
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(audioEngine != null)
            stopAudioEngine();
        this.finish();
        super.onStop();
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
                    //TODO implement logging functionality to make logging a settable option
                }
            }

        }
    }

    public void startAudioEngine(){
        audioEngine = new AudioEngine(mhandle, LEFT_DIVIDER, RIGHT_DIVIDER);
        audioEngine.start_engine();
    }

    public void stopAudioEngine(){
        audioEngine.stop_engine();
    }

    public void bindAudioRecord(){
        final Button button1 = (Button) findViewById(R.id.button1);
        final Button button2 = (Button) findViewById(R.id.button2);

        button2.setVisibility(View.INVISIBLE);
        button1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startAudioEngine();
                        button1.setVisibility(View.INVISIBLE);
                        button2.setVisibility(View.VISIBLE);
                    }
                }
        );
        button2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopAudioEngine();
                        button1.setVisibility(View.VISIBLE);
                        button2.setVisibility(View.INVISIBLE);
                    }
                }
        );
    }


}
