package com.example.ellioc.hearhere;

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
        soundManager = new SoundManager(3);
        soundManager.loadSoundManager(getActivity(), getResources().obtainTypedArray(R.array.sound_files));
        soundCategorizer = new Categorizer(calibrationValues);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_synth, container, false);
        ToggleButton recordButton = (ToggleButton) v.findViewById(R.id.record_button);
        recordButton.setOnCheckedChangeListener(this);
        Button playButton = (Button) v.findViewById(R.id.play_button);
        playButton.setOnClickListener(this);
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
                    audioHandler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            switch(msg.what) {
                                case CLASSIFICATION:
                                    soundManager.playSound(msg.arg1);
                                    soundManager.addSoundToSequence(soundCategorizer.categorizeSound(msg.arg1));
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
