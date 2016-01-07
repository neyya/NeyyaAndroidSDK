package com.finrobotics.neyyasdk.core;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.finrobotics.neyyasdk.core.exception.SettingsCommandException;
import com.finrobotics.neyyasdk.core.packet.InputPacket;
import com.finrobotics.neyyasdk.core.packet.OutputPacket;
import com.finrobotics.neyyasdk.core.packet.PacketAnalyser;
import com.finrobotics.neyyasdk.core.packet.PacketCreator;
import com.finrobotics.neyyasdk.core.packet.PacketFields;
import com.finrobotics.neyyasdk.core.preference.PreferenceManager;
import com.finrobotics.neyyasdk.error.NeyyaError;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Core service handles the connection, gesture detection etc
 * Created by zac on 23/09/15.
 */
public class NeyyaBaseService extends Service {
    private static String TAG = "NeyyaSDK";
    public static final String BROADCAST_STATE = "com.finrobotics.neyyasdk.core.BROADCAST_STATE";
    public static final String BROADCAST_DEVICES = "com.finrobotics.neyyasdk.core.BROADCAST_DEVICES";
    public static final String BROADCAST_ERROR = "com.finrobotics.neyyasdk.core.BROADCAST_ERROR";
    public static final String BROADCAST_LOG = "com.finrobotics.neyyasdk.core.BROADCAST_LOG";
    public static final String BROADCAST_GESTURE = "com.finrobotics.neyyasdk.core.BROADCAST_GESTURE";
    public static final String BROADCAST_INFO = "com.finrobotics.neyyasdk.core.BROADCAST_INFO";

    public static final String DATA_STATE = "com.finrobotics.neyyasdk.core.DATA_STATE";
    public static final String DATA_DEVICE_LIST = "com.finrobotics.neyyasdk.core.DATA_DEVICE_LIST";
    public static final String DATA_ERROR_NUMBER = "com.finrobotics.neyyasdk.core.DATA_ERROR_NUMBER";
    public static final String DATA_ERROR_MESSAGE = "com.finrobotics.neyyasdk.core.DATA_ERROR_MESSAGE";
    public static final String DATA_GESTURE = "com.finrobotics.neyyasdk.core.DATA_GESTURE";
    public static final String DATA_INFO = "com.finrobotics.neyyasdk.core.DATA_INFO";

    public static final String BROADCAST_COMMAND_SEARCH = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_SEARCH";
    public static final String BROADCAST_COMMAND_STOP_SEARCH = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_STOP_SEARCH";
    public static final String BROADCAST_COMMAND_CONNECT = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_CONNECT";
    public static final String BROADCAST_COMMAND_DISCONNECT = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_DISCONNECT";
    public static final String BROADCAST_COMMAND_SETTINGS = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_SETTINGS";
    public static final String BROADCAST_COMMAND_GET_STATE = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_GET_STATE";

    public static final String DATA_DEVICE = "com.finrobotics.neyyasdk.core.DATA_DEVICE";
    public static final String DATA_SETTINGS = "com.finrobotics.neyyasdk.core.DATA_SETTINGS";

    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_AUTO_DISCONNECTED = 2;
    public static final int STATE_AUTO_SEARCHING = 3;
    public static final int STATE_SEARCHING = 4;
    public static final int STATE_SEARCH_FINISHED = 5;
    public static final int STATE_CONNECTING = 6;
    public static final int STATE_CONNECTED = 7;
    public static final int STATE_CONNECTED_AND_READY = 8;
    public static final int STATE_DISCONNECTING = 9;
    public static final int STATE_BONDING = 10;
    public static final int STATE_BONDED = 11;
    public static final int STATE_FINDING_SERVICE = 12;
    public static final int STATE_FOUND_SERVICE_AND_CHAR = 13;
    public static final int STATE_ENABLING_NOTIFICATION = 14;
    public static final int STATE_NOTIFICATION_ENABLED = 15;
    public static final int STATE_SWITCHING_MODE = 16;

    public static final int REQUEST_SUCCESS = 0;
    public static final int REQUEST_FAILED = 1;
    public static final int REQUEST_MODE_SWITCH = 1;
    public static final int REQUEST_NAME_CHANGE = 2;
    public static final int REQUEST_HAND_CHANGE = 3;
    public static final int REQUEST_GESTURE_SPEED_CHANGE = 4;

    public static final int STATUS_RING_NAME_CHANGE_SUCCESS = 1;
    public static final int STATUS_RING_NAME_CHANGE_FAILED = 2;
    public static final int STATUS_HAND_CHANGE_SUCCESS = 3;
    public static final int STATUS_HAND_CHANGE_FAILED = 4;
    public static final int STATUS_GESTURE_SPEED_CHANGE_SUCCESS = 5;
    public static final int STATUS_GESTURE_SPEED_CHANGE_FAILED = 6;

    public static final int ERROR_MASK = 0x1000;
    public static final int ERROR_NO_BLE = ERROR_MASK | 0x01;
    public static final int ERROR_BLUETOOTH_NOT_SUPPORTED = ERROR_MASK | 0x02;
    public static final int ERROR_BLUETOOTH_OFF = ERROR_MASK | 0x03;
    public static final int ERROR_NOT_NEYYA = ERROR_MASK | 0x04;
    public static final int ERROR_BONDING_FAILED = ERROR_MASK | 0x05;
    public static final int ERROR_SERVICE_DISCOVERY_FAILED = ERROR_MASK | 0x06;
    public static final int ERROR_SERVICE_NOT_FOUND = ERROR_MASK | 0x07;
    public static final int ERROR_CHARACTERISTICS_NOT_FOUND = ERROR_MASK | 0x08;
    public static final int ERROR_DEVICE_DISCONNECTED = ERROR_MASK | 0x09;
    public static final int ERROR_PACKET_DELIVERY_FAILED = ERROR_MASK | 0x0A;
    public static final int ERROR_NAME_LENGTH_EXCEEDS = ERROR_MASK | 0x0B;
    public static final int ERROR_REMOTE_COMMAND_EXECUTION_FAILED = ERROR_MASK | 0x0C;
    public static final int ERROR_UNKNOWN_HAND_REQUEST = ERROR_MASK | 0x0D;
    public static final int ERROR_UNKNOWN_GESTURE_SPEED_REQUEST = ERROR_MASK | 0x0E;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private static final long TIME_OUT_PERIOD = 4000;

    private static final String neyyaMacSeries = "70:B3:D5:0C:8";
    private static final String neyyaMacSeries2 = "40:A3:6B:0";

    private static final Object mLock = new Object();
    private static int mError = 0;
    private boolean mAborted = false;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private int mCurrentStatus = STATE_AUTO_DISCONNECTED;
    private int mCurrentRequest = 0;
    private int mRequestStatus = REQUEST_SUCCESS;
    private static boolean isRequestPending = false;
    private ArrayList<NeyyaDevice> mNeyyaDevices = new ArrayList<>();
    private BluetoothManager bluetoothManager;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic controlCharacteristic, dataSenderCharacteristic, notificationSourceCharacteristic;
    private int notificationEnableSuccessCount = 0;
    private RequestTimeoutHandler mTimeOutHandler;
    private Runnable timeoutRunnable, scanRunnable;
    private Looper looper;
    private PreferenceManager mPreferenceManager;


    @Override
    public void onCreate() {
        super.onCreate();
        logd("Base service created");
        mPreferenceManager = new PreferenceManager(this);
        mHandler = new Handler();

        HandlerThread handlerThread = new HandlerThread("BondedStateThread");
        handlerThread.start();
        looper = handlerThread.getLooper();
        Handler handler = new Handler(looper, null);

        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                logd("Time out callback received.");
                mTimeOutHandler.sendMessage(new Message());
            }
        };

        registerReceiver(BondStateReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED), null, handler);
        registerReceiver(mCommandReceiver, getCommandIntentFilter());
        // registerReceiver(ReceiverForBondStateChanged, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @Override
    public void onDestroy() {
        logd("Base service destroyed");
        super.onDestroy();
    }

    /**
     * Broadcast receiver for receiving commands from thrid party application.
     * It filters the below commands
     * - BROADCAST_COMMAND_SEARCH
     * - BROADCAST_COMMAND_STOP_SEARCH
     * - BROADCAST_COMMAND_CONNECT
     * - BROADCAST_COMMAND_DISCONNECT
     * - BROADCAST_COMMAND_SETTINGS
     * - BROADCAST_COMMAND_GET_STATE
     */
    private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //Todo : Handle the parse exception
            final String action = intent.getAction();
            if (BROADCAST_COMMAND_SEARCH.equals(action)) {
                startSearch();
            } else if (BROADCAST_COMMAND_STOP_SEARCH.equals(action)) {
                stopSearch();
            } else if (BROADCAST_COMMAND_CONNECT.equals(action)) {
                connectToDevice((NeyyaDevice) intent.getSerializableExtra(DATA_DEVICE));
            } else if (BROADCAST_COMMAND_DISCONNECT.equals(action)) {
                if(mCurrentStatus == STATE_AUTO_SEARCHING) {
                    logd("Calling stop auto search");
                    startAutoSearch(false);
                }
                bluetoothGatt.disconnect();
            } else if (BROADCAST_COMMAND_SETTINGS.equals(action)) {
                sendSettings((Settings) intent.getSerializableExtra(DATA_SETTINGS));
            } else if (BROADCAST_COMMAND_GET_STATE.equals(action)) {
                broadcastState();
            }
        }
    };


    /**
     * Initialise bluetooth. Broadcast error if there is no ble, bluetooth is off or intialisation is failed
     * It launches the bluetooth on/off dialog if bluetooth is off.
     *
     * @return boolean status of whole operation.
     */
    private boolean initialize() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            logd("No BLE in device.");
            broadcastError(ERROR_NO_BLE);
            return false;
        }
        if (bluetoothManager == null) {
            bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        }
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            logd("Bluetooth is not supported by the device");
            broadcastError(ERROR_BLUETOOTH_NOT_SUPPORTED);
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            broadcastError(ERROR_BLUETOOTH_OFF);
            logd("Bluetooth is not enabled");
            startActivity(enableBtIntent);
            return false;
        }

        return true;
    }

    /**
     * Called when for start the seraching.
     * It calls scanLeDevice function to start scanning
     */
    public void startSearch() {
        if (initialize()) {
            scanLeDevice(true);
        }
    }

    /**
     * Starts and stops the device scanning
     *
     * @param enable boolean true for start scanning and false for stop scannuing
     */
    private void scanLeDevice(final boolean enable) {

        if (enable) {
            logd("Started search");
            mNeyyaDevices.clear();
            // Stops scanning after a pre-defined scan period.
            scanRunnable = new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    if (mCurrentStatus == STATE_SEARCHING)
                        mCurrentStatus = STATE_SEARCH_FINISHED;
                    logd("Search finished");
                    broadcastState();
                }
            };

            mHandler.postDelayed(scanRunnable, SCAN_PERIOD);
            mCurrentStatus = STATE_SEARCHING;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            logd("Search finished");
            mCurrentStatus = STATE_SEARCH_FINISHED;
            mHandler.removeCallbacks(scanRunnable);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        broadcastState();
    }

    public void stopSearch() {
        scanLeDevice(false);
    }

    /**
     * Call back for devices. The function onLeScan is being called when new device is found.
     * Adding the found device to array list and broadcast device list to third party application.
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    // String deviceAddress = device.getAddress().substring(0, 13);
                    if (isNeyyaDevice(new NeyyaDevice(device.getName(), device.getAddress()))) {
                        NeyyaDevice neyyaDevice = new NeyyaDevice(device.getName(), device.getAddress());
                        if (!mNeyyaDevices.contains(neyyaDevice)) {
                            logd("Device found - " + device.getAddress() + " Name - " + device.getName());
                            mNeyyaDevices.add(neyyaDevice);
                            broadcastDevices();
                        }
                    }
                }
            };

    /**
     * Main function to start the connection process. It calls series of another function.
     * The next function is called if the previous function execution is success.
     *
     * @param device : The device to which the connection is to be established.
     */
    public void connectToDevice(NeyyaDevice device) {
        logd("Calling connect..");
        mError = 0;
        scanLeDevice(false);

        if (!saveDeviceData(device))
            return;

        if (!initialize())
            return;

        if (!isNeyyaDevice(device))
            return;

        if (!bondDevice(device))
            return;

        if (!connect(device))
            return;

        if (!findServiceAndCharacteristic())
            return;

        if (!enableNotification())
            return;

        if (!switchToAndroidMode())
            return;

        logd("Device is connected and ready");
    }


    /**
     * This function initiates the pairing of device if it is not paired to the device.
     * It waits for the pairing to be done. And return boolean value
     *
     * @param device Neyya device
     * @return pairing is success or not
     */
    private boolean bondDevice(final NeyyaDevice device) {
        boolean isBonded = false;
        Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice mDevice : bondedDevices) {
            if (mDevice.getAddress().equals(device.getAddress())) {
                isBonded = true;
                break;
            }
        }

        if (isBonded) {
            logd("Device is already bonded.. Connecting");
            mCurrentStatus = STATE_BONDED;
            return true;

        } else {
            mCurrentStatus = STATE_BONDING;
            logd("Device is not bonded. Bonding starts");
            mBluetoothAdapter.getRemoteDevice(device.address).createBond();

            try {
                synchronized (mLock) {
                    while (((mCurrentStatus == STATE_BONDING || mCurrentStatus == STATE_DISCONNECTED) && mError == 0 && !mAborted)) {
                        mLock.wait();
                    }
                }
            } catch (InterruptedException e) {
                logd("Interruption exception occurred " + e);
            }
            if (mCurrentStatus == STATE_BONDED) {
                return true;
            }
            if (mError != 0) {
                broadcastError(mError);
                return false;
            }
        }
        return true;
    }

    /**
     * Broadcast receiver for listening the pairing status.
     * bonDevice() function waits to get status from this broadcast receiver.
     */
    BroadcastReceiver BondStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int state;
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                state = intent.getExtras().getInt(BluetoothDevice.EXTRA_BOND_STATE);

                if (state == 12) {
                    logd("Device is bonded");
                    mCurrentStatus = STATE_BONDED;
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                } else if (state == 10) {
                    logd("Bonding failed");
                    mError = ERROR_BONDING_FAILED;
                    mCurrentStatus = STATE_DISCONNECTED;
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                }
            }
        }
    };

    /**
     * Function to connect to start the connection process.
     * It waits for the Gatt callback to establish the connection. After the successful connection, we will get callback on onConnectionStateChange.
     * From there calls the function to start the service discovery. Service discovery is called after a 1.6 millisecond sleep.
     * Giving time to discover service internally. This function waits to complete all these process and return true or false.
     *
     * @param device Neyya device to connect.
     * @return whether the connection process is success or not.
     */
    private boolean connect(NeyyaDevice device) {
        mCurrentStatus = STATE_CONNECTING;
        broadcastState();
        logd("Connecting to device");

        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
        bluetoothGatt = bluetoothDevice.connectGatt(this, false, mGattCallback);

        //Waiting to complete the connection and service discovery
        try {
            synchronized (mLock) {
                while ((mCurrentStatus == STATE_CONNECTING && mError == 0 && !mAborted)) {
                    mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            logd("Interruption exception occurred " + e);
        }

        if (mCurrentStatus == STATE_CONNECTED) {
            logd("Device connected successfully");
            broadcastState();
            return true;
        }
        if (mError != 0) {
            broadcastState();
            broadcastError(mError);
            return false;
        }
        return false;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            logd("onConnectionStateChange : Status -  " + status);
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    logd("Connection state change - State connected");
                    try {
                        synchronized (this) {
                            logd("Waiting to complete the internal service discovery.. 1600ms");
                            wait(1600);
                        }
                    } catch (InterruptedException e) {
                        logd("Interruption exception occurred " + e);
                    }

                    //Calling function to find the discover service
                    logd("Requesting to discover service..");
                    gatt.discoverServices();


                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mCurrentStatus = STATE_DISCONNECTED;
                    bluetoothGatt.close();
                    logd("Connection state change - State disconnected");
                    broadcastState();
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }

                }

            } else {
                logd("Connection state change error occurred. Status - " + status + " New state - " + newState);
                mCurrentStatus = STATE_DISCONNECTED;
                mError = status;
                logd(NeyyaError.parseError(mError));
                broadcastState();
                bluetoothGatt.close();
                synchronized (mLock) {
                    mLock.notifyAll();
                }
                startAutoSearch(true);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            logd("onServicesDiscovered : Status -  " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logd("Service discovered...");
                mCurrentStatus = STATE_CONNECTED;
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            } else {
                logd("onServicesDiscovered received error : " + status);
                mCurrentStatus = STATE_DISCONNECTED;
                mError = ERROR_SERVICE_DISCOVERY_FAILED;
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            logd("OnCharacteristicRead : Status - " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            logd("OnCharacteristicWrite : Packet delivery status - " + status);
            if (mCurrentRequest == REQUEST_MODE_SWITCH) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    mRequestStatus = REQUEST_SUCCESS;
                } else {
                    mError = status;
                    mRequestStatus = REQUEST_FAILED;
                }
                isRequestPending = false;
                synchronized (mLock) {
                    mLock.notifyAll();
                    logd("OnCharacteristicWrite : Releasing thread");
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                logd("OnCharacteristicChanged : Raw data received - " + stringBuilder.toString());
            }


            InputPacket packet = PacketAnalyser.parsePacket(data);
            if (packet == null) {
                return;
            }
            if (packet.getPacketType() == InputPacket.TYPE_DATA) {
                logd("Data packet detected..");
                if (packet.getCommand() == PacketFields.COMMAND_GESTURE_DATA) {
                    final int gesture = Gesture.getGestureFromPacket(packet);
                    logd(Gesture.parseGesture(gesture));
                    broadcastGesture(gesture);
                }
            } else if (packet.getPacketType() == InputPacket.TYPE_ACKNOWLEDGEMENT) {
                setTimeoutTimer(false);
                logd("Acknowledgment packet detected..");
                if (packet.getData() == PacketFields.ACK_EXECUTION_SUCCESS) {
                    isRequestPending = false;
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                } else {
                    isRequestPending = false;
                    mError = ERROR_REMOTE_COMMAND_EXECUTION_FAILED;
                    synchronized (mLock) {
                        mLock.notifyAll();
                    }
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            logd("OnDescriptorRead : Status - " + status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            logd("OnDescriptorWrite : Status -  " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                notificationEnableSuccessCount++;
            } else {
                mError = status;
            }
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }
    };

    /**
     * This function checks the service discovered for neyya service and characteristics.
     * This function ensures that the device is neyya and the service and characteristics are available
     *
     * @return return true or false, found service and characteristics or not
     */
    private boolean findServiceAndCharacteristic() {
        logd("Finding Service and Characteristics");
        mCurrentStatus = STATE_FINDING_SERVICE;
        final BluetoothGattService neyyaService = bluetoothGatt.getService(UUID.fromString(AppConstants.SERVICE_UUID));

        if (neyyaService == null) {
            logd("Neyya service not found");
            mError = ERROR_SERVICE_NOT_FOUND;
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastError(mError);
            broadcastState();
            return false;
        }
        controlCharacteristic = neyyaService.getCharacteristic(UUID.fromString(AppConstants.CHARACTERISTICS_UUID_CONTROL));
        dataSenderCharacteristic = neyyaService.getCharacteristic(UUID.fromString(AppConstants.CHARACTERISTICS_UUID_DATA_SENDER));
        notificationSourceCharacteristic = neyyaService.getCharacteristic(UUID.fromString(AppConstants.CHARACTERISTICS_UUID_NOTIFICATION_SOURCE));
        if (controlCharacteristic == null || dataSenderCharacteristic == null || notificationSourceCharacteristic == null) {
            logd("Neyya characteristics not found");
            mError = ERROR_CHARACTERISTICS_NOT_FOUND;
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastError(mError);
            broadcastState();
            return false;
        }
        logd("Service and Characteristics found");
        mCurrentStatus = STATE_FOUND_SERVICE_AND_CHAR;
        return true;
    }

    /**
     * Enabling notification on the specified characteristics. This is for ring to communicate to phone.
     *
     * @return notification enabled or not.
     */
    private boolean enableNotification() {
        mCurrentStatus = STATE_ENABLING_NOTIFICATION;
        notificationEnableSuccessCount = 0;
        logd("Enabling notification for control characteristics");
        UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor desc = controlCharacteristic.getDescriptor(CONFIG_DESCRIPTOR);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(desc);

        //Waiting to get the callback
        try {
            synchronized (mLock) {
                while ((mCurrentStatus == STATE_ENABLING_NOTIFICATION && notificationEnableSuccessCount == 0 && mError == 0 && !mAborted)) {
                    mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            logd("Interruption exception occurred " + e);
        }

        if (mCurrentStatus == STATE_ENABLING_NOTIFICATION && notificationEnableSuccessCount == 1) {
            logd("Control notification enabled successfully");
        }
        if (mError != 0) {
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastError(mError);
            broadcastState();
            return false;
        }
        logd("Internal notification Status Control characteristics- " + bluetoothGatt.setCharacteristicNotification(controlCharacteristic, true));

        logd("Enabling notification for Data sender characteristics");
        desc = dataSenderCharacteristic.getDescriptor(CONFIG_DESCRIPTOR);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(desc);

        //Waiting to get the callback
        try {
            synchronized (mLock) {
                while ((mCurrentStatus == STATE_ENABLING_NOTIFICATION && notificationEnableSuccessCount == 1 && mError == 0 && !mAborted)) {
                    mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            logd("Interruption exception occurred " + e);
        }

        if (mCurrentStatus == STATE_ENABLING_NOTIFICATION && notificationEnableSuccessCount == 2) {
            logd("Data sender notification enabled successfully");
        }
        if (mError != 0) {
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastError(mError);
            broadcastState();
            return false;
        }
        mCurrentStatus = STATE_NOTIFICATION_ENABLED;

        logd("Internal notification Status Data sender characteristics- " + bluetoothGatt.setCharacteristicNotification(dataSenderCharacteristic, true));
        logd("All notification enabled");
        return true;
    }

    /**
     * Sending the packet to ring to switch to Android mode. It waits till sent the packet and verify the reply.
     * If the device is successfully switched to Android mode, then it returns the status
     *
     * @return success or not
     */
    private boolean switchToAndroidMode() {
        logd("Switching to Android mode");
        mCurrentStatus = STATE_SWITCHING_MODE;
        mCurrentRequest = REQUEST_MODE_SWITCH;
        isRequestPending = true;
        controlCharacteristic.setValue(PacketCreator.getAndroidSwitchPacket().getRawPacketData());
        bluetoothGatt.writeCharacteristic(controlCharacteristic);

        //Waiting to receive and verify the acknowledgement
        try {
            synchronized (mLock) {
                while ((mCurrentRequest == REQUEST_MODE_SWITCH && isRequestPending && mError == 0 && !mAborted)) {
                    mLock.wait();
                }
            }
        } catch (InterruptedException e) {
            logd("Interruption exception occurred " + e);
        }

        if (mRequestStatus == REQUEST_SUCCESS) {
            mCurrentStatus = STATE_CONNECTED_AND_READY;
            broadcastState();
            logd("Switched to Android mode");
            return true;
        }

        if (mError != 0) {
            broadcastError(mError);
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastState();
            return false;
        }

        return false;
    }


    private void startAutoSearch(boolean enable) {

        if (enable) {
            logd("Started auto searching search");
            if (!initialize()) {
                logd("Initialisation error");
                return;
            }
            mCurrentStatus = STATE_AUTO_SEARCHING;
            mBluetoothAdapter.startLeScan(mLeAutoScanCallback);

        } else {
            logd("Search finished");
            mCurrentStatus = STATE_DISCONNECTED;
            mBluetoothAdapter.stopLeScan(mLeAutoScanCallback);
        }
        broadcastState();

    }

    private BluetoothAdapter.LeScanCallback mLeAutoScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    NeyyaDevice neyyaDevice = getDeviceData();
                    logd("Searching devices - " + device.getName());
                    if (neyyaDevice.getAddress().equals(device.getAddress())) {
                        logd("Device found - " + device.getName() + " " + device.getAddress());
                        startAutoSearch(false);
                        connectToDevice(neyyaDevice);
                    }
                }
            };

    /**
     * This is for sending the settings to ring. We need to pass the object of settings class.
     * settings object could include name of the ring, hand preference and gesture speed.
     *
     * @param settings Settings want to change, object of Settings class.
     */
    private void sendSettings(Settings settings) {
        mError = 0;
        if (mCurrentStatus != STATE_CONNECTED_AND_READY) {
            mError = ERROR_DEVICE_DISCONNECTED;
            broadcastError(mError);
            return;
        }

        try {
            //If settings include name for ring
            if (!settings.getRingName().equals(Settings.NO_SETTINGS_NAME)) {
                final String name = settings.getRingName();
                if (name.length() > 16) {
                    throw new SettingsCommandException("Name length limit exceeds", ERROR_NAME_LENGTH_EXCEEDS);
                } else {
                    isRequestPending = true;
                    mCurrentRequest = REQUEST_NAME_CHANGE;
                    deliverPacket(controlCharacteristic, PacketCreator.getNamePacket(name));
                }

                if (mError != 0) {
                    logd("Name change failed - " + NeyyaError.parseError(mError));
                    broadcastInfoStatus(STATUS_RING_NAME_CHANGE_FAILED);
                } else {
                    logd("Name changed successfully");
                    broadcastInfoStatus(STATUS_RING_NAME_CHANGE_SUCCESS);
                }
            }
            mError = 0;

            //If settings include hand change preference
            if (settings.getHandPreference() != Settings.NO_SETTINGS) {
                final int preference = settings.getHandPreference();
                if (preference != Settings.LEFT_HAND && preference != Settings.RIGHT_HAND) {
                    throw new SettingsCommandException("Unknown hand preference", ERROR_UNKNOWN_HAND_REQUEST);
                } else {
                    isRequestPending = true;
                    mCurrentRequest = REQUEST_HAND_CHANGE;
                    deliverPacket(controlCharacteristic, PacketCreator.getHandPreferencePacket(preference));
                }
                if (mError != 0) {
                    logd("Hand preference change failed - " + NeyyaError.parseError(mError));
                    broadcastInfoStatus(STATUS_HAND_CHANGE_FAILED);
                } else {
                    logd("Hand preference changed successfully");
                    broadcastInfoStatus(STATUS_HAND_CHANGE_SUCCESS);
                }
            }

            //If settings include gesture speed preference
            if (settings.getGestureSpeed() != Settings.NO_SETTINGS) {
                final int speed = settings.getGestureSpeed();
                if (speed != Settings.SPEED_SLOW && speed != Settings.SPEED_MEDIUM && speed != Settings.SPEED_FAST) {
                    throw new SettingsCommandException("Unknown gesture speed", ERROR_UNKNOWN_GESTURE_SPEED_REQUEST);
                } else {
                    isRequestPending = true;
                    mCurrentRequest = REQUEST_GESTURE_SPEED_CHANGE;
                    deliverPacket(controlCharacteristic, PacketCreator.getGestureSpeedPacket(speed));
                }
                if (mError != 0) {
                    logd("Gesture speed change failed - " + NeyyaError.parseError(mError));
                    broadcastInfoStatus(STATUS_GESTURE_SPEED_CHANGE_FAILED);
                } else {
                    logd("Gesture speed changed successfully");
                    broadcastInfoStatus(STATUS_GESTURE_SPEED_CHANGE_SUCCESS);
                }
            }
        } catch (SettingsCommandException e) {
            isRequestPending = false;
            logd("Settings command exception occurred. " + e.getMessage());
            broadcastError(e.getError());

        }
    }

    /**
     * This function sends the packet to neyya and waits for its delivery.
     *
     * @param characteristics Characteristics to which we need to deliver the packet
     * @param packet          Packet to deliver. It would be the object of OutputPacket
     * @return success or failure
     */
    private boolean deliverPacket(BluetoothGattCharacteristic characteristics, OutputPacket packet) {
        mError = 0;
        logd("Requesting for packet delivery");

        characteristics.setValue(packet.getRawPacketData());
        if (packet.getAcknowledgement() == PacketFields.ACK_REQUIRED) {
            isRequestPending = true;
            setTimeoutTimer(true);
            bluetoothGatt.writeCharacteristic(characteristics);

            try {
                synchronized (mLock) {
                    while ((isRequestPending && mError == 0 && !mAborted)) {
                        mLock.wait();
                    }
                }
            } catch (InterruptedException e) {
                logd("Interruption exception occurred " + e);
            }
        } else {
            bluetoothGatt.writeCharacteristic(characteristics);
            isRequestPending = false;
        }

        if (mError != 0) {
            return false;
        }

        return true;
    }

    /**
     * Function to start a timer for timeout notification.
     * This is used to cancel the packet delivery wait of the thread.
     *
     * @param status start or stop the timer
     */
    public void setTimeoutTimer(boolean status) {
        if (status) {
            mTimeOutHandler = new RequestTimeoutHandler(looper);
            mTimeOutHandler.postDelayed(timeoutRunnable, TIME_OUT_PERIOD);
            Log.d(TAG, "Setting up time out.");
        } else {
            if (mTimeOutHandler != null) {
                Log.d(TAG, "Cancelling timeout timer..");
                mTimeOutHandler.removeCallbacks(timeoutRunnable);
            }
        }
    }

    /**
     * Handler class for for timeout timer
     */
    static class RequestTimeoutHandler extends Handler {

        public RequestTimeoutHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Handle message..");
            super.handleMessage(msg);
            if (isRequestPending) {
                isRequestPending = false;
                mError = ERROR_PACKET_DELIVERY_FAILED;
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            }
        }
    }

    /**
     * Broadcasts the current status to 3rd party application
     */
    private void broadcastState() {
        final Intent intent = new Intent(BROADCAST_STATE);
        switch (mCurrentStatus) {
            case STATE_DISCONNECTED:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_AUTO_DISCONNECTED:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_AUTO_SEARCHING:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_SEARCHING:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_SEARCH_FINISHED:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_CONNECTING:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_CONNECTED:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_CONNECTED_AND_READY:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_DISCONNECTING:
                intent.putExtra(DATA_STATE, mCurrentStatus);
                break;
            case STATE_BONDING:
                intent.putExtra(DATA_STATE, STATE_CONNECTING);
                break;
            case STATE_BONDED:
                intent.putExtra(DATA_STATE, STATE_CONNECTING);
                break;
            case STATE_FINDING_SERVICE:
                intent.putExtra(DATA_STATE, STATE_CONNECTED);
                break;
            case STATE_FOUND_SERVICE_AND_CHAR:
                intent.putExtra(DATA_STATE, STATE_CONNECTED);
                break;
            case STATE_ENABLING_NOTIFICATION:
                intent.putExtra(DATA_STATE, STATE_CONNECTED);
                break;
            case STATE_NOTIFICATION_ENABLED:
                intent.putExtra(DATA_STATE, STATE_CONNECTED);
                break;
            case STATE_SWITCHING_MODE:
                intent.putExtra(DATA_STATE, STATE_CONNECTED);
                break;
        }

        sendBroadcast(intent);
    }

    /**
     * Broadcast the error to 3rd party application.
     *
     * @param error Error number
     */
    private void broadcastError(int error) {
        final Intent intent = new Intent(BROADCAST_ERROR);
        intent.putExtra(DATA_ERROR_NUMBER, error);
        intent.putExtra(DATA_ERROR_MESSAGE, NeyyaError.parseError(error));
        sendBroadcast(intent);
    }

    /**
     * Broadcast the search list of devices to the 3rd party application.
     */
    private void broadcastDevices() {
        final Intent intent = new Intent(BROADCAST_DEVICES);
        intent.putExtra(DATA_DEVICE_LIST, mNeyyaDevices);
        sendBroadcast(intent);
    }

    /**
     * Broadcast the detected gesture to 3rd party application.
     *
     * @param gesture Gesture number. Gesture numbers are defined in Gesture class.
     */
    private void broadcastGesture(int gesture) {
        final Intent intent = new Intent(BROADCAST_GESTURE);
        intent.putExtra(DATA_GESTURE, gesture);
        sendBroadcast(intent);

    }

    /**
     * Broadcast the execution info state.
     *
     * @param status This includes the STATUS constant values.
     */
    private void broadcastInfoStatus(int status) {
        final Intent intent = new Intent(BROADCAST_INFO);
        intent.putExtra(DATA_INFO, status);
        sendBroadcast(intent);

    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private IntentFilter getCommandIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_COMMAND_SEARCH);
        intentFilter.addAction(BROADCAST_COMMAND_STOP_SEARCH);
        intentFilter.addAction(BROADCAST_COMMAND_CONNECT);
        intentFilter.addAction(BROADCAST_COMMAND_DISCONNECT);
        intentFilter.addAction(BROADCAST_COMMAND_SETTINGS);
        intentFilter.addAction(BROADCAST_COMMAND_GET_STATE);
        return intentFilter;
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Checks the device requested is Neyya device or not. It checks the MAC id of bluetooth device.
     *
     * @param device Neyya device
     * @return A boolean, neyya device or not
     */
    public boolean isNeyyaDevice(NeyyaDevice device) {
        String deviceAddress = device.getAddress().substring(0, 13);
        String deviceAddress2 = device.getAddress().substring(0, 10);
        if (!(neyyaMacSeries.equals(deviceAddress) || neyyaMacSeries2.equals(deviceAddress2))) {
            logd("Not a neyya device");
            broadcastError(ERROR_NOT_NEYYA);
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastState();
            return false;
        }
        return true;

    }

    private boolean saveDeviceData(NeyyaDevice device) {
        mPreferenceManager.setNeyyaName(device.getName());
        mPreferenceManager.setNeyyaAddress(device.getAddress());
        return true;
    }

    private NeyyaDevice getDeviceData() {
        return new NeyyaDevice(mPreferenceManager.getNeyyaName(), mPreferenceManager.getNeyyaAddress());
    }


    public class LocalBinder extends Binder {
        public NeyyaBaseService getService() {
            return NeyyaBaseService.this;
        }
    }


    private void logd(final String message) {
        Log.d(TAG, message);
    }

}
