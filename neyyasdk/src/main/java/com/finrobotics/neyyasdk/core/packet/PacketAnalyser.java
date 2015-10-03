package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 02/10/15.
 */
public class PacketAnalyser extends PacketFields{

    private static byte sCommand, sParameter, sData;


    public static InputPacket parsePacket(byte[] data) {
        if (data[0] == COMMAND_GESTURE_DATA && data[1] == PARAMETER_SWIPE_DATA) {
            sCommand = data[0];
            sParameter = data[1];
            sData = data[2];
            return new InputPacket(sCommand,sParameter,sData, data);
        }
        return null;
    }
}
