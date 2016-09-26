package com.example.ellioc.hearhere;

import android.util.Log;

import java.util.ArrayList;

/**
 * Class to categorize sounds.
 */

public class Categorizer {
    private ArrayList<Integer> calibrationValues;

    public Categorizer() {
        this.calibrationValues = new ArrayList<>();
    }

    public Categorizer(ArrayList<Integer> calibrationValues) {
        this.calibrationValues = new ArrayList<>(calibrationValues);

    }

    /**
     * Given an input sound index, categorize the sound and return the index of the
     * categorized sound.
     * @param soundIndex Sound to categorize.
     * @return Return the index of the categorized sound or -1 if unable to categorize.
     */
    public int categorizeSound(int soundIndex) {
        int min = Integer.MAX_VALUE;
        int minLocation = -1;
        for(int i = 0; i < calibrationValues.size(); i++){
            int test = Math.abs(calibrationValues.get(i) - soundIndex);
            if(test < min){
                min = test;
                minLocation = i;
            }
        }
        return minLocation;
    }
}
