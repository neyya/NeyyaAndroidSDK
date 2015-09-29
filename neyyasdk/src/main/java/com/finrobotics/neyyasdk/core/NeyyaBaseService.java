package com.finrobotics.neyyasdk.core;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
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

    public static final int ERROR_MASK = 0x1000;
    public static final int ERROR_NO_BLE = ERROR_MASK | 0x01;
    public static final int ERROR_BLUETOOTH_NOT_SUPPORTED = ERROR_MASK | 0x02;
    public static final int ERROR_BLUETOOTH_OFF = ERROR_MASK | 0x03;
    public static final int ERROR_NOT_NEYYA = ERROR_MASK | 0x04;
    public static final int ERROR_BONDING_FAILED = ERROR_MASK | 0x05;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private final Object mLock = new Object();
    private int mError = 0;
    private boolean mAborted = false;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private int mCurrentStatus = STATE_AUTO_DISCONNECTED;
    private ArrayList<NeyyaDevice> mNeyyaDevices = new ArrayList<>();
    private BluetoothManager bluetoothManager;
    private NeyyaDevice mCurrentDevice;
    //private HashMap<String, String> mBluetoothDevices = new HashMap<>();


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

        if (!isNeyyaDevice()) {
            loge("Not a neyya device");
            broadcastError(ERROR_NOT_NEYYA);
            mCurrentStatus = STATE_DISCONNECTED;
            broadcastState();
            return;
        }

        if (!bondDevice(device))
            return;

        connect(device);

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

    private BluetoothGatt connect(NeyyaDevice device) {
        mCurrentStatus = STATE_CONNECTING;
        broadcastState();
        logd("Connecting to device");

        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(device.getAddress());
        final BluetoothGatt gatt = bluetoothDevice.connectGatt(this, false, mGattCallback);

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
            logd("Device connected");
            broadcastState();
        }
        if (mError != 0) {
            broadcastError(mError);
        }

        return gatt;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                logd("State connected");
                mCurrentStatus = STATE_CONNECTED;
                synchronized (mLock) {
                    mLock.notifyAll();
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                logd("State disconnected");
            }


        }
    };

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

        return intentFilter;
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean isNeyyaDevice() {
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
