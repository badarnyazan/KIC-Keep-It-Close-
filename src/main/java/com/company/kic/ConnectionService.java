package com.company.kic;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Arrays;
import java.util.UUID;


public class ConnectionService extends Service {

    private static final String TAG = ConnectionService.class.getSimpleName();
    private static final UUID UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID RX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID UART_NOTIFICATION_DESCRIPTOR_UUID =UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private IBinder mBinder;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mConnectedDevice;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback mGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mCallBack.onConnected();
                Log.d(TAG, "Attempting to start service discovery: " + gatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mCallBack.onDisConnected();
                Log.d(TAG," disconnected");
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Following services discovered ");
                for(BluetoothGattService gattService:gatt.getServices()){
                    if(gattService.getUuid().equals(UART_SERVICE_UUID)){
                       // if(gattService.getCharacteristic(RX_CHAR_UUID).)
                        BluetoothGattCharacteristic mChar = gattService.getCharacteristic(RX_CHAR_UUID);
                        if(mChar == null){
                           Log.d(TAG,"RX char not found,one is ");
                           return;
                        }
                        mBluetoothGatt.setCharacteristicNotification(mChar,true);

                        BluetoothGattDescriptor descriptor = mChar.getDescriptor(UART_NOTIFICATION_DESCRIPTOR_UUID);
                        if(descriptor == null){
                            Log.d(TAG,"Descriptor is not found ");
                            return;
                        }
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(descriptor);
                        Log.d(TAG,"desired service found");
                    }
                    Log.d(TAG,"Service found which has UUID => "+gattService.getUuid());
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
             Log.d(TAG,characteristic.getUuid().toString()+" unable to read characteristic,status is "+status);
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicsWrite called and status is " + status);
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            byte[] bytes=characteristic.getValue();
            for (byte b : bytes) {
                Log.d(TAG, "Each received byte is " + String.format("0x%x ", b));
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            broadCastUpdate(rssi);
            Log.d(TAG,"RSSI value is "+rssi);
        }
    };
     public void readRSSIValue(){
         if(mBluetoothGatt != null){
             if(mBluetoothGatt.readRemoteRssi()){
                 Log.d(TAG,"RSSI value reading");
             }else {
                 Log.d(TAG,"Failed to read RSSI value");
             }
         }
     }
    public class MyBinder extends Binder {
        public ConnectionService getUartService(){
            Log.d(TAG,"UartService instance return_icon from MyBinder");
            return ConnectionService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(mBluetoothAdapter==null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if(mBluetoothAdapter==null){
                Log.d(TAG,"Unable to initilaized bluetooth adapter and quitting service.");
                stopSelf();
                return;
            }
        }
        mBinder=new MyBinder();
        Log.d(TAG,"onCreate method called");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStart Command method called");
        return START_NOT_STICKY;
    }
    @Override
    public void onDestroy() {
        Log.d(TAG,"Uart service onDestroy method called");
        if(mBluetoothGatt==null){
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt=null;

        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind method called");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"onUnbind method called");
        return super.onUnbind(intent);
    }
    private void broadCastUpdate(int value){
        Intent intent = new Intent();
        intent.setAction(BroadsastActions.ACTION_READ_RSSI);
        intent.putExtra("value",value);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        mConnectedDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (mConnectedDevice == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = mConnectedDevice.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }
    public boolean disConnect(){
        if(mBluetoothGatt==null){
            Log.d(TAG,"bluetooth Gat is null");
            return false;
        }
        mBluetoothGatt.disconnect();
        return true;
    }
    public void registerActivity(Activity activity){
        try {
            this.mCallBack = (MainActivity) activity;
            Log.d(TAG,"Home activity Registered");
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }
    private ConnectionServiceCallBack mCallBack;
    public interface ConnectionServiceCallBack{
        void notifyActivity(String msg);
        void onConnected();
        void onDisConnected();
    }
    public BluetoothDevice getConnectedDevice(){
        if(mConnectedDevice != null){
            return mConnectedDevice;
        }
        return null;
    }
}
