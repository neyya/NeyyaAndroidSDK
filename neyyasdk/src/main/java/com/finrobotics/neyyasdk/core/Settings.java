package com.finrobotics.neyyasdk.core;

import java.io.Serializable;

/**
 * Created by zac on 03/10/15.
 */
public class Settings implements Serializable{
    public static final String NO_SETTINGS_NAME = "";
    public static final int NO_SETTINGS = 0;
    public static final int LEFT_HAND = 1;
    public static final int RIGHT_HAND = 2;
    public static final int SPEED_SLOW = 3;
    public static final int SPEED_MEDIUM = 4;
    public static final int SPEED_FAST = 5;

    private String ringName = NO_SETTINGS_NAME;
    private int handPreference = NO_SETTINGS;
    private int gestureSpeed = NO_SETTINGS;

    public void setRingName(String name) {
        this.ringName = name;
    }

    public void setHandPreference(int handPreference) {
        this.handPreference = handPreference;
    }

    public void setGestureSpeed(int gestureSpeed) {
        this.gestureSpeed = gestureSpeed;
    }

    public String getRingName() {
        return ringName;
    }

    public int getHandPreference() {
        return handPreference;
    }

    public int getGestureSpeed() {
        return gestureSpeed;
    }
}
