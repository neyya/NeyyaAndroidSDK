package com.finrobotics.neyyasdk.core;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.finrobotics.neyyasdk.BuildConfig;
import com.finrobotics.neyyasdk.error.NeyyaError;

import java.util.ArrayList;

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

    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_AUTO_DISCONNECTED = 2;
    public static final int STATE_AUTO_SEARCHING = 3;
    public static final int STATE_SEARCHING = 4;
    public static final int STATE_SEARCH_FINISHED = 5;
    public static final int STATE_CONNECTING = 6;
    public static final int STATE_CONNECTED = 7;
    public static final int STATE_CONNECTED_AND_READY = 8;
    public static final int STATE_DISCONNECTING = 9;

    public static final int ERROR_MASK = 0x1000;
    public static final int ERROR_NO_BLE = ERROR_MASK | 0x01;
    public static final int ERROR_BLUETOOTH_NOT_SUPPORTED = ERROR_MASK | 0x03;
    public static final int ERROR_BLUETOOTH_OFF = ERROR_MASK | 0x03;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private int mCurrentStatus = STATE_AUTO_DISCONNECTED;
    private ArrayList<NeyyaDevice> mNeyyaDevices = new ArrayList<>();
    //private HashMap<String, String> mBluetoothDevices = new HashMap<>();


    @Override
    public void onCreate() {
        super.onCreate();
        logd("Base service created");
        mHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        logd("Base service destroyed");
        super.onDestroy();
    }

    public void startSearch() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            logd("No BLE in device.");
            broadcastError(ERROR_NO_BLE);
            return;
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            logd("Bluetooth is not supported by the device");
            broadcastError(ERROR_BLUETOOTH_NOT_SUPPORTED);
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            broadcastError(ERROR_BLUETOOTH_OFF);
            logd("Bluetooth is not enabled");
            startActivity(enableBtIntent);
            return;
        }
        scanLeDevice(true);

    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mNeyyaDevices.clear();
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    mCurrentStatus = STATE_SEARCH_FINISHED;
                    broadcastState();
                }
            }, SCAN_PERIOD);
            mCurrentStatus = STATE_SEARCHING;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mCurrentStatus = STATE_SEARCH_FINISHED;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        broadcastState();
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

    private final IBinder mBinder = new LocalBinder();

    public void stopSearch() {

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
