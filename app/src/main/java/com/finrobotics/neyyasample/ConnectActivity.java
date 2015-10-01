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
import android.widget.Button;
import android.widget.TextView;

import com.finrobotics.neyyasdk.core.NeyyaDevice;

/**
 * Created by zac on 25/09/15.
 */
public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = "NeyyaSDK";
    private static int currentState = MyService.STATE_DISCONNECTED;

    private MyService mMyService;
    private TextView mNameTextView, mAddressTextView, mStatusTextView, mDataTextView;
    private Button mConnectButton;
    private NeyyaDevice mSelectedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectactivity);
        mNameTextView = (TextView) findViewById(R.id.nameTextView);
        mAddressTextView = (TextView) findViewById(R.id.addressTextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
        mDataTextView = (TextView) findViewById(R.id.dataTextView);
        mConnectButton = (Button) findViewById(R.id.connectButton);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentState == MyService.STATE_DISCONNECTED) {
                    //mMyService.connectToDevice(mSelectedDevice);
                    final Intent intent = new Intent(MyService.BROADCAST_COMMAND_CONNECT);
                    intent.putExtra(MyService.DEVICE_DATA, mSelectedDevice);
                    sendBroadcast(intent);
                } else if (currentState == MyService.STATE_CONNECTED_AND_READY) {
                    final Intent intent = new Intent(MyService.BROADCAST_COMMAND_DISCONNECT);
                    sendBroadcast(intent);
                }
            }
        });
        Intent intent = getIntent();
        mSelectedDevice = (NeyyaDevice) intent.getSerializableExtra("SELECTED_DEVICE");
        if (mSelectedDevice != null) {
            mNameTextView.setText("Name - " + mSelectedDevice.getName());
            mAddressTextView.setText("Address - " + mSelectedDevice.getAddress());
        } else {
            mNameTextView.setText("Name - UNKNOWN");
            mAddressTextView.setText("Address - UNKNOWN");
        }

        Intent neyyaServiceIntent = new Intent(this, MyService.class);
        startService(neyyaServiceIntent);
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
        logd("On destroy Connect Activity");
        super.onDestroy();
    }

    private final BroadcastReceiver mNeyyaUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //  logd("Broadcast received");
            final String action = intent.getAction();
            if (MyService.BROADCAST_STATE.equals(action)) {
                int status = intent.getIntExtra(MyService.STATE_DATA, 0);
                if (status == MyService.STATE_DISCONNECTED) {
                    currentState = MyService.STATE_DISCONNECTED;
                    changeButtonStatus();
                    showStatus("Disconnected");
                } else if (status == MyService.STATE_CONNECTING) {
                    currentState = MyService.STATE_CONNECTING;
                    changeButtonStatus();
                    //  logd("Broadcast received - State connecting");
                    showStatus("Connecting");
                } else if (status == MyService.STATE_CONNECTED) {
                    currentState = MyService.STATE_CONNECTED;
                    //   logd("Broadcast received - State connected");
                    changeButtonStatus();
                    showStatus("Connected");
                } else if (status == MyService.STATE_CONNECTED_AND_READY) {
                    currentState = MyService.STATE_CONNECTED_AND_READY;
                    //   logd("Broadcast received - State connected and ready");
                    changeButtonStatus();
                    showStatus("Connected and Ready");
                }

            } else if (MyService.BROADCAST_GESTURE.equals(action)) {


            } else if (MyService.BROADCAST_ERROR.equals(action)) {
                int errorNo = intent.getIntExtra(MyService.ERROR_NUMBER_DATA, 0);
                String errorMessage = intent.getStringExtra(MyService.ERROR_MESSAGE_DATA);
                showData("Error occurred. Error number - " + errorNo + " Message - " + errorMessage);
                logd("Error occurred. Error number - " + errorNo + " Message - " + errorMessage);
            }
        }
    };

    private void changeButtonStatus() {
        if (currentState == MyService.STATE_DISCONNECTED) {
            mConnectButton.setText("Connect");
            mConnectButton.setEnabled(true);
        } else if (currentState == MyService.STATE_CONNECTING) {
            mConnectButton.setText("Connecting");
            mConnectButton.setEnabled(false);
        } else if (currentState == MyService.STATE_CONNECTED) {
            mConnectButton.setText("Connecting");
            mConnectButton.setEnabled(false);
        } else if (currentState == MyService.STATE_CONNECTED_AND_READY) {
            mConnectButton.setText("Disconnect");
            mConnectButton.setEnabled(true);
        }
    }

    private IntentFilter makeNeyyaUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.BROADCAST_STATE);
        intentFilter.addAction(MyService.BROADCAST_GESTURE);
        intentFilter.addAction(MyService.BROADCAST_ERROR);
        intentFilter.addAction(MyService.BROADCAST_LOG);
        return intentFilter;
    }

    private void showStatus(String msg) {
        mStatusTextView.setText("Status - " + msg);
    }

    private void showData(String msg) {
        mStatusTextView.setText("Data - " + msg);
    }

    private void logd(final String message) {
        //  if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
        Log.d(TAG, message);
    }
}
