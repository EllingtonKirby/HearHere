package com.example.ellioc.hearhere;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SynthFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SynthFragment extends Fragment {
    private ArrayList<Integer> calibrationValues;
    private SoundManager soundManager;
    private Categorizer soundCategorizer;
    private AudioEngine audioEngine;

    private final int CLASSIFICATION = 1;

    /**
     * Handler to respond to values obtained from the AudioEngine.
     */
    private Handler mhandle = new Handler(
            new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    if(msg.what == CLASSIFICATION) {
                        soundManager.addSoundToSequence(soundCategorizer.categorizeSound(msg.arg1));
                    }
                    return true;
                }
            }
    );


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
        audioEngine = new AudioEngine(mhandle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_synth, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        audioEngine.start_engine();
    }

    @Override
    public void onPause() {
        super.onPause();
        audioEngine.stop_engine();
    }

    @Override
    public void onStop() {
        super.onStop();
        soundManager.release();
    }

}
