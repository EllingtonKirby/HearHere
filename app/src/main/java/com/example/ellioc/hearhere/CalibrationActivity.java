package com.example.ellioc.hearhere;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class CalibrationActivity extends AppCompatActivity {

    AudioEngine audioEngine = null;
    ArrayAdapter<CharSequence> adapter = null;

    Handler mhandle = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg){

            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        Spinner spinner = (Spinner) findViewById(R.id.quadrantSpinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.quadrants, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assert spinner != null;
        spinner.setAdapter(adapter);

        Button startCalibrate = (Button) findViewById(R.id.startCalibration);
    }

    public void startAudioEngine(){
        audioEngine = new AudioEngine(mhandle);
        audioEngine.start_engine();
    }

    public void stopAudioEngine(){
        audioEngine.stop_engine();
    }
}
