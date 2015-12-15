package com.finrobotics.neyyasample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.finrobotics.neyyasdk.core.Gesture;
import com.finrobotics.neyyasdk.core.NeyyaDevice;
import com.finrobotics.neyyasdk.core.Settings;

/**
 * Activity for handling the connection, receiving gestures and sending settings
 * Created by zac on 25/09/15.
 */
public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = "NeyyaSDK";
    private static int currentState = MyService.STATE_DISCONNECTED;

    private MyService mMyService;
    private TextView mNameTextView, mAddressTextView, mStatusTextView, mDataTextView;
    private Button mNameChangeButton, mLeftHandButton, mRightHandButton, mSlowButton, mMediumButton, mFastButton;
    private NeyyaDevice mSelectedDevice;
    private EditText mRingNameEditText;
    private String tempName = "";
    private MenuItem connectMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connectactivity);
        currentState = MyService.STATE_DISCONNECTED;
        mNameTextView = (TextView) findViewById(R.id.nameTextView);
        mAddressTextView = (TextView) findViewById(R.id.addressTextView);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
        mDataTextView = (TextView) findViewById(R.id.dataTextView);
        mRingNameEditText = (EditText) findViewById(R.id.ringNameEditText);
        mNameChangeButton = (Button) findViewById(R.id.ringNameChangeButton);

        //Change ring name
        mNameChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData("");
                Settings settings = new Settings();
                tempName = mRingNameEditText.getText().toString();
                if (tempName.equals("")) {
                    Toast.makeText(ConnectActivity.this, "Type name", Toast.LENGTH_SHORT).show();
                } else {
                    settings.setRingName(tempName);
                    final Intent intent = new Intent(MyService.BROADCAST_COMMAND_SETTINGS);
                    intent.putExtra(MyService.DATA_SETTINGS, settings);
                    sendBroadcast(intent);
                }
            }
        });

        //Change hand preference to left hand
        mLeftHandButton = (Button) findViewById(R.id.leftHandButton);
        mLeftHandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData("");
                Settings settings = new Settings();
                settings.setHandPreference(Settings.LEFT_HAND);
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_SETTINGS);
                intent.putExtra(MyService.DATA_SETTINGS, settings);
                sendBroadcast(intent);
            }
        });

        //Change hand preference to right hand
        mRightHandButton = (Button) findViewById(R.id.rightHandButton);
        mRightHandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData("");
                Settings settings = new Settings();
                settings.setHandPreference(Settings.RIGHT_HAND);
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_SETTINGS);
                intent.putExtra(MyService.DATA_SETTINGS, settings);
                sendBroadcast(intent);
            }
        });

        //Change gesture speed to slow
        mSlowButton = (Button) findViewById(R.id.slowButton);
        mSlowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData("");
                Settings settings = new Settings();
                settings.setGestureSpeed(Settings.SPEED_SLOW);
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_SETTINGS);
                intent.putExtra(MyService.DATA_SETTINGS, settings);
                sendBroadcast(intent);
            }
        });

        //Change gesture speed to medium
        mMediumButton = (Button) findViewById(R.id.mediumButton);
        mMediumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showData("");
                Settings settings = new Settings();
                settings.setGestureSpeed(Settings.SPEED_MEDIUM);
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_SETTINGS);
                intent.putExtra(MyService.DATA_SETTINGS, settings);
                sendBroadcast(intent);
            }
        });

        //Change gesture speed to fast
        mFastButton = (Button) findViewById(R.id.fastButton);
        mFastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showData("");
                Settings settings = new Settings();
                settings.setGestureSpeed(Settings.SPEED_FAST);
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_SETTINGS);
                intent.putExtra(MyService.DATA_SETTINGS, settings);
                sendBroadcast(intent);
            }
        });

        Intent intent = getIntent();
        mSelectedDevice = (NeyyaDevice) intent.getSerializableExtra("SELECTED_DEVICE");
        if (mSelectedDevice != null) {
            setName(mSelectedDevice.getName());
            setAddress(mSelectedDevice.getAddress());
        } else {
            setName("UNKNOWN");
            setAddress("UNKNOWN");
        }

        Intent neyyaServiceIntent = new Intent(this, MyService.class);
        startService(neyyaServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.connectmenu, menu);
        connectMenuItem = menu.findItem(R.id.action_connect);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connect) {
            showData("");
            if (currentState == MyService.STATE_DISCONNECTED || currentState == MyService.STATE_SEARCH_FINISHED) {
                //Start the connection process
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_CONNECT);
                intent.putExtra(MyService.DATA_DEVICE, mSelectedDevice);
                sendBroadcast(intent);
            } else if (currentState == MyService.STATE_CONNECTED_AND_READY) {
                //Disconnect from Neyya device
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_DISCONNECT);
                sendBroadcast(intent);
            }
        }


        return super.onOptionsItemSelected(item);
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
        logd("On destroy Connect Activity");
        super.onDestroy();
    }

    /**
     * Broadcast receiver for getting data from SDK
     */
    private final BroadcastReceiver mNeyyaUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            //If the received data is state of the SDK service
            if (MyService.BROADCAST_STATE.equals(action)) {
                int status = intent.getIntExtra(MyService.DATA_STATE, 0);
                if (status == MyService.STATE_DISCONNECTED) {
                    currentState = MyService.STATE_DISCONNECTED;
                    changeButtonStatus();
                    showStatus("Disconnected");
                } else if (status == MyService.STATE_CONNECTING) {
                    currentState = MyService.STATE_CONNECTING;
                    changeButtonStatus();
                    showStatus("Connecting");
                } else if (status == MyService.STATE_CONNECTED) {
                    currentState = MyService.STATE_CONNECTED;
                    changeButtonStatus();
                    showStatus("Connected");
                } else if (status == MyService.STATE_CONNECTED_AND_READY) {
                    currentState = MyService.STATE_CONNECTED_AND_READY;
                    changeButtonStatus();
                    showStatus("Connected and Ready");
                }else if(status == MyService.STATE_AUTO_SEARCHING) {
                    currentState = MyService.STATE_AUTO_SEARCHING;
                    changeButtonStatus();
                    showStatus("Auto searching");
                }

            //If the received data is gesture
            } else if (MyService.BROADCAST_GESTURE.equals(action)) {
                int gesture = intent.getIntExtra(MyService.DATA_GESTURE, 0);
                showData(Gesture.parseGesture(gesture));

            //If the received data is error
            } else if (MyService.BROADCAST_ERROR.equals(action)) {
                int errorNo = intent.getIntExtra(MyService.DATA_ERROR_NUMBER, 0);
                String errorMessage = intent.getStringExtra(MyService.DATA_ERROR_MESSAGE);
                showData("Error occurred. Error number - " + errorNo + " Message - " + errorMessage);
                logd("Error occurred. Error number - " + errorNo + " Message - " + errorMessage);

            //If the received data is info of an action
            } else if (MyService.BROADCAST_INFO.equals(action)) {
                int status = intent.getIntExtra(MyService.DATA_INFO, 0);
                switch (status) {
                    case MyService.STATUS_RING_NAME_CHANGE_SUCCESS:
                        showData("Ring name changed");
                        setName(tempName);
                        break;
                    case MyService.STATUS_RING_NAME_CHANGE_FAILED:
                        showData("Ring name change failed");
                        break;
                    case MyService.STATUS_HAND_CHANGE_SUCCESS:
                        showData("Hand changed");
                        break;
                    case MyService.STATUS_HAND_CHANGE_FAILED:
                        showData("Hand change failed");
                        break;
                    case MyService.STATUS_GESTURE_SPEED_CHANGE_SUCCESS:
                        showData("Gesture speed changed");
                        break;
                    case MyService.STATUS_GESTURE_SPEED_CHANGE_FAILED:
                        showData("Gesture speed change failed");
                        break;
                }

            }
        }
    };

    /**
     * Function to change the status of buttons and UI according to the Bluetooth state
     */
    private void changeButtonStatus() {
        if (currentState == MyService.STATE_DISCONNECTED) {
            connectMenuItem.setTitle("Connect");
            connectMenuItem.setEnabled(true);
            enableSettings(false);
        } else if (currentState == MyService.STATE_CONNECTING) {
            connectMenuItem.setTitle("Connecting");
            connectMenuItem.setEnabled(false);

        } else if (currentState == MyService.STATE_CONNECTED) {
            connectMenuItem.setTitle("Connecting");
            connectMenuItem.setEnabled(false);

        } else if (currentState == MyService.STATE_CONNECTED_AND_READY) {
            connectMenuItem.setTitle("Disconnect");
            connectMenuItem.setEnabled(true);
            enableSettings(true);
        }
        else if (currentState == MyService.STATE_AUTO_SEARCHING) {
            connectMenuItem.setTitle("Disconnect");
            connectMenuItem.setEnabled(true);
            enableSettings(false);
        }
    }

    /**
     * Generate intent filter for thr broadcast receiver for getting data from SDK service
     * @return IntentFilter
     */
    private IntentFilter makeNeyyaUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.BROADCAST_STATE);
        intentFilter.addAction(MyService.BROADCAST_GESTURE);
        intentFilter.addAction(MyService.BROADCAST_ERROR);
        intentFilter.addAction(MyService.BROADCAST_INFO);
        return intentFilter;
    }

    /**
     * Function to enable and disable the UI components according to connection status.
     * @param status boolean, enable or disable
     */
    private void enableSettings(boolean status) {
        mNameChangeButton.setEnabled(status);
        mRingNameEditText.setEnabled(status);
        mLeftHandButton.setEnabled(status);
        mRightHandButton.setEnabled(status);
        mSlowButton.setEnabled(status);
        mMediumButton.setEnabled(status);
        mFastButton.setEnabled(status);
    }

    /**
     * Change the name of ring on UI
     * @param name String name to be changed
     */
    private void setName(String name) {
        mNameTextView.setText("Name - " + name);
    }

    /**
     * Change the bluetooth MAC address of the ring on UI
     * @param address String address
     */
    private void setAddress(String address) {
        mAddressTextView.setText("Address - " + address);
    }

    /**
     * Show the current status on Ui
     * @param msg String message
     */
    private void showStatus(String msg) {
        mStatusTextView.setText("Status - " + msg);
    }

    /**
     * Show the received data, gesture and log on UI
     * @param msg String message
     */
    private void showData(String msg) {
        mDataTextView.setText("Data - " + msg);
    }

    private void logd(final String message) {
        //  if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
        Log.d(TAG, message);
    }
}
