package com.example.ellioc.hearhere;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GameFragment} interface
 * to handle interaction events.
 * Use the {@link GameFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameFragment extends Fragment {

    private AudioEngine audioEngine = null;

    private static final int CLASSIFICATION = 1;

    private static ArrayList<Integer> CALIBRATION_VALUES;
    public static String KEY_CALIBRATION_A = "calibration_A";
    public static String KEY_CALIBRATION_B = "calibration_B";
    public static String KEY_CALIBRATION_C = "calibration_C";
    public static String KEY_CALIBRATION_D = "calibration_D";
    public static String KEY_CALIBRATION_E = "calibration_E";
    public static String KEY_CALIBRATION_F = "calibration_F";
    public static String KEY_CALIBRATION = "calibration_values";
    private View A;
    private View B;
    private View C;
    private View D;
    private View E;
    private View F;
    private ArrayList<Pair<View, Integer>> viewsToResourceId;

    public Handler mhandle = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            MediaPlayer mPlayer;
            final View view;
            switch (msg.what) {
                case CLASSIFICATION:
                    int location = msg.arg1;
                    Log.i("Location ", "Returned location is " + location);
                    int min = Integer.MAX_VALUE;
                    int minLoc = 0;
                    for(int i = 0; i < CALIBRATION_VALUES.size(); i++){
                        int test = Math.abs(CALIBRATION_VALUES.get(i) - location);
                        if( test < min){
                            min = test;
                            minLoc = i;
                        }
                    }
                    view = viewsToResourceId.get(minLoc).first;
                    Animation anim = new AlphaAnimation(0.0f, 1.0f);
                    anim.setDuration(50); //You can manage the blinking time with this parameter
                    anim.setStartOffset(20);
                    anim.setRepeatMode(Animation.REVERSE);
                    anim.setRepeatCount(5);
                    view.startAnimation(anim);
                    mPlayer = MediaPlayer.create(getActivity(), viewsToResourceId.get(minLoc).second);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.stop();
                            mp.release();
                            mp = null;
                        }
                    });
                default:
                    break;

            }
            return true;
        }

    });
    private void blink(final View view){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeToBlink = 500;    //in milissegunds
                try{Thread.sleep(timeToBlink);}catch (Exception e) {e.printStackTrace();}
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        TextView txt = (TextView) view;
                        if(txt.getVisibility() == View.VISIBLE){
                            txt.setVisibility(View.INVISIBLE);
                        }else{
                            txt.setVisibility(View.VISIBLE);
                        }
                        blink(view);
                    }
                });
            }
        }).start();
    }
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
    public static GameFragment newInstance(ArrayList<Integer> calibValues) {
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList(KEY_CALIBRATION, calibValues);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle b = getArguments();
            CALIBRATION_VALUES = b.getIntegerArrayList(KEY_CALIBRATION);
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
    public void onResume() {
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
        View view = inflater.inflate(R.layout.game_fragment, container, false);
        A = view.findViewById(R.id.textA);
        B = view.findViewById(R.id.textB);
        C = view.findViewById(R.id.textC);
        D = view.findViewById(R.id.textD);
        E = view.findViewById(R.id.textE);
        F = view.findViewById(R.id.textF);
        viewsToResourceId = new ArrayList<>();
        viewsToResourceId.add(Pair.create(A, R.raw.piano_a));
        viewsToResourceId.add(Pair.create(B, R.raw.piano_b));
        viewsToResourceId.add(Pair.create(C, R.raw.piano_c));
        viewsToResourceId.add(Pair.create(D, R.raw.piano_d));
        viewsToResourceId.add(Pair.create(E, R.raw.piano_e));
        viewsToResourceId.add(Pair.create(F, R.raw.piano_f));
        return view;

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
        if(audioEngine != null)
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
