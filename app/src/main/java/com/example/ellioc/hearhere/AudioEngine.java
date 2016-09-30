package com.example.ellioc.hearhere;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Environment;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.os.Handler;
import android.media.audiofx.NoiseSuppressor;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class AudioEngine extends Thread {
    private static int[] mSampleRates = new int[] { 48000, 8000, 11025, 22050, 44100 };

    private boolean isRunning = false;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    AudioRecord recordInstance = null;
    Handler mhandle = null;

    public AudioEngine(Handler mhandle) {
        Log.i("Audio Engine", "Audio Engine Constructor");
        this.isRunning = false;
        this.mhandle = mhandle;
        recordInstance = findAudioRecord();
        NoiseSuppressor.create(recordInstance.getAudioSessionId());
        AcousticEchoCanceler.create(recordInstance.getAudioSessionId());
    }

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.i("AudioRecording", "Trying: " + "Sample Rate: " + rate + " Format: " + audioFormat + " Channel:" + channelConfig);
                        int BUFFSIZE = 0;
                        BUFFSIZE = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (BUFFSIZE != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, rate, channelConfig, audioFormat, BUFFSIZE*2);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                        else{
                            Log.i("AudioRecording", "Error in Audio Record buffsize");
                        }
                    } catch (Exception e) {
                        Log.i("AudioRecording", "Error in Audio Record");
                    }
                }
            }
        }
        return null;
    }

    /* Checks if external storage is available for read and write */
    public void isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
            Log.i("External Storage", "isExternalStorageWritable: true");
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

    public File getSoundStorageDir() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                , "HearHere");
        if (!file.exists()) {
            if(!file.mkdirs()) {
                Log.e("Writing Audio", "Directory not created");
            }
        }
        return file;
    }

    public void start_engine(){
        this.isRunning = true;
        Log.i("Audio Engine", "Started");
        this.start();
    }

    public void stop_engine(){
        isRunning = false;
    }

    public void run(){
        try{
            final int READ_2MS = 96;
            recordInstance.startRecording();

            while (this.isRunning) {
                short[] buff = new short[4 * READ_2MS];
                short max_seen = 0;
                double[] leftVariable = new double[12 * READ_2MS];
                double[] rightVariable = new double[12 * READ_2MS];
                boolean metVal = false;
                recordInstance.read(buff, 0,  4*READ_2MS);
                for(int i = 0; i <  4*READ_2MS; i++) {
                    if(i % 2 == 0) {
                        if (Math.abs(buff[i]) > 1500) {
                            metVal = true;
                        }
                        if(buff[i] > max_seen){
                            max_seen = buff[i];
                        }
                    }
                }
                if(metVal){
                    boolean foundPeak = false;
                    short[] validationBuffer = new short[20 * READ_2MS];
                    recordInstance.read(validationBuffer, 0, 20 * READ_2MS);
                    for(int i = 0; i < validationBuffer.length; i++){
                        if (i % 2 ==0){
                            if( Math.abs(validationBuffer[i]) > 10000) {
                                foundPeak = true;
                            }
                        }
                        if(validationBuffer[i] > max_seen){
                            max_seen = validationBuffer[i];
                        }
                    }
                    if(foundPeak){
                        for(int i = 0; i < buff.length; i++){
                            if(i % 2 == 0){
                                leftVariable[i/2] = buff[i];
                            }
                            else{
                                rightVariable[i/2] = buff[i];
                            }
                        }
                        //Create variable length buffers for both left and right microphones
                        for(int i = buff.length; i < buff.length + validationBuffer.length; i++){
                            if(i % 2 == 0){
                                leftVariable[i/2] = validationBuffer[i - buff.length];
                            }
                            else{
                                rightVariable[i/2] = validationBuffer[i - buff.length];
                            }
                        }

                        double[] xCorrFull = DSP.xcorr(leftVariable, rightVariable);
                        double maxFull = xCorrFull[0];

                        int indexFull = 0;
                        int locationFull = 0;

                        for(double weight : xCorrFull){
                            if (weight > maxFull){
                                maxFull = weight;
                                locationFull = indexFull;
                            }
                            indexFull++;
                        }

                        locationFull = locationFull - leftVariable.length;
                        Message msg;
                        Log.i("Max Amp", "Saw " + Integer.toString(max_seen));
                        int CLASSIFICATION = 1;
                        msg = mhandle.obtainMessage(CLASSIFICATION);
                        msg.arg1 = locationFull;
                        msg.obj = SystemClock.currentThreadTimeMillis();
                        Log.i("AudioEngine", "Location: " + locationFull);
                        mhandle.sendMessage(msg);
                        //Thread.sleep(1200);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(recordInstance != null &&recordInstance.getState() == AudioRecord.STATE_INITIALIZED) {
                recordInstance.stop();
                recordInstance.release();
            }
        }
    }
}
