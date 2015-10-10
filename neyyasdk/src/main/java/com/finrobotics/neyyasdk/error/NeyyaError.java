
package com.finrobotics.neyyasdk.error;

import android.bluetooth.BluetoothGatt;

import com.finrobotics.neyyasdk.core.NeyyaBaseService;

/**
 * Parses the error numbers according to the <b>gatt_api.h</b> file from bluedroid stack.
 * See: https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/android-5.1.0_r1/stack/include/gatt_api.h (and other versions) for details.
 */
public class NeyyaError {

    // Starts at line 106 of gatt_api.h file

    /**
     * Converts the connection status given by the {@link android.bluetooth.BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)} to error name.
     *
     * @param error the status number
     * @return the error name as stated in the gatt_api.h file
     */
    public static String parseConnectionError(final int error) {
        switch (error) {
            case BluetoothGatt.GATT_SUCCESS:
                return "SUCCESS";
            case 0x01:
                return "GATT CONN L2C FAILURE";
            case 0x08:
                return "GATT CONN TIMEOUT";
            case 0x13:
                return "GATT CONN TERMINATE PEER USER";
            case 0x16:
                return "GATT CONN TERMINATE LOCAL HOST";
            case 0x3E:
                return "GATT CONN FAIL ESTABLISH";
            case 0x22:
                return "GATT CONN LMP TIMEOUT";
            case 0x0100:
                return "GATT CONN CANCEL ";
            case 0x0085:
                return "GATT ERROR"; // Device not reachable
            default:
                return "UNKNOWN (" + error + ")";
        }
    }

    // Starts at line 29 of the gatt_api.h file

    /**
     * Converts the bluetooth communication status given by other BluetoothGattCallbacks to error name. It also parses the DFU errors.
     *
     * @param error the status number
     * @return the error name as stated in the gatt_api.h file
     */
    public static String parseError(final int error) {
        switch (error) {
            case 0x0001:
                return "GATT INVALID HANDLE";
            case 0x0002:
                return "GATT READ NOT PERMIT";
            case 0x0003:
                return "GATT WRITE NOT PERMIT";
            case 0x0004:
                return "GATT INVALID PDU";
            case 0x0005:
                return "GATT INSUF AUTHENTICATION";
            case 0x0006:
                return "GATT REQ NOT SUPPORTED";
            case 0x0007:
                return "GATT INVALID OFFSET";
            case 0x0008:
                return "GATT INSUF AUTHORIZATION";
            case 0x0009:
                return "GATT PREPARE Q FULL";
            case 0x000a:
                return "GATT NOT FOUND";
            case 0x000b:
                return "GATT NOT LONG";
            case 0x000c:
                return "GATT INSUF KEY SIZE";
            case 0x000d:
                return "GATT INVALID ATTR LEN";
            case 0x000e:
                return "GATT ERR UNLIKELY";
            case 0x000f:
                return "GATT INSUF ENCRYPTION";
            case 0x0010:
                return "GATT UNSUPPORT GRP TYPE";
            case 0x0011:
                return "GATT INSUF RESOURCE";
            case 0x0087:
                return "GATT ILLEGAL PARAMETER";
            case 0x0080:
                return "GATT NO RESOURCES";
            case 0x0081:
                return "GATT INTERNAL ERROR";
            case 0x0082:
                return "GATT WRONG STATE";
            case 0x0083:
                return "GATT DB FULL";
            case 0x0084:
                return "GATT BUSY";
            case 0x0085:
                return "GATT ERROR";
            case 0x0086:
                return "GATT CMD STARTED";
            case 0x0088:
                return "GATT PENDING";
            case 0x0089:
                return "GATT AUTH FAIL";
            case 0x008a:
                return "GATT MORE";
            case 0x008b:
                return "GATT INVALID CFG";
            case 0x008c:
                return "GATT SERVICE STARTED";
            case 0x008d:
                return "GATT ENCRYPTED NO MITM";
            case 0x008e:
                return "GATT NOT ENCRYPTED";
            case 0x01FF:
                return "GATT VALUE OUT OF RANGE";
            case 0x0101:
                return "TOO MANY OPEN CONNECTIONS";
            case NeyyaBaseService.ERROR_NO_BLE:
                return "ERROR_NO_BLE";
            case NeyyaBaseService.ERROR_BLUETOOTH_NOT_SUPPORTED:
                return "ERROR_BLUETOOTH_NOT_SUPPORTED";
            case NeyyaBaseService.ERROR_BLUETOOTH_OFF:
                return "ERROR_BLUETOOTH_OFF";
            case NeyyaBaseService.ERROR_NOT_NEYYA:
                return "ERROR_NOT_NEYYA";
            case NeyyaBaseService.ERROR_BONDING_FAILED:
                return "ERROR_BONDING_FAILED";
            case NeyyaBaseService.ERROR_SERVICE_DISCOVERY_FAILED:
                return "ERROR_SERVICE_DISCOVERY_FAILED";
            case NeyyaBaseService.ERROR_SERVICE_NOT_FOUND:
                return "ERROR_SERVICE_NOT_FOUND";
            case NeyyaBaseService.ERROR_CHARACTERISTICS_NOT_FOUND:
                return "ERROR_CHARACTERISTICS_NOT_FOUND";
            case NeyyaBaseService.ERROR_DEVICE_DISCONNECTED:
                return "ERROR_DEVICE_DISCONNECTED";
            case NeyyaBaseService.ERROR_PACKET_DELIVERY_FAILED:
                return "ERROR_PACKET_DELIVERY_FAILED";
            case NeyyaBaseService.ERROR_NAME_LENGTH_EXCEEDS:
                return "ERROR_NAME_LENGTH_EXCEEDS";
            case NeyyaBaseService.ERROR_REMOTE_COMMAND_EXECUTION_FAILED:
                return "ERROR_REMOTE_COMMAND_EXECUTION_FAILED";
            default:
                return "UNKNOWN ERROR (" + error + ")";
        }
    }
}
