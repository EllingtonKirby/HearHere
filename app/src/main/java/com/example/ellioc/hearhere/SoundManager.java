package com.example.ellioc.hearhere;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * Class that manages and simplifies the operations of the SoundPool since complex sound
 * operations are not required in HearHere. SoundManager can play multiple sound streams
 * which can be altered in the constructor and sounds are loaded onto the manager by
 * providing a list of
 */

public class SoundManager {
    private SoundPool soundManager;
    private ArrayList<Integer> soundIds;
    private ArrayDeque<Integer> soundSequence;

    public SoundManager() {
        this(1);
    }

    public SoundManager(int maxStreams) {
        soundManager = buildSoundManager(maxStreams);
        soundIds = new ArrayList<>();
        soundSequence = new ArrayDeque<>();
    }

    /**
     * Create a Sound Pool object that will manage the sound resources, completing operations such
     * as loading and playing sounds.
     * @return SoundPool object containing the number of streams indicated in the argument.
     */
    private SoundPool buildSoundManager(int maxStreams) {
        SoundPool sp;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            sp = new SoundPool(maxStreams, AudioManager.STREAM_MUSIC, 0);
        }
        else {
            sp = (new SoundPool.Builder()).setMaxStreams(maxStreams).setAudioAttributes(
                    (new AudioAttributes.Builder()).setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
            ).build();
        }
        return sp;
    }

    /**
     * Load the SoundManager with soundIDs from the given list of sound resource IDs.
     * @param context Context
     * @param filenames TypedArray containing the list of filenames to map to sound IDs
     */
    public void loadSoundManager(Context context, TypedArray filenames) {
        ArrayList<Integer> resourceIds = getResourceIds(filenames);
        for(Integer id : resourceIds) {
            soundIds.add(soundManager.load(context, id, 1));
        }
    }

    /**
     * Play the stored sequence of sounds back to the user.
     */
    public void playSequence() {
        for(Integer soundIDIndex : soundSequence) {
            this.playSound(soundIDIndex);
        }
    }

    /**
     * Play the sound loaded onto the given location.
     * @param soundIDIndex Index of the sound to play.
     */
    private void playSound(int soundIDIndex) {
        soundManager.play(soundIds.get(soundIDIndex), 1, 1, 1, 0, 1);
    }

    /**
     * Clear the sound sequence so that no sounds are saved.
     */
    public void resetSoundSequence() {
        this.soundSequence.clear();
    }

    /**
     * Remove the most recent stored sound and return the sound ID index back
     * to the user.
     * @return Sonund ID Index of the removed sound.
     */
    public Integer removeSoundFromSequence() {
        return this.soundSequence.removeLast();
    }

    /**
     * Returns the resourceIds of raw files in an ArrayList.
     * @param filenames TypedArray containing the list of filenames to map to resource IDs.
     * @return An ArrayList of IDs corresponding to the filenames.
     */
    private ArrayList<Integer> getResourceIds(TypedArray filenames) {
        ArrayList<Integer> resourceIds = new ArrayList<>();
        for(int i = 0; i < filenames.length(); ++i) {
            resourceIds.add(filenames.getResourceId(i, -1));
        }
        return resourceIds;
    }

    /**
     * Add sound to the sequence of sounds to play back to the user.
     * @param soundIDIndex Index of the sound to store.
     */
    public void addSoundToSequence(int soundIDIndex) {
        soundSequence.add(soundIDIndex);
    }

    /**
     * Release SoundPool resource to allow for garbage collection. Ensure calling this method
     * when the SoundManager will not be used in order to prevent memory leaks.
     */
    public void release() {
        soundManager.release();
        soundManager = null;
    }
}
