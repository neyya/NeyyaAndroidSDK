package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 02/10/15.
 */
public class InputPacket {
    private byte command = 0x00, parameter = 0x00, data = 0x00;
    private byte[] rawPacketData;

    public InputPacket(byte command, byte parameter, byte data, byte[] rawPacketData) {
        this.command = command;
        this.parameter = parameter;
        this.data = data;
        this.rawPacketData = rawPacketData;
    }


    public byte getCommand() {
        return command;
    }

    public byte getParameter() {
        return parameter;
    }

    public byte getData() {
        return data;
    }

    public byte[] getRawPacketData() {
        return rawPacketData;
    }


}
