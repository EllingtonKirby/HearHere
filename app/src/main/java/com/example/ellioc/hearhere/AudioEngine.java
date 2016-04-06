package com.example.ellioc.hearhere;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

public class AudioEngine extends Thread {
    private static final int SAMPLERATE = 44100;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIOSOURCE = MediaRecorder.AudioSource.CAMCORDER;

   private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };

    private volatile int BUFFSIZE = 0;

    private boolean isRunning = false;
    private boolean isStarted = false;

    AudioRecord recordInstance = null;


    public AudioEngine() {
        this.isRunning = false;
        this.isStarted = false;
        recordInstance = findAudioRecord();

    }

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.i("AudioRecording", "Error in Audio Record");
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, rate, channelConfig, audioFormat, bufferSize*2);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.i("AudioRecording", "Error in Audio Record");
                    }
                }
            }
        }
        return null;
    }

    public void start_engine(){
        if(!isStarted) {
            this.isRunning = true;
            this.isStarted = true;
            this.start();
        }
        else{
            this.isRunning = true;
        }
    }

    public void stop_engine(){
        this.isRunning = false;
        if(recordInstance != null && recordInstance.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            recordInstance.stop();
        }
    }

    public void run(){
        try{
            while(this.isRunning){
                recordInstance.startRecording();
                int SIZE = BUFFSIZE;
                short[] buff = new short[SIZE];
                recordInstance.read(buff, 0, SIZE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
