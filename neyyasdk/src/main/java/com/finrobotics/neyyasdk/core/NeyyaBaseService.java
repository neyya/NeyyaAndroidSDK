package com.finrobotics.neyyasdk.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.finrobotics.neyyasdk.BuildConfig;

/**
 * Core service
 * Created by zac on 23/09/15.
 */
public class NeyyaBaseService extends Service {
    private String TAG = "NeyyaService";
    public static final String BROADCAST_STATE = "com.finrobotics.neyyasdk.core.BROADCAST_STATE";
    public static final String BROADCAST_ERROR = "com.finrobotics.neyyasdk.core.BROADCAST_ERROR";
    public static final String BROADCAST_LOG = "com.finrobotics.neyyasdk.core.BROADCAST_LOG";

    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_AUTO_DISCONNECTED = 2;
    public static final int STATE_AUTO_SEARCHING = 3;
    public static final int STATE_SEARCHING = 4;
    public static final int STATE_CONNECTING = 5;
    public static final int STATE_CONNECTED = 6;
    public static final int STATE_CONNECTED_AND_READY = 7;
    public static final int STATE_DISCONNECTING = 8;

    public static final int TYPE_STATE_CHANGE = -1;
    public static final int TYPE_ERROR = -2;
    public static final int TYPE_INFO = -3;

    public static final int ERROR_NO_BLE = 10;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Base service created");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Base service destroyed");
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
        if (BuildConfig.DEBUG)
            Log.d(TAG, message);
    }

}
