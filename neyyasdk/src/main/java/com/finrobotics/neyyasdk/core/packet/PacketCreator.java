package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 01/10/15.
 */
public class PacketCreator extends PacketFields {


    public static OutputPacket getAndroidSwitchPacket() {
        byte mPacketArray[] = new byte[3];
        mPacketArray[0] = 0x0B;
        mPacketArray[1] = 0x00;
        mPacketArray[2] = 0x02;
        return new OutputPacket(mPacketArray[0], mPacketArray[1], mPacketArray[2], (byte) 0x00, mPacketArray);
    }

    public static OutputPacket getNamePacket(String name) {
        byte mPacketArray[] = new byte[20];
        mPacketArray[0] = COMMAND_SYSTEM_CHANGE_NAME;
        mPacketArray[1] = PARAMETER_CHANGE_NAME;
        byte[] mNameByte = name.getBytes();
        for (int i = 0; i < mNameByte.length; i++) {
            mPacketArray[i + 3] = mNameByte[i];
        }
        mPacketArray[19] = ACK_REQUIRED;
        return new OutputPacket(mPacketArray[0], mPacketArray[1], mNameByte, ACK_REQUIRED, mPacketArray);
    }
}
