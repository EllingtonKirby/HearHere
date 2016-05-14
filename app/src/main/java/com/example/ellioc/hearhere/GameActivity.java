package com.example.ellioc.hearhere;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class GameActivity extends Activity {

    private AudioEngine audioEngine = null;

    private static final int CLASSIFICATION = 1;

    private int LEFT_DIVIDER = 0;
    private int RIGHT_DIVIDER = 0;

    private ImageView topRight = null;
    private ImageView botRight = null;
    private ImageView topLeft = null;
    private ImageView botLeft = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle calibValues = getIntent().getExtras();
        if (calibValues != null){
            LEFT_DIVIDER = calibValues.getInt("left_calibration");
            RIGHT_DIVIDER = calibValues.getInt("right_calibration");
            Log.i("Calibration", " calibration at " + LEFT_DIVIDER + " " + RIGHT_DIVIDER);
        }
        else {
            LEFT_DIVIDER = 14;
            RIGHT_DIVIDER = -17;
        }

        topRight = (ImageView) findViewById(R.id.topRight);
        botRight = (ImageView) findViewById(R.id.botRight);
        topLeft = (ImageView) findViewById(R.id.topLeft);
        botLeft = (ImageView) findViewById(R.id.botLeft);
        bindAudioRecord();

    }

    public Handler mhandle = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            MediaPlayer mPlayer;
            switch (msg.what) {
                case CLASSIFICATION:
                    int location = msg.arg1;
                    Log.i("Location ", "Returned location is " + location);
                    if(location > 0){
                        if(location < LEFT_DIVIDER){
                            topLeft.setVisibility(View.INVISIBLE);
                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.snare);
                            topLeft.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    topLeft.setVisibility(View.VISIBLE);
                                }
                            }, 500);
                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mPlayer.start();
                        }
                        else{
                            botLeft.setVisibility(View.INVISIBLE);
                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.crash);
                            botLeft.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    botLeft.setVisibility(View.VISIBLE);
                                }
                            }, 500);
                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mPlayer.start();
                        }
                    }
                    else{
                        if(location > RIGHT_DIVIDER){
                            topRight.setVisibility(View.INVISIBLE);
                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.kick);
                            topRight.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    topRight.setVisibility(View.VISIBLE);
                                }
                            }, 500);
                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mPlayer.start();
                        }
                        else{
                            botRight.setVisibility(View.INVISIBLE);
                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.hat);
                            botRight.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    botRight.setVisibility(View.VISIBLE);
                                }
                            }, 500);
                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mPlayer.start();
                        }
                    }
                    default:
                        break;

            }
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

    public void startAudioEngine(){
        audioEngine = new AudioEngine(mhandle);
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
