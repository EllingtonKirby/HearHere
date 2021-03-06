package com.example.ellioc.hearhere;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.example.ellioc.hearhere.CalibrationFragment.OnSubmitCalibrationValuesListener} interface
 * to handle interaction events.
 * Use the {@link CalibrationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalibrationFragment extends Fragment {

    private static final int CLASSIFICATION = 1;

    private static final CharSequence[] charSequences = {
            "A",
            "B",
            "C",
            "D",
            "E",
            "F"
    };
    AudioEngine audioEngine = null;
    ArrayAdapter<CharSequence> adapter = null;
    Spinner spinner = null;
    private Button finishCalibrateButton = null;
    private Button calibrateButton = null;
    private ProgressBar calibrateProgressBar = null;

    private static HashMap<String, ArrayList<Integer>> calibrationValues = new HashMap<String, ArrayList<Integer>>() {{
        put("A", new ArrayList<Integer>());
        put("B", new ArrayList<Integer>());
        put("C", new ArrayList<Integer>());
        put("D", new ArrayList<Integer>());
        put("E", new ArrayList<Integer>());
        put("F", new ArrayList<Integer>());
    }};

    //The thresholds will contain the max integer value to denote calibration did not occur
    //because 0 can be an actual threshold.
    private static HashMap<String, Integer> thresholds = new HashMap<String, Integer>() {{
        put("A", Integer.MAX_VALUE);
        put("B", Integer.MAX_VALUE);
        put("C", Integer.MAX_VALUE);
        put("D", Integer.MAX_VALUE);
        put("E", Integer.MAX_VALUE);
        put("F", Integer.MAX_VALUE);
    }};

    Handler mhandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CLASSIFICATION:
                    int location = msg.arg1;
                    Log.i("Calibration: ", "returned value is " + location);
                    Toast.makeText(getActivity().getApplicationContext(), "Last TDoA " + Integer.toString(location),
                            Toast.LENGTH_SHORT).show();
                    String selectedSection = spinner.getSelectedItem().toString();
                    ArrayList<Integer> selectedCalibrationValues = calibrationValues.get(selectedSection);
                    calibrateProgressBar.incrementProgressBy(1);
                    selectedCalibrationValues.add(location);
                    if (selectedCalibrationValues.size() >= 5) {
                        stopAudioEngine();
                        updateThresholds();
                        spinner.setSelection((spinner.getSelectedItemPosition() + 1) % adapter.getCount());
                        calibrateButton.setEnabled(true);
                        calibrateProgressBar.setProgress(0);
                        if(isCalibrated()) {
                            finishCalibrateButton.setEnabled(true);
                        }
                    }
                    break;
            }
            return true;
        }
    });

    OnSubmitCalibrationValuesListener onSubmitCalibrationValuesListener;

    public interface OnSubmitCalibrationValuesListener {
        void onSubmitCalibrationValues(int requestCode, int resultCode, Intent data);
    }

    public CalibrationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CalibrationFragment.
     */
    public static CalibrationFragment newInstance() {
        return new CalibrationFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String calibratedValues = getActivity().getSharedPreferences(MainActivity.PREF_FILE_NAME, 0)
                                            .getString(GameFragment.KEY_CALIBRATION, "");

        //Update thresholds from shared preferences if previously calibrated.
        if(!calibratedValues.equals("")) {
            String[] values = calibratedValues.split(",");
            for(int i = 0; i < values.length; ++i) {
                thresholds.put(charSequences[i].toString(), Integer.parseInt(values[i]));
                Log.i("Calibration Fragment", "Threshold at " + charSequences[i] + ": " + values[i]);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.calibration_fragment, container, false);
        spinner = (Spinner) view.findViewById(R.id.quadrantSpinner);
        // adapter = ArrayAdapter.createFromResource(this, R.array.quadrants, android.R.layout.simple_spinner_item);
        ArrayList<CharSequence> quadrantNames = new ArrayList<>(Arrays.asList(charSequences));
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, quadrantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner != null;
        spinner.setAdapter(adapter);
        //Set the button name depending on if the selected spinner item has already been calibrated or not.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CharSequence selectedPos = (CharSequence) parent.getItemAtPosition(position);
                if(thresholds.get(selectedPos.toString()) == Integer.MAX_VALUE) {
                    calibrateButton.setText(R.string.calibrate);
                }
                else {
                    calibrateButton.setText(R.string.recalibrate);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner.setSelection(0);
        this.setupButtons(view);

        calibrateProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        calibrateProgressBar.setProgress(0);

        return view;
    }

    /**
     * Set up the buttons and add click listeners to the buttons for use in the
     * onCreateView method.
     */
    private void setupButtons(View view) {
        calibrateButton = (Button) view.findViewById(R.id.startCalibration);
        assert calibrateButton != null;
        if(thresholds.get(spinner.getSelectedItem().toString()) == Integer.MAX_VALUE) {
            calibrateButton.setText(R.string.calibrate);
        }
        else {
            calibrateButton.setText(R.string.recalibrate);
        }
        //Whenever the calibrate or recalibrate button is pressed, override the previous
        //values that were stored for the selection.
        calibrateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        calibrationValues.get(spinner.getSelectedItem().toString()).clear();
                        startAudioEngine();
                        calibrateButton.setEnabled(false);
                        finishCalibrateButton.setEnabled(false);
                    }
                }
        );

        finishCalibrateButton = (Button) view.findViewById(R.id.finishCalibration);
        assert finishCalibrateButton != null;
        if(!isCalibrated()) {
            finishCalibrateButton.setEnabled(false);
        }
        finishCalibrateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<Integer> calibVals = new ArrayList<>();
                        Intent data = new Intent();

                        for(CharSequence selection : charSequences) {
                            calibVals.add(thresholds.get(selection.toString()));
                        }
                        data.putExtra(GameFragment.KEY_CALIBRATION, calibVals);

                        Log.i(GameFragment.KEY_CALIBRATION_A, Integer.toString(thresholds.get("A")));
                        Log.i(GameFragment.KEY_CALIBRATION_B, Integer.toString(thresholds.get("B")));
                        Log.i(GameFragment.KEY_CALIBRATION_C, Integer.toString(thresholds.get("C")));
                        Log.i(GameFragment.KEY_CALIBRATION_D, Integer.toString(thresholds.get("D")));
                        Log.i(GameFragment.KEY_CALIBRATION_E, Integer.toString(thresholds.get("E")));
                        Log.i(GameFragment.KEY_CALIBRATION_F, Integer.toString(thresholds.get("F")));

                        SharedPreferences preferences = getActivity().getSharedPreferences(MainActivity.PREF_FILE_NAME, 0);
                        String calibratedValues = TextUtils.join(",", calibVals);

                        SharedPreferences.Editor prefEditor = preferences.edit();
                        prefEditor.putString(GameFragment.KEY_CALIBRATION, calibratedValues);
                        prefEditor.apply();

                        onSubmitCalibrationValuesListener.onSubmitCalibrationValues(
                                CLASSIFICATION,
                                Activity.RESULT_OK,
                                data);
                    }
                }
        );

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSubmitCalibrationValuesListener) {
            onSubmitCalibrationValuesListener = (OnSubmitCalibrationValuesListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSubmitCalibrationValuesListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!calibrateButton.isEnabled()) {
            startAudioEngine();
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        stopAudioEngine();
    }

    @Override
    public void onStop(){
        super.onStop();
        stopAudioEngine();
        ArrayList<Integer> calibVals = new ArrayList<>();
        for(CharSequence selection : charSequences) {
            calibVals.add(thresholds.get(selection.toString()));
        }
        String currentCalibratedValues = TextUtils.join(",", calibVals);
        SharedPreferences preferences = getActivity().getSharedPreferences(MainActivity.PREF_FILE_NAME, 0);
        preferences.edit().putString(GameFragment.KEY_CALIBRATION, currentCalibratedValues).apply();
        Log.i("Calibration Fragment", "onStop");
    }

    private int getMedian(ArrayList<Integer> values) {
        int middle = values.size() / 2;
        if(values.size() % 2 == 1){
            return values.get(middle);
        }
        else{
            return (values.get(middle) + values.get(middle - 1)) / 2;
        }
    }

    /**
     * Update the thresholds for the currently selected spinner item.
     */
    private void updateThresholds() {
        String selectedSection = spinner.getSelectedItem().toString();
        Collections.sort(calibrationValues.get(selectedSection));
        thresholds.put(selectedSection, getMedian(calibrationValues.get(selectedSection)));
        Toast.makeText(getActivity().getApplicationContext(), "Calibration complete for " + selectedSection,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Indicate whether all sections are calibrated.
     * @return true if all sections are calibrated, false otherwise.
     */
    private boolean isCalibrated() {
        for(CharSequence section : charSequences) {
            if(thresholds.get(section.toString()) == Integer.MAX_VALUE) {
                return false;
            }
        }
        return true;
    }

    public void startAudioEngine(){
        audioEngine = new AudioEngine(mhandle);
        audioEngine.start_engine();
    }

    public void stopAudioEngine(){
        if(audioEngine != null)
            audioEngine.stop_engine();
        audioEngine = null;
    }
}
