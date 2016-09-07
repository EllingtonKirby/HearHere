package com.example.ellioc.hearhere;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;

public class GameActivity extends Activity {

    private AudioEngine audioEngine = null;

    private static final int CLASSIFICATION = 1;

    private int TOP_LEFT_CLASSIFICATION  = 0;
    private int TOP_MID_CLASSIFICATION   = 0;
    private int TOP_RIGHT_CLASSIFICATION = 0;
    private int BOT_LEFT_CLASSIFICATION  = 0;
    private int BOT_MID_CLASSIFICATION   = 0;
    private int BOT_RIGHT_CLASSIFICATION = 0;

    private TextView topRight = null;
    private TextView botRight = null;
    private TextView topLeft = null;
    private TextView botLeft = null;
    private TextView topMid = null;
    private TextView botMid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent data = getIntent();
        if (data != null){
            TOP_LEFT_CLASSIFICATION  = data.getIntExtra("left_calibration", 14);
            TOP_MID_CLASSIFICATION   = data.getIntExtra("top_mid_calibration", 0);
            TOP_RIGHT_CLASSIFICATION = data.getIntExtra("right_calibration", -17);
            BOT_LEFT_CLASSIFICATION  = data.getIntExtra("bot_left_calibration", 0);
            BOT_MID_CLASSIFICATION   = data.getIntExtra("bot_mid_calibration", 0);
            BOT_RIGHT_CLASSIFICATION = data.getIntExtra("bot_right_calibration", 0);
        }

        topRight = (TextView) findViewById(R.id.topRight);
        botRight = (TextView) findViewById(R.id.botRight);
        topLeft  = (TextView) findViewById(R.id.topLeft);
        botLeft  = (TextView) findViewById(R.id.botLeft);
        topMid   = (TextView) findViewById(R.id.topMid);
        botMid   = (TextView) findViewById(R.id.botMid);
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
//                    if(location > 0){
//                        if(location < LEFT_DIVIDER){
//                            Log.i("Location", "Top Left");
//                            topLeft.setVisibility(View.INVISIBLE);
//                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.snare);
//                            topLeft.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    topLeft.setVisibility(View.VISIBLE);
//                                }
//                            }, 500);
//                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                            mPlayer.start();
//                        }
//                        else{
//                            Log.i("Location", "Bot Left");
//                            botLeft.setVisibility(View.INVISIBLE);
//                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.crash);
//                            botLeft.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    botLeft.setVisibility(View.VISIBLE);
//                                }
//                            }, 500);
//                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                            mPlayer.start();
//                        }
//                    }
//                    else{
//                        if(location > RIGHT_DIVIDER){
//                            Log.i("Location", "Top Right");
//                            topRight.setVisibility(View.INVISIBLE);
//                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.kick);
//                            topRight.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    topRight.setVisibility(View.VISIBLE);
//                                }
//                            }, 500);
//                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                            mPlayer.start();
//                        }
//                        else{
//                            Log.i("Location", "Bot Right");
//                            botRight.setVisibility(View.INVISIBLE);
//                            mPlayer = MediaPlayer.create(GameActivity.this, R.raw.hat);
//                            botRight.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    botRight.setVisibility(View.VISIBLE);
//                                }
//                            }, 500);
//                            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                            mPlayer.start();
//                        }
//                    }
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
        Log.i("Audio Engine", "Not Null");
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
                        Log.i("Start Game", "Classification Started");
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
