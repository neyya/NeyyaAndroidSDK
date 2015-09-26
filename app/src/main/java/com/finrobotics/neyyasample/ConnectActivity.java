package com.finrobotics.neyyasample;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by zac on 25/09/15.
 */
public class ConnectActivity extends AppCompatActivity {
    private static String TAG = "NeyyaSDK";
    private MyService mMyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectactivity);
        Intent neyyaServiceIntent = new Intent(this, MyService.class);
        bindService(neyyaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            logd("Service bound");
            mMyService = (MyService) ((MyService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private void logd(final String message) {
        //  if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
        Log.d(TAG, message);
    }
}
