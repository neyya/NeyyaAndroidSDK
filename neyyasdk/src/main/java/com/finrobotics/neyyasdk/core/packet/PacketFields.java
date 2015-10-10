package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 03/10/15.
 */
public class PacketFields {
    public static final byte COMMAND_ACK_PACKET = 0x0C;
    public static final byte COMMAND_SYSTEM_CHANGE_NAME = 0x00;
    public static final byte COMMAND_GESTURE_DATA = 0x01;
    public static final byte COMMAND_HAND_SWITCHING = 0x1B;
    public static final byte COMMAND_GESTURE_SPEED_SWITCHING = 0x1F;
    public static final byte PARAMETER_SWIPE_DATA = 0x01;
    public static final byte PARAMETER_CHANGE_NAME = 0x04;
    public static final byte PARAMETER_RIGHT_HAND = 0x00;
    public static final byte PARAMETER_LEFT_HAND = 0x01;
    public static final byte PARAMETER_GESTURE_SPEED_SLOW = 0x02;
    public static final byte PARAMETER_GESTURE_SPEED_MEDIUM = 0x01;
    public static final byte PARAMETER_GESTURE_SPEED_FAST = 0x00;
    public static final byte ACK_REQUIRED = 0x01;
    public static final byte ACK_NOT_REQUIRED = 0x0f;
    public static final byte ACK_EXECUTION_FAILED = 0x00;
    public static final byte ACK_EXECUTION_SUCCESS = 0x01;
}
