package com.example.mac.unlock;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class unlockpage extends AppCompatActivity {

    Button btnUnlock, btnDis;
    BluetoothDevice device = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "DeviceList";
    private BluetoothGatt bluetoothGatt = null;
    private BluetoothGattCallback mGattCallback = null;
    private BluetoothGattCharacteristic gattCharacteristic = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlockpage);
        //receive the address of the bluetooth device
        Intent newint = getIntent();
        String devString = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        device = new Gson().fromJson(devString,BluetoothDevice.class);
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mGattCallback= new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            gatt.readRemoteRssi();
                            gatt.discoverServices();
                        }//连接成功，开始搜索服务，一定要调用此方法，否则获取不到服务
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Log.e(TAG, gatt.getDevice().getName() + " write successfully");
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    String value = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        value = byte2hex(characteristic.getValue());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Log.e(TAG, gatt.getDevice().getName() + " recieved " + value);
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS){
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                            BluetoothGattService service= gatt.getService(myUUID);
//                            if (service == null)
//                            {
//                                return;
//                            }
//                            gattCharacteristic = service.getCharacteristic(myUUID);
//                            if (gattCharacteristic == null)
//                            {
//                                return;
//                            }
//                            gatt.setCharacteristicNotification(gattCharacteristic, true);
//                            BluetoothGattDescriptor descriptor = gattCharacteristic
//                                    .getDescriptor(myUUID);
//                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                            gatt.writeDescriptor(descriptor);
                            List<BluetoothGattService> serviceList = gatt.getServices();
                            for (BluetoothGattService bgs:serviceList){
                                Log.e(TAG,bgs.getUuid() + "\n" + bgs.getCharacteristic(bgs.getUuid()));
                            }
                        }
                    }
                }
            };
        connectDevice();
        }

        //view of the unlock layout
        setContentView(R.layout.activity_unlockpage);
        //call the widgtes
        btnUnlock = (Button)findViewById(R.id.button2);
        btnDis = (Button)findViewById(R.id.button3);

        btnUnlock.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                unlock();      //method to turn on
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public boolean connectDevice()
    {

        if (bluetoothGatt != null)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                return bluetoothGatt.connect();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothGatt = device.connectGatt(unlockpage.this, false, mGattCallback);
        }
        return true;
    }

//    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
//    {
//        private boolean ConnectSuccess = true; //if it's here, it's almost connected
//
//        @Override
//        protected void onPreExecute()
//        {
//            progress = ProgressDialog.show(unlockpage.this, "Connecting...", "Please wait!!!");  //show a progress dialog
//        }
//
//        @Override
//        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
//        {
//            try
//            {
//                if (btSocket == null || !isBtConnected)
//                {
//                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
//                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
//                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
//                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
//                    btSocket.connect();//start connection
//                }
//            }
//            catch (IOException e)
//            {
//                ConnectSuccess = false;//if the try failed, you can check the exception here
//            }
//            return null;
//        }
//        @Override
//        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
//        {
//            super.onPostExecute(result);
//
//            if (!ConnectSuccess)
//            {
//                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
//                finish();
//            }
//            else
//            {
//                msg("Connected.");
//                isBtConnected = true;
//            }
//            progress.dismiss();
//        }
//    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }

    private void Disconnect()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothGatt.disconnect();
        }
        finish(); //return to the first layout
    }

    private void unlock()
    {
        writeCharacteristic(gattCharacteristic, String.valueOf(R.string.code));
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic,
                                    String text) {
        if (myBluetooth == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        byte[] data = hexStringToByteArray(text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            characteristic.setValue(data);
            boolean status = bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public String byte2hex(byte[] a) {
        /*StringBuilder sb = new StringBuilder(a.length * 2);

        for (byte b : a)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();*/

        String hexString = "";

        for(int i = 0; i < a.length; i++){
            String thisByte = "".format("%x", a[i]);
            hexString += thisByte;
        }

        return hexString;

    }

}
