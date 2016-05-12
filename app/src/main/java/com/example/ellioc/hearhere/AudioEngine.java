package com.example.ellioc.hearhere;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Message;
import android.util.Log;
import android.os.Handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class AudioEngine extends Thread {
    private static int SAMPLERATE = 0;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIOSOURCE = MediaRecorder.AudioSource.CAMCORDER;
    private static final double THRESHOLD = 1000000000;

    private static int[] mSampleRates = new int[] { 48000, 8000, 11025, 22050, 44100 };
    private volatile int BUFFSIZE = 0;

    private int LEFT_DIVIDER = 0;
    private int RIGHT_DIVIDER = 0;


    private boolean isRunning = false;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;

    AudioRecord recordInstance = null;
    Handler mhandle = null;

    public AudioEngine(Handler mhandle, int LEFT_DIVIDER, int RIGHT_DIVIDER) {
        this.isRunning = false;
        this.mhandle = mhandle;
        isExternalStorageWritable();
        this.LEFT_DIVIDER = LEFT_DIVIDER;
        this.RIGHT_DIVIDER = RIGHT_DIVIDER;
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

    public double[] smooth(double[] input){
        final int windowSize = 5;
        double[] output = new double[input.length];
        double SMAy = 0;
        for(int i = windowSize; i < input.length; i++){
            SMAy = SMAy + (input[i]/windowSize) - (input[i-windowSize]/windowSize);
            output[i-windowSize] = SMAy;
        }

        return output;
    }

    public void run(){
        try{
            if(mExternalStorageAvailable && mExternalStorageWriteable) {
//                File root = getSoundStorageDir();
//                File toWrite = new File(root, "RecordedAudio");
//
//                FileWriter writer = new FileWriter(toWrite);
//                BufferedWriter bufferedWriter = new BufferedWriter(writer);
//                ArrayList<String> leftList = new ArrayList<>();
//                ArrayList<String> rightList = new ArrayList<>();
//                ArrayList<String> corrList = new ArrayList<>();

                final int READ_2MS = 96;
                recordInstance.startRecording();

                while (this.isRunning) {
                    short[] buff = new short[4 * READ_2MS];
//                    double[] left = new double[2 * READ_2MS];
//                    double[] right = new double[2 * READ_2MS];

                    double[] leftVariable = new double[12 * READ_2MS];
                    double[] rightVariable = new double[12 * READ_2MS];
                    boolean metVal = false;
                    recordInstance.read(buff, 0,  4*READ_2MS);
                    for(int i = 0; i <  4*READ_2MS; i++) {
                        if(i % 2 == 0) {
                            if (Math.abs(buff[i]) > 1500) {
                                metVal = true;
                                break;
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
                                    break;
                                }
                            }
                        }
                        if(foundPeak){
                            for(int i = 0; i < buff.length; i++){
                                if(i % 2 == 0){
//                                    left[i/2] = buff[i];
                                    leftVariable[i/2] = buff[i];
                                }
                                else{
//                                    right[i/2] = buff[i];
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
//                            for(int i = 0; i < leftVariable.length; i++){
//                                leftList.add(String.valueOf(leftVariable[i]));
//                                rightList.add(String.valueOf(rightVariable[i]));
//                            }

//                            //Cross correlation for identical sized buffers
//                            double[] xCorrelation = DSP.xcorr(left, right);
//                            double max = xCorrelation[0];

                            double[] xCorrFull = DSP.xcorr(leftVariable, rightVariable);
                            double maxFull = xCorrFull[0];

//                            int index = 0;
//                            int location = 0;
                            int indexFull = 0;
                            int locationFull = 0;
//                            for(double weight : xCorrelation){
////                                corrList.add(String.valueOf(weight));
//                                if(weight > max){
//                                    max = weight;
//                                    location = index;
//                                }
//                                index++;
//                            }


                            for(double weight : xCorrFull){
                                if (weight > maxFull){
                                    maxFull = weight;
                                    locationFull = indexFull;
                                }
                                indexFull++;
                            }
//                            double TDoA = (1/(double)SAMPLERATE) * (max - xCorrelation.length);
//                            double TDoAFull = (1/(double)SAMPLERATE) * (maxFull - xCorrFull.length);
//
//                            location = location - left.length;
                            locationFull = locationFull - leftVariable.length;
                            Log.i("Index: ", " full index is " + locationFull);
                            Message msg;

                            int TOP_LEFT_MSG = 1;
                            int BOT_LEFT_MSG = 2;
                            int TOP_RIGHT_MSG = 3;
                            int BOT_RIGHT_MSG = 4;
                            if(locationFull < 0){
                                //RIGHT
                                if(locationFull > RIGHT_DIVIDER){
                                    msg = mhandle.obtainMessage(TOP_RIGHT_MSG, locationFull);
                                }
                                else{
                                    msg = mhandle.obtainMessage(BOT_RIGHT_MSG, locationFull);
                                }
                            }
                            else{
                                //LEFT
                                if(locationFull < LEFT_DIVIDER){
                                    msg = mhandle.obtainMessage(TOP_LEFT_MSG, locationFull);
                                }
                                else{
                                    msg = mhandle.obtainMessage(BOT_LEFT_MSG, locationFull);
                                }
                            }
                            mhandle.sendMessage(msg);
                            Thread.sleep(1200);
                        }
                    }
                }
//                int j = 0;
//                for(int i = 0; i < leftList.size() / 2; i++){
//                    bufferedWriter.write(leftList.get(i) + "\t" + rightList.get(i));
//                    bufferedWriter.newLine();
//                }
//                bufferedWriter.close();
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
