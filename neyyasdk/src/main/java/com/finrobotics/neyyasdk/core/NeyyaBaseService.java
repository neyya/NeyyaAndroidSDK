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
import android.util.Log;

import com.finrobotics.neyyasdk.BuildConfig;
import com.finrobotics.neyyasdk.error.NeyyaError;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Core service
 * Created by zac on 23/09/15.
 */
public class NeyyaBaseService extends Service {
    private static String TAG = "NeyyaSDK";
    public static final String BROADCAST_STATE = "com.finrobotics.neyyasdk.core.BROADCAST_STATE";
    public static final String BROADCAST_DEVICES = "com.finrobotics.neyyasdk.core.BROADCAST_DEVICES";
    public static final String BROADCAST_ERROR = "com.finrobotics.neyyasdk.core.BROADCAST_ERROR";
    public static final String BROADCAST_LOG = "com.finrobotics.neyyasdk.core.BROADCAST_LOG";
    public static final String BROADCAST_GESTURE = "com.finrobotics.neyyasdk.core.BROADCAST_GESTURE";

    public static final String STATE_DATA = "com.finrobotics.neyyasdk.core.STATE_DATA";
    public static final String DEVICE_LIST_DATA = "com.finrobotics.neyyasdk.core.DEVICE_LIST_DATA";
    public static final String ERROR_NUMBER_DATA = "com.finrobotics.neyyasdk.core.ERROR_NUMBER_DATA";
    public static final String ERROR_MESSAGE_DATA = "com.finrobotics.neyyasdk.core.ERROR_MESSAGE_DATA";
    public static final String GESTURE_DATA = "com.finrobotics.neyyasdk.core.GESTURE_DATA";

    public static final String BROADCAST_COMMAND_SEARCH = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_SEARCH";
    public static final String BROADCAST_COMMAND_CONNECT = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_CONNECT";
    public static final String BROADCAST_COMMAND_DISCONNECT = "com.finrobotics.neyyasdk.core.BROADCAST_COMMAND_DISCONNECT";

    public static final String DEVICE_DATA = "com.finrobotics.neyyasdk.core.DEVICE_DATA";

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

    public static final int ERROR_MASK = 0x1000;
    public static final int ERROR_NO_BLE = ERROR_MASK | 0x01;
    public static final int ERROR_BLUETOOTH_NOT_SUPPORTED = ERROR_MASK | 0x02;
    public static final int ERROR_BLUETOOTH_OFF = ERROR_MASK | 0x03;
    public static final int ERROR_NOT_NEYYA = ERROR_MASK | 0x04;
    public static final int ERROR_BONDING_FAILED = ERROR_MASK | 0x05;
    public static final int ERROR_SERVICE_DISCOVERY_FAILED = ERROR_MASK | 0x06;
    public static final int ERROR_SERVICE_NOT_FOUND = ERROR_MASK | 0x07;
    public static final int ERROR_CHARACTERISTICS_NOT_FOUND = ERROR_MASK | 0x08;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private final Object mLock = new Object();
    private int mError = 0;
    private boolean mAborted = false;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private int mCurrentStatus = STATE_AUTO_DISCONNECTED;
    private int mCurrentRequest = 0;
    private int mRequestStatus = REQUEST_SUCCESS;
    private boolean isRequestPending = false;
    private ArrayList<NeyyaDevice> mNeyyaDevices = new ArrayList<>();
    private BluetoothManager bluetoothManager;
    private NeyyaDevice mCurrentDevice;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic controlCharacteristic, gestureCharacteristic;
    private int notificationEnableSuccessCount = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        logd("Base service created");
        mHandler = new Handler();

        HandlerThread handlerThread = new HandlerThread("BondedStateThread");
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        Handler handler = new Handler(looper, null);

        registerReceiver(BondStateReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED), null, handler);
        registerReceiver(mCommandReceiver, getCommandIntentFilter());
        // registerReceiver(ReceiverForBondStateChanged, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }

    @Override
    public void onDestroy() {
        logd("Base service destroyed");
        super.onDestroy();
    }

    private final BroadcastReceiver mCommandReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BROADCAST_COMMAND_SEARCH.equals(action)) {
                startSearch();
            } else if (BROADCAST_COMMAND_CONNECT.equals(action)) {
                connectToDevice((NeyyaDevice) intent.getSerializableExtra(DEVICE_DATA));
            } else if (BROADCAST_COMMAND_DISCONNECT.equals(action)) {
                bluetoothGatt.disconnect();
            }
        }
    };

    private boolean initialize() {

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

    public void startSearch() {
        if (initialize()) {
            scanLeDevice(true);
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            logd("Started search");
            mNeyyaDevices.clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mCurrentStatus = STATE_SEARCH_FINISHED;
                    logd("Search finished");
                    broadcastState();
                }
            }, SCAN_PERIOD);
            mCurrentStatus = STATE_SEARCHING;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            logd("Search finished");
            mCurrentStatus = STATE_SEARCH_FINISHED;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        broadcastState();
    }

    public void stopSearch() {
        scanLeDevice(false);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    NeyyaDevice neyyaDevice = new NeyyaDevice(device.getName(), device.getAddress());
                    if (!mNeyyaDevices.contains(neyyaDevice)) {
                        logd("Device found - " + device.getAddress() + " Name - " + device.getName());
                        mNeyyaDevices.add(neyyaDevice);
                        broadcastDevices();
                    }
                }
            };

    public void connectToDevice(NeyyaDevice device) {
        mCurrentDevice = device;
        mError = 0;
        if (!initialize())
            return;

        if (!isNeyyaDevice())
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

    private boolean connect(NeyyaDevice device) {
        mCurrentStatus = STATE_CONNECTING;
        broadcastState();
        logd("Connecting to device");

        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
        bluetoothGatt = bluetoothDevice.connectGatt(this, false, mGattCallback);

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

                    }

                    //Calling function to find the discover service
                    logd("Requesting to discover service..");
                    boolean reply = gatt.discoverServices();


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

                synchronized (mLock) {
                    mLock.notifyAll();
                }
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
            logd("OnCharacteristicWrite : Status - " + status);
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
                }

            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            logd("OnCharacteristicChanged");
            logd("Characteristics - " + characteristic.getUuid().toString());
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                logd(new String(data) + "" + stringBuilder.toString());
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
        gestureCharacteristic = neyyaService.getCharacteristic(UUID.fromString(AppConstants.CHARACTERISTICS_UUID_GESTURE));
        if (controlCharacteristic == null || gestureCharacteristic == null) {
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

    private boolean enableNotification() {
        mCurrentStatus = STATE_ENABLING_NOTIFICATION;
        notificationEnableSuccessCount = 0;
        logd("Enabling notification for control characteristics");
        UUID CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor desc = controlCharacteristic.getDescriptor(CONFIG_DESCRIPTOR);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(desc);

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

        logd("Enabling notification for gesture characteristics");
        desc = gestureCharacteristic.getDescriptor(CONFIG_DESCRIPTOR);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(desc);

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
            logd("Gesture notification enabled successfully");
        }
        if (mError != 0) {
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastError(mError);
            broadcastState();
            return false;
        }
        mCurrentStatus = STATE_NOTIFICATION_ENABLED;

        logd("Internal notification Status Gesture characteristics- " + bluetoothGatt.setCharacteristicNotification(gestureCharacteristic, true));
        logd("All notification enabled");
        return true;
    }

    private boolean switchToAndroidMode() {
        logd("Switching to Android mode");
        mCurrentStatus = STATE_SWITCHING_MODE;
        mCurrentRequest = REQUEST_MODE_SWITCH;
        isRequestPending = true;
        controlCharacteristic.setValue(PacketCreator.getAndroidSwitchPacket());
        bluetoothGatt.writeCharacteristic(controlCharacteristic);

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


    public void disconnect() {
        mCurrentStatus = STATE_DISCONNECTED;
        broadcastState();
    }


    private void broadcastState() {
        final Intent intent = new Intent(BROADCAST_STATE);
        intent.putExtra(STATE_DATA, mCurrentStatus);
        sendBroadcast(intent);
    }

    private void broadcastError(int error) {
        final Intent intent = new Intent(BROADCAST_ERROR);
        intent.putExtra(ERROR_NUMBER_DATA, error);
        intent.putExtra(ERROR_MESSAGE_DATA, NeyyaError.parseError(error));
        sendBroadcast(intent);
    }

    private void broadcastDevices() {
        final Intent intent = new Intent(BROADCAST_DEVICES);
        intent.putExtra(DEVICE_LIST_DATA, mNeyyaDevices);
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
        intentFilter.addAction(BROADCAST_COMMAND_CONNECT);
        intentFilter.addAction(BROADCAST_COMMAND_DISCONNECT);

        return intentFilter;
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean isNeyyaDevice() {
        if (false) {
            logd("Not a neyya device");
            broadcastError(ERROR_NOT_NEYYA);
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastState();
        }
        return true;
    }


    public class LocalBinder extends Binder {
        public NeyyaBaseService getService() {
            return NeyyaBaseService.this;
        }
    }

    private void loge(final String message) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, message);
    }

    private void loge(final String message, final Throwable e) {
        if (BuildConfig.DEBUG)
            Log.e(TAG, message, e);
    }

    private void logw(final String message) {
        if (BuildConfig.DEBUG)
            Log.w(TAG, message);
    }

    private void logi(final String message) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, message);
    }

    private void logd(final String message) {
        //  if (BuildConfig.DEBUG)
        Log.d(TAG, message);
    }

}
