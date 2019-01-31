package com.zxdmjr.bluetoothdemo.service;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    private static final String appName = "MyApp";

    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private final BluetoothAdapter mBluetoothAdapter; // = BluetoothAdapter.getDefaultAdapter();
    private Context context;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private BluetoothDevice mDevice;
    private UUID deviceUUID;
    private ProgressDialog mProgressDialog;

    public BluetoothConnectionService(Context context) {
        this.context = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    private class AcceptThread extends Thread{

        private final BluetoothServerSocket mServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;
            try{
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);
            } catch (Exception e){
                Log.d(TAG, "AcceptThread: "+e.getLocalizedMessage());
            }
            mServerSocket = tmp;
        }

        @Override
        public void run() {
            super.run();

            BluetoothSocket socket = null;

            try{
                socket = mServerSocket.accept();
            } catch (Exception e){
                Log.d(TAG, "run: "+e.getLocalizedMessage());
            }

            if(socket != null){
                connected(socket, mDevice);
            }
        }

        public void cancel(){
            try{
                mServerSocket.close();
            } catch (Exception e){
                Log.d(TAG, "cancel: "+e.getLocalizedMessage());
            }
        }
    }

    private class ConnectThread extends Thread{
        private BluetoothSocket socket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            mDevice = device;
            deviceUUID = uuid;
        }

        @Override
        public void run() {
            super.run();
            BluetoothSocket tmp = null;
            try{
                tmp = mDevice.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (Exception e){
                Log.d(TAG, "run: "+e.getLocalizedMessage());
            }
            socket = tmp;
            mBluetoothAdapter.cancelDiscovery();

            try {
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            connected(socket, mDevice);
        }

        public void cancel(){
            try{
                socket.close();
            }catch (Exception e){
                Log.d(TAG, "cancel: "+e.getLocalizedMessage());
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread(BluetoothSocket socket){
            mSocket = socket;

            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;

            try{
                tmpInputStream = mSocket.getInputStream();
                tmpOutputStream = mSocket.getOutputStream();
                mProgressDialog.dismiss();
            } catch (Exception e){
                Log.d(TAG, "ConnectedThread: "+e.getLocalizedMessage());
            }
            mInputStream = tmpInputStream;
            mOutputStream = tmpOutputStream;
        }

        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;

            while (true){
                try{
                    bytes = mInputStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                } catch (Exception e){
                    Log.d(TAG, "run: "+e.getLocalizedMessage());
                    break;
                }
            }
        }

        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            try{
                mOutputStream.write(bytes);
            } catch (Exception e){
                Log.d(TAG, "write: "+e.getLocalizedMessage());
            }
        }

        public void cancel(){
            try{
                mSocket.close();
                mInputStream.close();
                mOutputStream.close();
            } catch (Exception e){
                Log.d(TAG, "cancel: "+e.getLocalizedMessage());
            }
        }
    }

    public synchronized void start(){
        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }

        if(acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){
        mProgressDialog = ProgressDialog.show(context, "Connecting bluetooth...",
                "Please wait...", true);

        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }

    private void connected(BluetoothSocket socket, BluetoothDevice device){
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
    }

    public void write(byte[] out){
        ConnectedThread r;
        connectedThread.write(out);
    }
}
