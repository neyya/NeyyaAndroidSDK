package com.finrobotics.neyyasdk.core;

import java.io.Serializable;

/**
 * Created by zac on 25/09/15.
 */
public class NeyyaDevice implements Serializable {
    public String name;
    public String address;

    public NeyyaDevice(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    //For future use
    private void getDeviceType() {

    }

    //For future use
    private void getDeviceVersion() {

    }

    @Override
    public boolean equals(Object o) {
        NeyyaDevice device2 = (NeyyaDevice) o;
        if (this.getAddress().equals(device2.getAddress()))
            return true;
        else
            return false;
    }
}
