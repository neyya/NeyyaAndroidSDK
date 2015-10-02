package com.finrobotics.neyyasdk.core;

import com.finrobotics.neyyasdk.core.packet.InputPacket;
import com.finrobotics.neyyasdk.core.packet.PacketAnalyser;

/**
 * Created by zac on 01/10/15.
 */
public class Gesture {

    public static final int SWIPE_LEFT = 1;
    public static final int SWIPE_RIGHT = 2;
    public static final int SWIPE_UP = 3;
    public static final int SWIPE_DOWN = 4;
    public static final int SINGLE_TAP = 5;
    public static final int DOUBLE_TAP = 6;
    public static final int TAP_AND_HOLD = 7;
    public static final int TAP_AND_SWIPE_LEFT = 8;
    public static final int TAP_AND_SWIPE_RIGHT = 9;
    public static final int TAP_AND_SWIPE_UP = 10;
    public static final int TAP_AND_SWIPE_DOWN = 11;
    public static final int SWIPE_RIGHT_AND_TAP = 12;
    public static final int SWIPE_LEFT_AND_TAP = 13;
    public static final int SWIPE_UP_AND_TAP = 14;
    public static final int SWIPE_DOWN_AND_TAP = 15;
    public static final int DOUBLE_SWIPE_LEFT = 16;
    public static final int DOUBLE_SWIPE_RIGHT = 17;
    public static final int DOUBLE_SWIPE_UP = 18;
    public static final int DOUBLE_SWIPE_DOWN = 19;
    public static final int TRIPLE_TAP = 20;


    protected static int getGestureFromPacket(InputPacket packet) {
        if (packet.getParameter() == PacketAnalyser.PARAMETER_SWIPE_DATA && packet.getCommand() == PacketAnalyser.COMMAND_GESTURE_DATA) {
            return (int) packet.getData();
        }
        return 0;
    }

    public static String parseGesture(int gesture) {
        switch (gesture) {
            case SWIPE_LEFT:
                return "SWIPE_LEFT";
            case SWIPE_RIGHT:
                return "SWIPE_RIGHT";
            case SWIPE_UP:
                return "SWIPE_UP";
            case SWIPE_DOWN:
                return "SWIPE_DOWN";
            case SINGLE_TAP:
                return "SINGLE_TAP";
            case DOUBLE_TAP:
                return "DOUBLE_TAP";
            case TAP_AND_HOLD:
                return "TAP_AND_HOLD";
            case TAP_AND_SWIPE_LEFT:
                return "TAP_AND_SWIPE_LEFT";
            case TAP_AND_SWIPE_RIGHT:
                return "TAP_AND_SWIPE_RIGHT";
            case TAP_AND_SWIPE_UP:
                return "TAP_AND_SWIPE_UP";
            case TAP_AND_SWIPE_DOWN:
                return "TAP_AND_SWIPE_DOWN";
            case SWIPE_RIGHT_AND_TAP:
                return "SWIPE_RIGHT_AND_TAP";
            case SWIPE_LEFT_AND_TAP:
                return "SWIPE_LEFT_AND_TAP";
            case SWIPE_UP_AND_TAP:
                return "SWIPE_UP_AND_TAP";
            case SWIPE_DOWN_AND_TAP:
                return "SWIPE_DOWN_AND_TAP";
            case DOUBLE_SWIPE_LEFT:
                return "DOUBLE_SWIPE_LEFT";
            case DOUBLE_SWIPE_RIGHT:
                return "DOUBLE_SWIPE_RIGHT";
            case DOUBLE_SWIPE_UP:
                return "DOUBLE_SWIPE_UP";
            case DOUBLE_SWIPE_DOWN:
                return "DOUBLE_SWIPE_DOWN";
            case TRIPLE_TAP:
                return "TRIPLE_TAP";

        }
        return "";
    }
}
