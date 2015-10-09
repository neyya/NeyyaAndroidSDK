package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 03/10/15.
 */
public class OutputPacket {

    private byte command = 0x00, parameter = 0x00, acknowledgement = 0x00;
    private byte[] data = null;
    private byte[] rawPacketData = null;

    public OutputPacket(byte command, byte parameter, byte[] data, byte acknowledgement, byte[] rawPacketData) {
        this.command = command;
        this.parameter = parameter;
        this.data = data;
        this.acknowledgement = acknowledgement;
        this.rawPacketData = rawPacketData;
    }

    public OutputPacket(byte command, byte parameter, byte data, byte acknowledgement, byte[] rawPacketData) {
        this.command = command;
        this.parameter = parameter;
        this.data = new byte[1];
        this.data[0] = data;
        this.acknowledgement = acknowledgement;
        this.rawPacketData = rawPacketData;
    }

    public byte[] getRawPacketData() {
        return rawPacketData;
    }

    public byte getCommand() {
        return command;
    }

    public byte getParameter() {
        return parameter;
    }

    public byte[] getData() {
        return data;
    }

    public byte getAcknowledgement() {
        return acknowledgement;
    }
}
