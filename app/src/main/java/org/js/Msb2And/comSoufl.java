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
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by js on 4/11/18.
 */

public class comSoufl {

    private MyHandler mHandler;
    private UsbService usbService;
    private Context context;
    private static Handler topCom;
    private static TextView listing;
    private static Long logTime;
    private static SensorReading[] allSensor;
    private static Long lastSent;
    public static DispRecord disp;
    private Timer myTimer;
    private boolean initialized;

    public static final int MSG_TMOUT=10;

    public void Soufl(Context context, Handler topCom, DispRecord disp){

        this.context=context;
        this.topCom=topCom;
        this.disp=disp;
        allSensor=new SensorReading[16];
        logTime=null;
        lastSent=null;
        mHandler=new MyHandler(this);
        setFilters();
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
            if (extras != null && !extras.isEmpty()){
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

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            usbService = ((UsbService.UsbBinder) service).getService();
            usbService.setBaudRate(115200);
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            usbService = null;
        }
    };

    private class MyTimerTask extends TimerTask {
        public void run(){
            if (mHandler!=null){
                mHandler.obtainMessage(MSG_TMOUT).sendToTarget();
            }
        }
    }

    public static ByteBuffer unesc(byte[] buf){
        ByteBuffer u=ByteBuffer.allocate(buf.length);
        int i=0;
        int l=buf.length;
        while (i<l){
            byte b=buf[i];
            if ((b == 0x1b) && i<l-1) {
                i++;
                u.put((byte)(buf[i] & 0x1b));
            } else u.put(b);
            i++;
        }
        return u;
    }

    public static boolean checks(ByteBuffer record){
        byte[] ar=record.array();
        int l=record.limit();
        int check=0;
        for (int i=1;i<l-2;i++){
            int x=(0xff & ar[i]);
            check+=x;
        }
        check=~check;
        byte c=ar[l-2];
        if ((byte)(0xff & check) == c) return true;
        return false;
    }

    public static void store(long tim, byte[] s, Long now){

        int addr=(int)((s[0]&0xff)>>>4);
        if (addr<0 || addr>15) return;
        int cls=(int)(0xf & s[0]);
        if (cls<=0 || cls>13) return;
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
        if (allSensor[addr]!=null){
            if (logTime==null) logTime=now;
            if (lastSent==null) lastSent=now;
            RecordReading fullSensor=new RecordReading();
            fullSensor.logTime=tim-logTime;
            fullSensor.record=allSensor;
            disp.dispRecord(fullSensor,now-5000);
            Arrays.fill(allSensor,null);
            lastSent=now;
        }
        allSensor[addr]=sensor;
        return;
    }

    private static class MyHandler extends Handler {

        private final WeakReference<comSoufl> mActivity;
        private Long startWhen = null;

        public MyHandler(comSoufl activity) {
            mActivity = new WeakReference<comSoufl>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            Message upMsg;
            Long now = System.currentTimeMillis();
            if (msg.what == UsbService.MESSAGE_FROM_SERIAL_PORT) {
                TimBytes timBytes = (TimBytes) msg.obj;
                byte[] arg0 = timBytes.getData();
                int l = timBytes.getData().length;
                ByteBuffer buf = unesc(arg0);
                buf.flip();
                buf.rewind();
                if (l > 19 && arg0[0] == 0x02 && arg0[l - 1] == 0x03 && checks(buf)) {
                    buf.rewind();
                    if (arg0[7] == 6) {
                        buf.position(8);
                        Long when = timBytes.getWhen();
                        byte[] sens1 = new byte[3];
                        buf.get(sens1, 0, 3);
                        store(when, sens1,now);
                        byte[] sens2 = new byte[3];
                        buf.get(sens2, 0, 3);
                        store(when, sens2,now);
                    } else {
                        if (comSoufl.lastSent != null && now - comSoufl.lastSent > 5000) {
                            RecordReading fullSensor = new RecordReading();
                            fullSensor.logTime = now - comSoufl.logTime;
                            comSoufl.disp.dispRecord(fullSensor, now - 5000);
                            comSoufl.lastSent = now;
                        }
                    }
                }
            } else if (msg.what == MSG_TMOUT) {
                if (comSoufl.lastSent != null && now - comSoufl.lastSent > 3000) {
                    RecordReading fullSensor = new RecordReading();
                    fullSensor.logTime = now - comSoufl.logTime;
                    comSoufl.disp.dispRecord(fullSensor, now - 3000);
                    comSoufl.lastSent = now;
                    topCom.obtainMessage(1, (Object) false).sendToTarget();
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
