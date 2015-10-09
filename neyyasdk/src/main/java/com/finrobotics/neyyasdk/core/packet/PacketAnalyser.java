package com.finrobotics.neyyasdk.core.packet;

import android.util.Log;

import com.finrobotics.neyyasdk.core.exception.PacketParseException;

/**
 * Created by zac on 02/10/15.
 */
public class PacketAnalyser extends PacketFields {
    private static String TAG = "NeyyaSDK";
    private static byte sCommand, sParameter, sData, sAcknowledgmentCommand, sAcknowledgmentParameter;
    ;


    public static InputPacket parsePacket(byte[] data) {
        try {
            //If it is data packet
            if (data[0] != COMMAND_ACK_PACKET) {
                if (data[0] == COMMAND_GESTURE_DATA && data[1] == PARAMETER_SWIPE_DATA) {
                    sCommand = data[0];
                    sParameter = data[1];
                    sData = data[2];
                    return new InputPacket(InputPacket.TYPE_DATA, sCommand, sParameter, sData, data);
                }
            }
            // If the packet is acknowledgment of a request
            else {
                sCommand = data[0];
                sAcknowledgmentCommand = data[1];
                sAcknowledgmentParameter = data[2];
                sData = data[3];
                return new InputPacket(InputPacket.TYPE_ACKNOWLEDGEMENT, sCommand, sAcknowledgmentCommand, sAcknowledgmentParameter, sData, data);
            }


            throw new PacketParseException("Unknown packet.", data);

        } catch (PacketParseException e) {
            Log.d(TAG, "Packet parse exception occurred. " + e.getMessage());
            return null;
        }
    }
}