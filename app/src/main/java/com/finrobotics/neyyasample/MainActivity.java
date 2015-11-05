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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.finrobotics.neyyasdk.core.NeyyaDevice;

import java.util.ArrayList;

/**
 * Activity for searching Neyya device
 *
 * Created by zac on 23/09/15.
 */
public class MainActivity extends AppCompatActivity {
    private static String TAG = "NeyyaSDK";
    private MyService mMyService;
    private TextView mStatusTextView;
    private boolean mScanning = false;
    private DeviceListAdapter mDeviceListAdapter;
    private Intent neyyaServiceIntent;
    private MenuItem searchMenuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
        ListView mDeviceListView = (ListView) findViewById(R.id.deviceListView);
        mDeviceListAdapter = new DeviceListAdapter();
        mDeviceListView.setAdapter(mDeviceListAdapter);
        mDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ConnectActivity.class);
                intent.putExtra("SELECTED_DEVICE", mDeviceListAdapter.getDevice(position));
                startActivity(intent);
            }
        });

        mStatusTextView.setText("Disconnected");
        neyyaServiceIntent = new Intent(this, MyService.class);
        // bindService(neyyaServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        startService(neyyaServiceIntent);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        searchMenuItem = menu.findItem(R.id.action_test);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_test) {
            if (!mScanning) {
                //Start search
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_SEARCH);
                sendBroadcast(intent);
            } else {
                //Stop search
                final Intent intent = new Intent(MyService.BROADCAST_COMMAND_STOP_SEARCH);
                sendBroadcast(intent);
            }
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //logd("On Resume Main Activity");
        registerReceiver(mNeyyaUpdateReceiver, makeNeyyaUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        //logd("On Pause Main Activity");
        unregisterReceiver(mNeyyaUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        // unbindService(mServiceConnection);
        stopService(neyyaServiceIntent);
        logd("On destroy MainActivity");
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

    /**
     * Broadcast receiver for getting data from Neyya SDK
     */
    private final BroadcastReceiver mNeyyaUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            //If received data is state of Neyya base service
            if (MyService.BROADCAST_STATE.equals(action)) {
                int status = intent.getIntExtra(MyService.DATA_STATE, 0);
                if (status == MyService.STATE_DISCONNECTED) {
                    showStatus("Disconnected");
                } else if (status == MyService.STATE_SEARCHING) {
                    showStatus("Searching");
                    searchMenuItem.setTitle("Stop Search");
                    mDeviceListAdapter.clear();
                    mDeviceListAdapter.notifyDataSetChanged();
                    mScanning = true;
                } else if (status == MyService.STATE_SEARCH_FINISHED) {
                    showStatus("Searching finished");
                    searchMenuItem.setTitle("Start Search");
                    mScanning = false;
                } else {
                    showStatus(status + "");
                }

            // If received data is list of found devices
            } else if (MyService.BROADCAST_DEVICES.equals(action)) {
                mDeviceListAdapter.setDevices((ArrayList<NeyyaDevice>) intent.getSerializableExtra(MyService.DATA_DEVICE_LIST));
                mDeviceListAdapter.notifyDataSetChanged();

            //If received data is error
            } else if (MyService.BROADCAST_ERROR.equals(action)) {
                int errorNo = intent.getIntExtra(MyService.DATA_ERROR_NUMBER, 0);
                String errorMessage = intent.getStringExtra(MyService.DATA_ERROR_MESSAGE);
                logd("Error occurred. Error number - " + errorNo + " Message - " + errorMessage);
            }
        }
    };

    private void showStatus(String message) {
        mStatusTextView.setText("Status - " + message);
    }

    /**
     * Adapter to show found devices on an ArrayList
     */
    private class DeviceListAdapter extends BaseAdapter {
        private ArrayList<NeyyaDevice> mDevices;
        private LayoutInflater mInflator;


        public DeviceListAdapter() {
            super();
            mDevices = new ArrayList<>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void setDevices(ArrayList<NeyyaDevice> devices) {
            this.mDevices = devices;
        }

        public NeyyaDevice getDevice(int position) {
            return mDevices.get(position);
        }

        public void clear() {
            mDevices.clear();
        }

        @Override
        public int getCount() {
            return mDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            NeyyaDevice device = mDevices.get(position);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
    }

    /**
     * ViewHolder class for holding data in ArrayList
     */
    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }

    /**
     * Intent filter generator for registering broadcast receiver
     * @return
     */
    private IntentFilter makeNeyyaUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyService.BROADCAST_STATE);
        intentFilter.addAction(MyService.BROADCAST_DEVICES);
        intentFilter.addAction(MyService.BROADCAST_ERROR);
        intentFilter.addAction(MyService.BROADCAST_LOG);
        return intentFilter;
    }

    private void logd(final String message) {
        //  if (com.finrobotics.neyyasdk.BuildConfig.DEBUG)
        Log.d(TAG, message);
    }
}
