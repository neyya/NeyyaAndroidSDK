package com.finrobotics.neyyasdk.core.packet;

/**
 * Created by zac on 02/10/15.
 */
public class InputPacket {
    public static final int TYPE_DATA = 1;
    public static final int TYPE_ACKNOWLEDGEMENT = 2;
    private byte command = 0x00, parameter = 0x00, data = 0x00;
    private byte acknowledgmentCommand, acknowledgmentParameter;
    private int packetType = 0;


    private byte[] rawPacketData;

    public InputPacket(int packetType, byte command, byte parameter, byte data, byte[] rawPacketData) {
        this.packetType = packetType;
        this.command = command;
        this.parameter = parameter;
        this.data = data;
        this.rawPacketData = rawPacketData;
    }

    public InputPacket(int packetType, byte command, byte acknowledgmentCommand, byte acknowledgmentParameter, byte data, byte[] rawPacketData) {
        this.packetType = packetType;
        this.packetType = packetType;
        this.command = command;
        this.acknowledgmentCommand = acknowledgmentCommand;
        this.acknowledgmentParameter = acknowledgmentParameter;
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

    public byte getAcknowledgmentCommand() {
        return acknowledgmentCommand;
    }

    public byte getAcknowledgmentParameter() {
        return acknowledgmentParameter;
    }

    public byte[] getRawPacketData() {
        return rawPacketData;
    }

    public int getPacketType() {
        return packetType;
    }
}
