package com.zxdmjr.bluetoothdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.zxdmjr.bluetoothdemo.adapters.DeviceListAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener {

    private static final String TAG = "MainActivity";

    private Button btnONOFF;
    private Button btnDiscoverable;
    private Button btnDiscover;
    private Button btnConnection;
    private Button btnSend;

    private EditText etMessage;

    private ListView lvDevices;

    private List<BluetoothDevice> devices = new ArrayList<>();
    private DeviceListAdapter adapter;

    private BluetoothDevice mDevice;

    private BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver mBroastCastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);
                
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "onReceive: STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: STATE_ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "onReceive: STATE_TURNING_ON");
                        break;
                    
                }
                
            }
        }
    };

    private final BroadcastReceiver mBroastCastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, bluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "onReceive: SCAN_MODE_CONNECTABLE_DISCOVERABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "onReceive: SCAN_MODE_CONNECTABLE");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "onReceive: SCAN_MODE_NONE");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "onReceive: STATE_CONNECTING");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "onReceive: STATE_CONNECTED");
                        break;

                }

            }
        }
    };

    private final BroadcastReceiver mBroastCastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                devices.add(device);
                adapter = new DeviceListAdapter(context, R.layout.simple_device_item, devices);
                lvDevices.setAdapter(adapter);
            }
        }
    };

    private final BroadcastReceiver mBroastCastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases
                //case 1: bonded already
                if(device.getBondState() == BluetoothDevice.BOND_BONDED){
                    mDevice = device;
                }
                //case 2: create a bond
                if(device.getBondState() == BluetoothDevice.BOND_BONDING){

                }
                //case 3: breaking a bond
                if(device.getBondState() == BluetoothDevice.BOND_NONE){

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initial
        btnONOFF = findViewById(R.id.btnONOFF);
        btnDiscoverable = findViewById(R.id.btnDiscoverable);
        btnDiscover = findViewById(R.id.btnDiscover);

        lvDevices = findViewById(R.id.lvDevices);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        lvDevices.setOnItemClickListener(this);

        IntentFilter bondIntentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroastCastReceiver4, bondIntentFilter);

        btnONOFF.setOnClickListener(view -> {
            enableOrDisableBT();
        });

        btnDiscoverable.setOnClickListener(view -> {
            discoverable();
        });

        btnDiscover.setOnClickListener(view -> {
            doDiscover();
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroastCastReceiver1);
        unregisterReceiver(mBroastCastReceiver2);
        unregisterReceiver(mBroastCastReceiver3);
        unregisterReceiver(mBroastCastReceiver4);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        bluetoothAdapter.cancelDiscovery();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            devices.get(position).createBond();
        }
    }

    private void doDiscover() {

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();

            checkForPermission();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroastCastReceiver3, discoverIntentFilter);
        }

        if(!bluetoothAdapter.isDiscovering()){
            checkForPermission();
            bluetoothAdapter.startDiscovery();
            IntentFilter discoverIntentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroastCastReceiver3, discoverIntentFilter);
        }

    }

    private void checkForPermission() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {}
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {}
        }).check();

    }

    private void discoverable() {

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter discoverableIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroastCastReceiver2, discoverableIntentFilter);

    }


    private void enableOrDisableBT() {
        if(bluetoothAdapter == null){
            Log.d(TAG, "enableOrDisableBT: does not support bluetooth");
            return;
        }

        if(!bluetoothAdapter.isEnabled()){
            Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(bluetoothEnableIntent);

            IntentFilter btChangeIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroastCastReceiver1, btChangeIntentFilter);
        }

        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();

            IntentFilter btChangeIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroastCastReceiver1, btChangeIntentFilter);
        }
    }


}
