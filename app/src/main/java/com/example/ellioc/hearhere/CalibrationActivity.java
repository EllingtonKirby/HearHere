package com.example.ellioc.hearhere;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;


public class CalibrationActivity extends AppCompatActivity {

    private static final int CLASSIFICATION = 1;
    private static final CharSequence[] charSequences = {"Top Left", "Top Mid",
            "Top Right",
            "Bottom Left",
            "Bottom Mid",
            "Bottom Right"};
    AudioEngine audioEngine = null;
    ArrayAdapter<CharSequence> adapter = null;
    Spinner spinner = null;
    private Button finishCalibrate = null;
    private Button startCalibrate = null;

    private static HashMap<String, ArrayList<Integer>> calibrationValues = new HashMap<String, ArrayList<Integer>>(){{
        put("Top Left", new ArrayList<Integer>());
        put("Top Mid", new ArrayList<Integer>());
        put("Top Right", new ArrayList<Integer>());
        put("Bottom Left", new ArrayList<Integer>());
        put("Bottom Mid", new ArrayList<Integer>());
        put("Bottom Right", new ArrayList<Integer>());
    }};

    private static HashMap<String, Integer> thresholds = new HashMap<String, Integer>() {{
        put("Top Left", 0);
        put("Top Mid", 0);
        put("Top Right", 0);
        put("Bottom Left", 0);
        put("Bottom Mid", 0);
        put("Bottom Right", 0);
    }};

    Handler mhandle = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg){
            switch (msg.what){
                case CLASSIFICATION:
                    int location = msg.arg1;
                    Log.i("Calibration: ", "returned value is " + location);
                    String selectedSection = spinner.getSelectedItem().toString();
                    ArrayList<Integer> selectedCalibrationValues = calibrationValues.get(selectedSection);
                    selectedCalibrationValues.add(location);
                    if(selectedCalibrationValues.size() >= 5){
                        stopAudioEngine();
                        updateThresholds();
                    }
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        spinner = (Spinner) findViewById(R.id.quadrantSpinner);
       // adapter = ArrayAdapter.createFromResource(this, R.array.quadrants, android.R.layout.simple_spinner_item);
        ArrayList<CharSequence> quadrantNames = new ArrayList<>(Arrays.asList(charSequences));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, quadrantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner != null;
        spinner.setAdapter(adapter);
        startCalibrate = (Button) findViewById(R.id.startCalibration);
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
        finishCalibrate = (Button) findViewById(R.id.finishCalibration);
        assert finishCalibrate != null;
        finishCalibrate.setVisibility(View.INVISIBLE);
        finishCalibrate.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        int topLeft = thresholds.get("Top Left");
                        int botLeft = thresholds.get("Bottom Left");
                        int topMid = thresholds.get("Top Mid");
                        int botMid = thresholds.get("Bottom Mid");
                        int botRight = thresholds.get("Bottom Right");
                        int topRight = thresholds.get("Top Right");

                        Intent data = new Intent();
                        data.putExtra("top_left_calibration", topLeft);
                        data.putExtra("top_mid_calibration", topMid);
                        data.putExtra("top_right_calibration", topRight);
                        data.putExtra("bot_left_calibration", botLeft);
                        data.putExtra("bot_mid_calibration", botMid);
                        data.putExtra("bot_right_calibration", botRight);
                        Log.i("Top Left calibration", Integer.toString(topLeft));
                        Log.i("Top Mid calibration",Integer.toString(topMid) );
                        Log.i("Top right calibration",Integer.toString(topRight) );
                        Log.i("Bot Left calibration", Integer.toString(botLeft));
                        Log.i("Bot Mid calibration", Integer.toString(botMid));
                        Log.i("Bot Right calibration",Integer.toString(botRight) );
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
        );
    }

    public void onPause(){
        super.onPause();
        stopAudioEngine();
    }

    public void onStop(){
        super.onStop();
        stopAudioEngine();
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
        String selectedSection = spinner.getSelectedItem().toString();
        adapter.remove((CharSequence)spinner.getSelectedItem());
        Collections.sort(calibrationValues.get(selectedSection));
        thresholds.put(selectedSection, getMedian(calibrationValues.get(selectedSection)));
        Toast.makeText(getApplicationContext(), "Calibration complete for " + selectedSection,
                Toast.LENGTH_SHORT).show();
        if(adapter.isEmpty()){
            finishCalibrate.setVisibility(View.VISIBLE);
            startCalibrate.setVisibility(View.INVISIBLE);
        }
        else{
            startCalibrate.setClickable(true);
        }
    }

    public void startAudioEngine(){
        audioEngine = new AudioEngine(mhandle);
        audioEngine.start_engine();
    }

    public void stopAudioEngine(){
        audioEngine.stop_engine();
    }
}
