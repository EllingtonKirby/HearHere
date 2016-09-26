package com.example.ellioc.hearhere;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ToggleButton;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SynthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SynthFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private ArrayList<Integer> calibrationValues;
    private SoundManager soundManager;
    private Categorizer soundCategorizer;
    private Handler audioHandler;
    private AudioEngine audioEngine;

    private final int CLASSIFICATION = 1;

    //Fragment Views
    private GridLayout soundGridLayout;
    private ToggleButton recordButton;
    private Button playButton;
    private ObjectAnimator anim;

    public SynthFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param calibrationValues Calibrated values for each view.
     * @return A new instance of fragment SynthFragment.
     */
    public static SynthFragment newInstance(ArrayList<Integer> calibrationValues) {
        SynthFragment fragment = new SynthFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList(GameFragment.KEY_CALIBRATION, calibrationValues);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            calibrationValues = getArguments().getIntegerArrayList(GameFragment.KEY_CALIBRATION);
        }
        soundManager = new SoundManager();
        soundManager.loadSoundManager(getActivity(), getResources().obtainTypedArray(R.array.sound_files));
        soundCategorizer = new Categorizer(calibrationValues);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_synth, container, false);

        //Find and inflate all views in the fragment
        recordButton = (ToggleButton) v.findViewById(R.id.record_button);
        playButton = (Button) v.findViewById(R.id.play_button);
        soundGridLayout = (GridLayout) v.findViewById(R.id.soundboard);

        //Set listeners and initialize behaviors for views
        recordButton.setOnCheckedChangeListener(this);
        playButton.setOnClickListener(this);
        anim = (ObjectAnimator) AnimatorInflater.loadAnimator(
                getContext(), R.animator.sound_board_blinker
        );
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        soundManager.release();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.play_button:
                soundManager.playSequence();
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()) {
            case R.id.record_button:
                if(isChecked) {
                    soundManager.resetSoundSequence();
                    audioHandler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            switch(msg.what) {
                                case CLASSIFICATION:
                                    Integer soundLocation = soundCategorizer.categorizeSound(msg.arg1);
                                    soundManager.playSound(soundLocation);
                                    soundManager.addSoundToSequence(
                                            soundCategorizer.categorizeSound(soundLocation)
                                    );
                                    anim.setTarget(soundGridLayout.getChildAt(soundLocation));
                                    anim.start();
                                    return true;
                                default :
                                    break;
                            }
                            return false;
                        }
                    });
                    audioEngine = new AudioEngine(audioHandler);
                    audioEngine.start_engine();
                }
                else {
                    if(audioEngine != null) {
                        audioEngine.stop_engine();
                    }
                }
                break;
            default:
                break;
        }
    }

}
