package com.example.ellioc.hearhere;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private int LEFT_CALIBRATION = 14;
    private int RIGHT_CALIBRATION = -17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindButtons();

    }

    private void bindButtons(){
        Button calibration = (Button) findViewById(R.id.calibrate);
        Button startAudioEngine = (Button) findViewById(R.id.startAudioEngine);

        assert calibration != null;
        assert startAudioEngine != null;

        startAudioEngine.setVisibility(View.INVISIBLE);
        startAudioEngine.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, AudioEngineActivity.class);
                        myIntent.putExtra("left_calibration", LEFT_CALIBRATION);
                        myIntent.putExtra("right_calibration", RIGHT_CALIBRATION);
                        MainActivity.this.startActivity(myIntent);                    }
                }
        );
        calibration.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent myIntent = new Intent(MainActivity.this, CalibrationActivity.class);
//                        myIntent.putExtra("key", value); //Optional parameters
                        MainActivity.this.startActivity(myIntent);
                    }
                }
        );
    }

}
