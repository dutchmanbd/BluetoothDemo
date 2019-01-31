package com.zxdmjr.bluetoothdemo.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zxdmjr.bluetoothdemo.R;
import java.util.List;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mLayoutInflator;
    private List<BluetoothDevice> mDevices;
    private int mViewResourceId;


    public DeviceListAdapter(Context context, int resource,List<BluetoothDevice> devices) {
        super(context, resource, devices);
        mViewResourceId = resource;
        mDevices = devices;
        mLayoutInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position,View convertView,ViewGroup parent) {

        convertView = mLayoutInflator.inflate(mViewResourceId, null);
        BluetoothDevice device = mDevices.get(position);

        if(device != null){
            TextView tvDeviceName = convertView.findViewById(R.id.tv_device_name);
            TextView tvDeviceAddress = convertView.findViewById(R.id.tv_device_address);

            if(tvDeviceName != null)
                tvDeviceName.setText(device.getName());
            if(tvDeviceAddress != null)
                tvDeviceAddress.setText(device.getAddress());
        }

        return convertView;
    }
}
