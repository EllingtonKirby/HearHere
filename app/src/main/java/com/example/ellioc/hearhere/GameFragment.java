package com.example.ellioc.hearhere;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment {

    private AudioEngine audioEngine = null;

    private static final int CLASSIFICATION = 1;

    private int VALUE_CALIBRATION_A = 0;
    private int VALUE_CALIBRATION_B = 0;
    private int VALUE_CALIBRATION_C = 0;
    private int VALUE_CALIBRATION_D = 0;
    private int VALUE_CALIBRATION_E = 0;
    private int VALUE_CALIBRATION_F = 0;

    public static String KEY_CALIBRATION_A = "calibration_A";
    public static String KEY_CALIBRATION_B = "calibration_B";
    public static String KEY_CALIBRATION_C = "calibration_C";
    public static String KEY_CALIBRATION_D = "calibration_D";
    public static String KEY_CALIBRATION_E = "calibration_E";
    public static String KEY_CALIBRATION_F = "calibration_F";

    private TextView topRight = null;
    private TextView botRight = null;
    private TextView topLeft = null;
    private TextView botLeft = null;
    private TextView topMid = null;
    private TextView botMid = null;

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

    public GameFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GameFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameFragment newInstance(int A, int B, int C, int D, int E, int F) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_CALIBRATION_A, A);
        args.putInt(KEY_CALIBRATION_B, B);
        args.putInt(KEY_CALIBRATION_C, C);
        args.putInt(KEY_CALIBRATION_D, D);
        args.putInt(KEY_CALIBRATION_E, E);
        args.putInt(KEY_CALIBRATION_F, F);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle b = getArguments();
            VALUE_CALIBRATION_A = b.getInt(KEY_CALIBRATION_A     , 14);
            VALUE_CALIBRATION_B = b.getInt(KEY_CALIBRATION_B, 0);
            VALUE_CALIBRATION_C = b.getInt(KEY_CALIBRATION_C     , -17);
            VALUE_CALIBRATION_D = b.getInt(KEY_CALIBRATION_D, 0);
            VALUE_CALIBRATION_E = b.getInt(KEY_CALIBRATION_E  , 0);
            VALUE_CALIBRATION_F = b.getInt(KEY_CALIBRATION_F, 0);
        }
    }
    @Override
    public void onPause(){
        super.onPause();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(audioEngine != null) {
            stopAudioEngine();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bindAudioRecord();

    }

    @Override
    public void onStop(){
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(audioEngine != null)
            stopAudioEngine();
        super.onStop();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.game_fragment, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
        final Button button1 = (Button) getView().findViewById(R.id.button1);
        final Button button2 = (Button) getView().findViewById(R.id.button2);
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
