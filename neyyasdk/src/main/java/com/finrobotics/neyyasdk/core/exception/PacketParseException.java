package com.finrobotics.neyyasdk.core.exception;

/**
 * Created by zac on 03/10/15.
 */
public class PacketParseException extends Exception {
    private static final long serialVersionUID = 4664456874499611218L;
    private byte[] packet;

    public PacketParseException(String detailMessage, byte[] data) {
        super(detailMessage);
        this.packet = data;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + " Packet data - " + getString(packet);
    }

    private String getString(byte[] data) {
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            return stringBuilder.toString();
        }
        return "";
    }
}
