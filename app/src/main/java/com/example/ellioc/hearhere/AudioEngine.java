package com.example.ellioc.hearhere;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;

public class AudioEngine extends Thread {
    private static int SAMPLERATE = 0;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIOSOURCE = MediaRecorder.AudioSource.CAMCORDER;
    private static final double THRESHOLD = 1000000000;

    private static int[] mSampleRates = new int[] { 44100, 8000, 11025, 22050, 48000 };
    private volatile int BUFFSIZE = 0;

    private boolean isRunning = false;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    AudioRecord recordInstance = null;

    public AudioEngine() {
        this.isRunning = false;
        isExternalStorageWritable();
        recordInstance = findAudioRecord();

    }

    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.i("AudioRecording", "Trying: " + "Sample Rate: " + rate + " Format: " + audioFormat + " Channel:" + channelConfig);
                        BUFFSIZE = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (BUFFSIZE != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, rate, channelConfig, audioFormat, BUFFSIZE*2);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                SAMPLERATE = rate;
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
            this.start();
    }

    public void stop_engine(){
        isRunning = false;
    }

    public void run(){
        try{
            if(mExternalStorageAvailable && mExternalStorageWriteable) {
                File root = getSoundStorageDir();
                File toWrite = new File(root, "RecordedAudio");

                FileWriter writer = new FileWriter(toWrite);
                ArrayList<String> shortList = new ArrayList<>();

                final int READ_2MS = 96;
                recordInstance.startRecording();

                while (this.isRunning) {
                    short[] buff = new short[2*READ_2MS];
                    double[] left = new double[13*READ_2MS];
                    double[] right = new double[13*READ_2MS];
                    int accumL = 0;
                    int accumR = 0;
                    recordInstance.read(buff, 0,  2*READ_2MS);
                    for(int i = 0; i <  2*READ_2MS; i++) {
                        if(i % 2 == 0){
                            left[i/2] = buff[i];
                        }
                        else{
                            right[i/2] = buff[i];
                        }
                        shortList.add(String.valueOf(buff[i]));
                    }
                    for(int i = 0; i < READ_2MS; i++){
                        accumL += Math.pow(left[i], 2);
                        accumR += Math.pow(right[i], 2);
                    }
                    if(accumL > THRESHOLD){
                        short[] fullSound = new short[24 * READ_2MS];
                        System.arraycopy(buff, 0, fullSound, 0, buff.length);
                        recordInstance.read(fullSound, 2*READ_2MS+1, 22*READ_2MS);
                        for(int i = 0; i <  24*READ_2MS; i++) {
                            if(i % 2 == 0){
                                left[i/2] = fullSound[i];
                            }
                            else{
                                right[i/2] = fullSound[i];
                            }
                        }
                        double[] xCorrelation = DSP.xcorr(left, right);
                        double max = xCorrelation[0];

                        for(double weight : xCorrelation){
                            if(weight > max){
                                max = weight;
                            }
                        }
                        Log.i("Max: ", "max is: " + max);
                        double TDoA = (1/SAMPLERATE) * (max - xCorrelation.length);
                        Log.i("TDoA: ", "TDoA is: " + TDoA);
                    }
                }
                //for(String str : shortList){
                //    writer.write(str);
                //    writer.write("\n");
                //}

            }
            else{
                Log.i("Checking Storage", "run: External Storage Not Available");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        if(recordInstance != null){
            recordInstance.stop();
            recordInstance.release();
            recordInstance = null;
        }
    }
}
