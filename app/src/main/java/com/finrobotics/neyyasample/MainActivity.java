package com.finrobotics.neyyasample;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "NeyyaSDK";
    private MyService mMyService;
    private TextView mStatusTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
        mStatusTextView.setText("Disconnected");
        Intent neyyaServiceIntent = new Intent(this, MyService.class);
        bindService(neyyaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNeyyaUpdateReceiver, makeNeyyaUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNeyyaUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        super.onDestroy();
    }

    // Code to manage Service lifecycle.
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

    private final BroadcastReceiver mNeyyaUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            logd("Broadcast received");
            final String action = intent.getAction();
            if (MyService.BROADCAST_STATE.equals(action)) {
                int status = intent.getIntExtra(MyService.STATE_DATA, 0);
                if (status == MyService.STATE_DISCONNECTED) {
                    mStatusTextView.setText("Disconnected");
                } else if (status == MyService.STATE_SEARCHING) {
                    mStatusTextView.setText("Searching");
                } else if (status == MyService.STATE_SEARCH_FINISHED) {
                    mStatusTextView.setText("Searching finished");
                } else {
                    mStatusTextView.setText("Status - " + status);
                }

            } else if (MyService.BROADCAST_DEVICES.equals(action)) {

            } else if (MyService.BROADCAST_ERROR.equals(action)) {
                int errorNo = intent.getIntExtra(MyService.ERROR_NUMBER_DATA, 0);
                String errorMessage = intent.getStringExtra(MyService.ERROR_MESSAGE_DATA);
                logd("Error occurred. Error number - " + errorNo + " Message - " + errorMessage);
            }
        }
    };

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchButton:
                mMyService.startSearch();
                break;
        }
    }


    private IntentFilter makeNeyyaUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.BROADCAST_STATE);
        intentFilter.addAction(MyService.BROADCAST_DEVICES);
        intentFilter.addAction(MyService.BROADCAST_ERROR);
        intentFilter.addAction(MyService.BROADCAST_LOG);
        return intentFilter;
    }


    private void loge(final String message) {
        if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
            Log.e(TAG, message);
    }

    private void loge(final String message, final Throwable e) {
        if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
            Log.e(TAG, message, e);
    }

    private void logw(final String message) {
        if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
            Log.w(TAG, message);
    }

    private void logi(final String message) {
        if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
            Log.i(TAG, message);
    }

    private void logd(final String message) {
        //  if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
        Log.d(TAG, message);
    }
}
