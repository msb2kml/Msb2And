package org.js.Msb2And;

import java.util.Locale;

/**
 * Created by js on 4/5/18.
 */

public class RecordReading {

    Long logTime;
    SensorReading[] record=new SensorReading[16];

    public String prTime(){
        Float ft= Float.valueOf(logTime)/1000;
        return String.format(Locale.ENGLISH,"%.4f",ft);
    }

    public String prTimeG(){
        Float ft=Float.valueOf(logTime)/1000;
        return String.format(Locale.GERMAN,"%06.4f",ft);
    }

    public Float fTime(){
        Float f=Float.valueOf(logTime)/1000.0f;
        return f;
    }

    public void cp(RecordReading other){
        if (other==null) return;
        logTime=other.logTime;
        for (int i=0;i<16;i++){
            if (other.record[i]!=null) {
                if (record[i]==null) record[i]=new SensorReading();
                record[i].cp(other.record[i]);
                record[i].chkXtrm();
            }
        }
    }
}
