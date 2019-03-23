package org.js.Msb2And;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public class comSim {

    private Handler mHandler=new Handler();
    private Runnable timerTask=new Runnable() {
        @Override
        public void run() {
            exploit();
            mHandler.postDelayed(timerTask,500L);
        }
    };
    private static SensorReading[] allSensor;
    private static boolean initialized;
    FileInputStream input;
    InputStreamReader reader;
    BufferedReader buf=null;

    private static Handler topCom;
    public static DispRecord disp;
    private Context context;
    Pattern patSemi=Pattern.compile(";");

    private static int[] addrClass={1, 9, 1, 2,11, 5, 8, 3,
                                    8, 8, 7, 8, 0, 6, 0, 0};
    private static String testPath;
    private Long nxtTime=0L;
    private boolean running=true;
    private Long now;
    private Long startOpen=null;
    private Timer myTimer;

    public void Sim(Context context, Handler topCom, DispRecord disp, String testPath){
        this.context=context;
        this.topCom=topCom;
        this.disp=disp;
        this.testPath=testPath;
        allSensor=new SensorReading[16];
        mHandler.post(timerTask);
        return;
    }

    public void close(){
        mHandler.removeCallbacks(timerTask);
        if (buf!=null) {
            try {
                buf.close();
                buf=null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public void pause(){
        mHandler.removeCallbacks(timerTask);
    }

    public void restart(){
        mHandler.post(timerTask);
    }

    public void skip(Long ms){
        nxtTime+=ms;
        mHandler.post(timerTask);
    }


    RecordReading nwRec(){
        RecordReading fullSensor=new RecordReading();
        SensorReading place=new SensorReading();
        place.v_class=0;
        place.alarm=false;
        place.valid=false;
        place.value=0;
        String line=rLine();
        if (line==null) return null;
        String fields[]=patSemi.split(line.replace(",","."));
        fullSensor.logTime=(long)(Float.parseFloat(fields[1])*1000.0);
        int nf=fields.length;
        for (int i=2;i<nf-1;i++){
            if (!fields[i].isEmpty() && fields[i].matches(".*[0-9]+")){
                float v=Float.parseFloat(fields[i]);
                fullSensor.record[i-2]=mkSR(i-2,v);
            }
        }
        int j=fullSensor.record.length;
        return  fullSensor;
    }

    private String rLine(){
        try {
            if (buf==null){
                input=new FileInputStream(testPath);
                reader=new InputStreamReader(input);
                buf=new BufferedReader(reader);
                Message msg=topCom.obtainMessage(1,true);
                msg.sendToTarget();
            }
            String line="";
            while (line!=null){
                line=buf.readLine();
                if (line==null) continue;
                if (line.startsWith("$SETUP2")) setup2(line);
                if (line.startsWith("$D;")){
                    return line;
                } else continue;
            }
            buf.close();
            buf=null;
            return null;
        }
        catch (Exception e){
            return null;
        }
    }

    private void setup2(String line){
        if (line==null || line.isEmpty()) return;
        String fields[]=patSemi.split(line.replace(",","."));
        if (fields.length<4) return;
        Character deg='\ufffd';
        for (int i=2;i<18;i++){
            if (i>fields.length-1){
                addrClass[i-2]=0;
                continue;
            }
            if (fields[i]==null || fields[i].isEmpty()) continue;
            if (fields[i].contains("V")) addrClass[i-2]=1;
            else if (fields[i].contains("m/s")) addrClass[i-2]=3;
            else if (fields[i].contains("km/h")) addrClass[i-2]=4;
            else if (fields[i].contains("t/min")) addrClass[i-2]=5;
            else if (fields[i].contains(deg.toString()+"C")) addrClass[i-2]=6;
            else if (fields[i].contains(deg.toString())) addrClass[i-2]=7;
            else if (fields[i].contains("% LQI")) addrClass[i-2]=10;
            else if (fields[i].contains("%")) addrClass[i-2]=9;
            else if (fields[i].contains("mAh")) addrClass[i-2]=11;
            else if (fields[i].contains("A")) addrClass[i-2]=2;
            else if (fields[i].contains("ml")) addrClass[i-2]=12;
            else if (fields[i].contains("km")) addrClass[i-2]=13;
            else if (fields[i].contains("m")) addrClass[i-2]=8;
            else addrClass[i-2]=0;
        }
    }

    private SensorReading mkSR(int addr, float fval){
        SensorReading result=new SensorReading();
        result.addr=addr;
        result.v_class=addrClass[addr];
        result.alarm=false;
        result.inTime=0L;
        result.valid=true;
        switch (result.v_class){
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
            case 13:
                result.value=(short) (fval*10.0);
                break;
            case 5:
                result.value=(short) (fval/100.0);
                break;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                result.value=(short) fval;
                break;
            default:
                result.value=0;
                result.valid=false;
                break;
        }
        return result;
    }

    public void exploit(){
        RecordReading rec=null;
        now=System.currentTimeMillis();
        Long diff=-1L;
        while (diff<0L){
            rec=nwRec();
            if (rec==null) break;
            diff=rec.logTime-nxtTime;
        }
        if (rec==null){
            topCom.obtainMessage(1, (Object) false).sendToTarget();
            close();
        }
        else {
            nxtTime = rec.logTime + 500L;
            disp.dispRecord(rec, 0L);
        }
    }

}
