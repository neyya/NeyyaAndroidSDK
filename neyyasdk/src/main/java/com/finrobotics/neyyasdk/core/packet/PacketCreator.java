package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 01/10/15.
 */
public class PacketCreator extends PacketFields {
    public static byte COMMAND_CONTACT_SEND = 0x1A;
    public static byte PARAMETER_CONTACT_NAME = 0x00;
    public static byte PARAMETER_CONTACT_TYPE = 0x01;
    public static byte INDEX_1ST_CONTACT = 0x00;
    public static byte INDEX_2ND_CONTACT = 0x01;
    public static byte INDEX_3RD_CONTACT = 0x02;
    public static byte ACK_REQUIRED = 0x01;
    public static byte ACK_NOT_REQUIRED = 0x0f;


    public static OutputPacket getAndroidSwitchPacket() {
        byte mPacketArray[] = new byte[3];
        mPacketArray[0] = 0x0B;
        mPacketArray[1] = 0x00;
        mPacketArray[2] = 0x02;
        return new OutputPacket(mPacketArray[0], mPacketArray[1], mPacketArray[2], (byte) 0x00, mPacketArray);
    }
}
