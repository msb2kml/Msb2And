package org.js.Msb2And;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by js on 4/6/18.
 */

public class comRx {

    private MyHandler mHandler;
    private static Long lastSent;
    private static Long logTime;
    private static SensorReading[] allSensor;
    private UsbService usbService;
    private Timer myTimer;

    private static Handler topCom;
    public static DispRecord disp;
    private Context context;
    public static byte probed;
    private static boolean initialized;

    public static final int MSG_TMOUT=10;

    public void Rx(Context context, Handler topCom, DispRecord disp){
        this.context=context;
        this.topCom=topCom;
        this.disp=disp;
        allSensor=new SensorReading[16];
        logTime=null;
        lastSent=null;
        mHandler=new MyHandler(this);
        setFilters();
        probed=-1;
        startService(UsbService.class, usbConnection,null);
        MyTimerTask myTask=new MyTimerTask();
        myTimer=new Timer(true);
        myTimer.schedule(myTask,10000,3000);
        initialized=true;
        return;
    }

    public void close(){
        if (initialized) {
            myTimer.cancel();
            context.unbindService(usbConnection);
            usbService.stopSelf();
            initialized=false;
        }
        return;
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection,
                              Bundle extras){
        if (!UsbService.SERVICE_CONNECTED){
            Intent startSer=new Intent(context,service);
            if (extras !=null && !extras.isEmpty()){
                Set<String> keys=extras.keySet();
                for (String key : keys){
                    String extra=extras.getString(key);
                    startSer.putExtra(key,extra);
                }
            }
            context.startService(startSer);
        }
        Intent bindingIntent=new Intent(context,service);
        context.bindService(bindingIntent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection usbConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            usbService=((UsbService.UsbBinder) service).getService();
            usbService.setBaudRate(38400);
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            usbService=null;
        }
    };

    private class MyTimerTask extends TimerTask{
        public void run(){
            if (mHandler!=null){
                mHandler.obtainMessage(MSG_TMOUT).sendToTarget();
            }
        }
    }

    public static void store(long tim, byte[] s){
        int addr=(int)((s[0]&0xff)>>>4);
        if (addr<0 || addr>15) return;
        int cls=(int)(0xf & s[0]);
        if (cls<=0 || cls>13) return;
        if (s[1]==0x80 && s[2]==0x0) return;
        SensorReading sensor=new SensorReading();
        sensor.addr=addr;
        sensor.v_class=cls;
        sensor.alarm=(0x1 & s[1])==1;
        short low=(short)(0xfe&s[1]);
        short high=(short)((0xff&s[2])<<8);
        sensor.value=(short)((high|low)>>1);
        if (logTime==null) logTime=tim;
        if (Math.abs(sensor.value)<16000) sensor.valid=true;
        sensor.inTime=tim;
        allSensor[addr]=sensor;
        return;
    }

    private static class MyHandler extends Handler{

        private final WeakReference<comRx> mActivity;
        private Long startWhen=null;

        public MyHandler(comRx activity){
            mActivity=new WeakReference<comRx>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            Message upMsg;
            Long now=System.currentTimeMillis();
            if (msg.what == UsbService.MESSAGE_FROM_SERIAL_PORT){
                TimBytes timBytes=(TimBytes) msg.obj;
                byte[] arg0=timBytes.getData();
                int l=arg0.length;
                switch (l){
                    case 1:
//                           probe
                        if (arg0[0]>=0 && arg0[0]<16) comRx.probed=arg0[0];
                        else comRx.probed=-1;
                        break;
                    case 3:
//                           data from sensor
                        int addr=(int)((arg0[0]&0xff)>>>4);
                        if (addr<0 || addr>15 || addr!=comRx.probed){
                            comRx.probed=-1;
                            return;
                        }
                        Long when = timBytes.getWhen();
                        if (comRx.allSensor[addr]!=null){
                            if (comRx.logTime==null) comRx.logTime=now;
                            if (comRx.lastSent==null) comRx.lastSent=now;
                            RecordReading fullSensor=new RecordReading();
                            fullSensor.logTime=when-comRx.logTime;
                            fullSensor.record=comRx.allSensor;
                            comRx.disp.dispRecord(fullSensor,now-5000);
                            Arrays.fill(comRx.allSensor,null);
                            comRx.lastSent=now;
                        }
                        store(when,arg0);
                        comRx.probed=-1;
                        break;
                    default:
//                           from GPS?
                        comRx.probed=-1;
                        break;

                }
            } else if (msg.what == MSG_TMOUT){
               if (comRx.lastSent!=null && now-comRx.lastSent>3000){
                   RecordReading fullSensor=new RecordReading();
                   fullSensor.logTime=now-comRx.logTime;
                   comRx.disp.dispRecord(fullSensor,now-3000);
                   comRx.lastSent=now;
                   comRx.probed=-1;
                   topCom.obtainMessage(1,(Object)false).sendToTarget();
               }
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg;
            switch (intent.getAction()){
                case UsbService.ACTION_USB_PERMISSION_GRANTED:
                    Toast.makeText(context,"USB ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED:
                    Toast.makeText(context,"USB permission not granted",
                            Toast.LENGTH_LONG).show();
                    msg=topCom.obtainMessage(0);
                    topCom.sendMessage(msg);
                    break;
                case UsbService.ACTION_NO_USB:
                    Toast.makeText(context,"No USB connected",Toast.LENGTH_LONG).show();
                    msg=topCom.obtainMessage(0);
                    topCom.sendMessage(msg);
                    break;
                case UsbService.ACTION_USB_DISCONNECTED:
                    Toast.makeText(context,"USB disconnected",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED:
                    Toast.makeText(context,"USB device not supported",
                            Toast.LENGTH_LONG).show();
                    msg=topCom.obtainMessage(0);
                    topCom.sendMessage(msg);
                    break;
            }

        }
    };

    private void setFilters(){
        IntentFilter filter=new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        context.registerReceiver(mUsbReceiver,filter);
    }
}
