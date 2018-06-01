package org.js.Msb2And;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by js on 4/3/18.
 */

public class UsbService extends Service {

    public static final String ACTION_USB_ATTACHED=
            "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED=
            "android.hardware.usb.action.USB_DEVICE_DETACHED";
    public static final String ACTION_USB_PERMISSION=
            "com.android.example.USB_PERMISSION";

    public static final String ACTION_NO_USB="org.js.usbservice.NO_USB";
    public static final String ACTION_USB_PERMISSION_GRANTED=
            "org.js.usbservice.USB_PERMISSION_GRANTED";
    public static final String ACTION_USB_PERMISSION_NOT_GRANTED=
            "org.js.usbservice.USB_PERMISSION_NOT_GRANTED";
    public static final String ACTION_USB_DISCONNECTED=
            "org.js.usbservice.USB_DISCONNECTED";
    public static final String ACTION_USB_NOT_SUPPORTED=
            "org.js.usbservice.USB_NOT_SUPPORTED";
    public static final String ACTION_USB_DEVICE_NOT_WORKING=
            "org.js.usbservice.USB_DEVICE_NOT_WORKING";
    public static final String ACTION_USB_READY=
            "org.js.usbservice.USB_READY";
    public static final int MESSAGE_FROM_SERIAL_PORT=0;

    private IBinder binder = new UsbBinder();

    private Context context;
    private Handler mHandler;
    private UsbManager usbManager;
    private UsbDevice device;
    private UsbDeviceConnection connection;
    private UsbSerialDevice serialPort;
    private boolean serialPortConnected;
    private static int BAUD_RATE=115200;

    public static boolean SERVICE_CONNECTED=false;

    private UsbSerialInterface.UsbReadCallback mCallback=new UsbSerialInterface.UsbReadCallback() {
        @Override
        public void onReceivedData(byte[] bytes) {
            int l=bytes.length;
            if (l>0){
                TimBytes tim=new TimBytes();
                tim.when=System.currentTimeMillis();
                tim.data=bytes;
                if (mHandler!=null)
                    mHandler.obtainMessage(MESSAGE_FROM_SERIAL_PORT,tim).sendToTarget();
            }
        }
    };

    @Override
    public void onCreate(){
        this.context=this;
        serialPortConnected=false;
        UsbService.SERVICE_CONNECTED=true;
        setFilter();
        usbManager=(UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(usbReceiver);
        if (connection!=null) connection.close();
        UsbService.SERVICE_CONNECTED=false;
    }

    public void setHandler(Handler mHandler){
        this.mHandler=mHandler;
    }

    public void setBaudRate(int baud){
        this.BAUD_RATE=baud;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    private final BroadcastReceiver usbReceiver=new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_GRANTED);
                    arg0.sendBroadcast(intent);
                    connection = usbManager.openDevice(device);
                    new ConnectionThread().start();
                } else {
                    Intent intent = new Intent(ACTION_USB_PERMISSION_NOT_GRANTED);
                    arg0.sendBroadcast(intent);
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                if (!serialPortConnected) findSerialPortDevice();
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                Intent intent = new Intent(ACTION_USB_DISCONNECTED);
                arg0.sendBroadcast(intent);
                if (serialPortConnected) serialPort.close();
                serialPortConnected = false;
            }
        }
    };

    private class ConnectionThread extends Thread {

        @Override
        public void run() {
            serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
            if (serialPort != null) {
                if (serialPort.open()) {
                    serialPortConnected = true;
                    serialPort.setBaudRate(BAUD_RATE);
                    serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                    serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                    serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                    serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                    serialPort.read(mCallback);
                    Intent intent = new Intent(ACTION_USB_READY);
                    context.sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(ACTION_USB_DEVICE_NOT_WORKING);
                    context.sendBroadcast(intent);
                }
            } else {
                Intent intent = new Intent(ACTION_USB_NOT_SUPPORTED);
                context.sendBroadcast(intent);
            }
        }
    }

    private void setFilter(){
        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver,filter);
    }

    private void findSerialPortDevice(){
        HashMap<String ,UsbDevice> usbDevices=usbManager.getDeviceList();
        if (!usbDevices.isEmpty()){
            boolean keep=true;
            for (Map.Entry<String,UsbDevice> entry : usbDevices.entrySet()){
                device=entry.getValue();
                int deviceVID=device.getVendorId();
                int devicePID=device.getProductId();
                if (deviceVID==getResources().getInteger(R.integer.MPX_VID) &&
                        devicePID==getResources().getInteger(R.integer.MPX_PID)){
                    requestUserPermission();
                    keep=false;
                } else {
                    connection=null;
                    device=null;
                }
                if (!keep) break;
            }
            if (keep){
                Intent intent=new Intent(ACTION_NO_USB);
                sendBroadcast(intent);
            }
        } else {
            Intent intent=new Intent(ACTION_NO_USB);
            sendBroadcast(intent);
        }
    }

    private void requestUserPermission(){
        PendingIntent mPendingIntent =PendingIntent.getBroadcast(this,
                0,new Intent(ACTION_USB_PERMISSION),0);
        usbManager.requestPermission(device,mPendingIntent);
    }
}
