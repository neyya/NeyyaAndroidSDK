package com.finrobotics.neyyasdk.core.packet;

import com.finrobotics.neyyasdk.core.Settings;

/**
 * Class for creating packets to send to Neyya device.
 * Created by zac on 01/10/15.
 */
public class PacketCreator extends PacketFields {

    /**
     * Generate packet to switch to android mode
     * @return object of OutputPacket
     */
    public static OutputPacket getAndroidSwitchPacket() {
        byte mPacketArray[] = new byte[3];
        mPacketArray[0] = 0x0B;
        mPacketArray[1] = 0x00;
        mPacketArray[2] = 0x02;
        return new OutputPacket(mPacketArray[0], mPacketArray[1], mPacketArray[2], (byte) 0x00, mPacketArray);
    }

    /**
     * Generate the packet to send to Neyya device for name change
     * @param name String name of the device.
     * @return object of OutputPacket includes the byte data.
     */
    public static OutputPacket getNamePacket(String name) {
        byte mPacketArray[] = new byte[20];
        mPacketArray[0] = COMMAND_SYSTEM_CHANGE_NAME;
        mPacketArray[1] = PARAMETER_CHANGE_NAME;
        byte[] mNameByte = name.getBytes();
        for (int i = 0; i < mNameByte.length; i++) {
            mPacketArray[i + 2] = mNameByte[i];
        }
        mPacketArray[19] = ACK_REQUIRED;
        return new OutputPacket(mPacketArray[0], mPacketArray[1], mNameByte, mPacketArray[19], mPacketArray);
    }

    /**
     * Generate the packet to send to Neyya device to change the hand preference.
     * @param preference hand preference id. Hand preference id is defined in Settings class.
     * @return object of OutputPacket includes the byte data .
     */
    public static OutputPacket getHandPreferencePacket(int preference) {
        byte mPacketArray[] = new byte[3];
        mPacketArray[0] = COMMAND_HAND_SWITCHING;
        if (preference == Settings.RIGHT_HAND) {
            mPacketArray[1] = PARAMETER_RIGHT_HAND;
        } else {
            mPacketArray[1] = PARAMETER_LEFT_HAND;
        }
        mPacketArray[2] = ACK_REQUIRED;
        return new OutputPacket(mPacketArray[0], mPacketArray[1], (byte) 0x00, mPacketArray[2], mPacketArray);
    }

    /**
     * Generate the packet to send to Neyya device to change the gesture speed.
     * @param speed gesture speed id. Gesture speed id is defined in the Settings class.
     * @return object of OutputPacket includes the byte data.
     */
    public static OutputPacket getGestureSpeedPacket(int speed) {

        byte mPacketArray[] = new byte[3];
        mPacketArray[0] = COMMAND_GESTURE_SPEED_SWITCHING;
        if (speed == Settings.SPEED_SLOW) {
            mPacketArray[1] = PARAMETER_GESTURE_SPEED_SLOW;
        } else if (speed == Settings.SPEED_MEDIUM) {
            mPacketArray[1] = PARAMETER_GESTURE_SPEED_MEDIUM;
        } else {
            mPacketArray[1] = PARAMETER_GESTURE_SPEED_FAST;
        }
        mPacketArray[2] = ACK_REQUIRED;
        return new OutputPacket(mPacketArray[0], mPacketArray[1], (byte) 0x00, mPacketArray[2], mPacketArray);
    }
}
