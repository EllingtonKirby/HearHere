package com.example.ellioc.hearhere;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class CalibrationActivity extends AppCompatActivity {

    private static final int CLASSIFICATION = 1;
    AudioEngine audioEngine = null;
    ArrayAdapter<CharSequence> adapter = null;
    Spinner spinner = null;

    private static HashMap<String, ArrayList<Integer>> calibrationValues = new HashMap<String, ArrayList<Integer>>(){{
        put("Top Left", new ArrayList<Integer>());
        put("Top Right", new ArrayList<Integer>());
        put("Bottom Left", new ArrayList<Integer>());
        put("Bottom Right", new ArrayList<Integer>());
    }};

    private static HashMap<String, Integer> thresholds = new HashMap<String, Integer>() {{
        put("Top Left", 0);
        put("Top Right", 0);
        put("Bottom Left", 0);
        put("Bottom Right", 0);
    }};

    Handler mhandle = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg){
            switch (msg.what){
                case CLASSIFICATION:
                    //TODO Is this thread safe?
                    int location = msg.arg1;
                    String selectedSection = spinner.getSelectedItem().toString();
                    ArrayList<Integer> selectedCalibrationValues = calibrationValues.get(selectedSection);
                    selectedCalibrationValues.add(location);
                    if(selectedCalibrationValues.size() >= 5){
                        stopAudioEngine();
                        updateThresholds();
                    }
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        spinner = (Spinner) findViewById(R.id.quadrantSpinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.quadrants, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner != null;
        spinner.setAdapter(adapter);
        final Button startCalibrate = (Button) findViewById(R.id.startCalibration);
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
    }

    public int getMedian(ArrayList<Integer> values){
        int middle = values.size() / 2;
        if(values.size() % 2 == 1){
            return values.get(middle);
        }
        else{
            return (values.get(middle) + values.get(middle - 1)) / 2;
        }
    }

    public void updateThresholds(){
        adapter.remove(spinner.getSelectedItem().toString());
        //TODO add toast when threshold is being set to indicate enough taps. Update arrays
//        Collections.sort(calibrationValues.get(selectedSection));
//        thresholds.put(selectedSection, getMedian(calibrationValues.get(selectedSection)));
    }

    public void startAudioEngine(){
        audioEngine = new AudioEngine(mhandle);
        audioEngine.start_engine();
    }

    public void stopAudioEngine(){
        audioEngine.stop_engine();
    }
}
