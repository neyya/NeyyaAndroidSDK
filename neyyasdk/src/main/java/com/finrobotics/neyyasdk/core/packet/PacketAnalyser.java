package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 02/10/15.
 */
public class PacketAnalyser {

    public static final byte COMMAND_GESTURE_DATA = 0x01;
    public static final byte PARAMETER_SWIPE_DATA = 0x01;

    private static byte sCommand, sParameter, sData;


    public static InputPacket parsePacket(byte[] data) {
        if (data[0] == COMMAND_GESTURE_DATA && data[1] == PARAMETER_SWIPE_DATA) {
            sCommand = data[0];
            sParameter = data[1];
            sData = data[2];
            return new InputPacket(sCommand,sParameter,sData);
        }
        return null;
    }
}
