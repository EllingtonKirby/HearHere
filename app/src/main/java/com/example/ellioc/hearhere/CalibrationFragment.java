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
import android.support.v4.content.PermissionChecker;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
            "F"};
    AudioEngine audioEngine = null;
    ArrayAdapter<CharSequence> adapter = null;
    Spinner spinner = null;
    private Button finishCalibrate = null;
    private Button startCalibrate = null;
    private Button recalibrateButton = null;


    private static HashMap<String, ArrayList<Integer>> calibrationValues = new HashMap<String, ArrayList<Integer>>() {{
        put("A", new ArrayList<Integer>());
        put("B", new ArrayList<Integer>());
        put("C", new ArrayList<Integer>());
        put("D", new ArrayList<Integer>());
        put("E", new ArrayList<Integer>());
        put("F", new ArrayList<Integer>());
    }};

    private static HashMap<String, Integer> thresholds = new HashMap<String, Integer>() {{
        put("A", 0);
        put("B", 0);
        put("C", 0);
        put("D", 0);
        put("E", 0);
        put("F", 0);
    }};

    Handler mhandle = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CLASSIFICATION:
                    int location = msg.arg1;
                    Log.i("Calibration: ", "returned value is " + location);
                    String selectedSection = spinner.getSelectedItem().toString();
                    ArrayList<Integer> selectedCalibrationValues = calibrationValues.get(selectedSection);
                    selectedCalibrationValues.add(location);
                    if (selectedCalibrationValues.size() >= 5) {
                        stopAudioEngine();
                        updateThresholds();
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
        this.setupButtons(view);

        return view;
    }

    /**
     * Set up the buttons and add click listeners to the buttons for use in the
     * onCreateView method.
     */
    private void setupButtons(View view) {
        startCalibrate = (Button) view.findViewById(R.id.startCalibration);
        assert startCalibrate != null;
        startCalibrate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startAudioEngine();
                        startCalibrate.setClickable(false);
                    }
                }
        );

        finishCalibrate = (Button) view.findViewById(R.id.finishCalibration);
        assert finishCalibrate != null;
        finishCalibrate.setVisibility(View.INVISIBLE);
        finishCalibrate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ArrayList<Integer> calibVals = new ArrayList<>();
                        Intent data = new Intent();
                        calibVals.add(thresholds.get("A"));
                        calibVals.add(thresholds.get("B"));
                        calibVals.add(thresholds.get("C"));
                        calibVals.add(thresholds.get("D"));
                        calibVals.add(thresholds.get("E"));
                        calibVals.add(thresholds.get("F"));
                        data.putExtra(GameFragment.KEY_CALIBRATION, calibVals);
//                        data.putExtra(GameFragment.KEY_CALIBRATION_A, thresholds.get("A"));
//                        data.putExtra(GameFragment.KEY_CALIBRATION_B, thresholds.get("B"));
//                        data.putExtra(GameFragment.KEY_CALIBRATION_C, thresholds.get("C"));
//                        data.putExtra(GameFragment.KEY_CALIBRATION_D, thresholds.get("D"));
//                        data.putExtra(GameFragment.KEY_CALIBRATION_E, thresholds.get("E"));
//                        data.putExtra(GameFragment.KEY_CALIBRATION_F, thresholds.get("F"));

                        Log.i(GameFragment.KEY_CALIBRATION_A, Integer.toString(thresholds.get("A")));
                        Log.i(GameFragment.KEY_CALIBRATION_B, Integer.toString(thresholds.get("B")));
                        Log.i(GameFragment.KEY_CALIBRATION_C, Integer.toString(thresholds.get("C")));
                        Log.i(GameFragment.KEY_CALIBRATION_D, Integer.toString(thresholds.get("D")));
                        Log.i(GameFragment.KEY_CALIBRATION_E, Integer.toString(thresholds.get("E")));
                        Log.i(GameFragment.KEY_CALIBRATION_F, Integer.toString(thresholds.get("F")));

                        SharedPreferences preferences = getActivity().getSharedPreferences(MainActivity.PREF_FILE_NAME, 0);
                        String preference = TextUtils.join(",", calibVals);

                        SharedPreferences.Editor prefEditor = preferences.edit();
                        prefEditor.putString(GameFragment.KEY_CALIBRATION, preference);
                        prefEditor.apply();

                        onSubmitCalibrationValuesListener.onSubmitCalibrationValues(
                                CLASSIFICATION,
                                Activity.RESULT_OK,
                                data);
                    }
                }
        );
        String calibrationValuesString = getActivity().getSharedPreferences(MainActivity.PREF_FILE_NAME, 0)
                                                .getString(GameFragment.KEY_CALIBRATION, "");

        recalibrateButton = (Button) view.findViewById(R.id.recalibrate);
        recalibrateButton.setVisibility(View.INVISIBLE);
        if(!calibrationValuesString.equals("")) {
            startCalibrate.setVisibility(View.INVISIBLE);
            recalibrateButton.setVisibility(View.VISIBLE);
            recalibrateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calibrationValues.get(spinner.getSelectedItem().toString()).clear();
                    startAudioEngine();
                    recalibrateButton.setClickable(false);
                }
            });
        }

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
        if(!startCalibrate.isClickable() || !recalibrateButton.isClickable()) {
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

    private void updateThresholds() {
        String selectedSection = spinner.getSelectedItem().toString();
        adapter.remove((CharSequence) spinner.getSelectedItem());
        Collections.sort(calibrationValues.get(selectedSection));
        thresholds.put(selectedSection, getMedian(calibrationValues.get(selectedSection)));
        Toast.makeText(getActivity().getApplicationContext(), "Calibration complete for " + selectedSection,
                Toast.LENGTH_SHORT).show();
        if (adapter.isEmpty()) {
            finishCalibrate.setVisibility(View.VISIBLE);
            startCalibrate.setVisibility(View.INVISIBLE);
            recalibrateButton.setVisibility(View.INVISIBLE);
        } else {
            startCalibrate.setClickable(true);
            recalibrateButton.setClickable(true);
        }
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
