package org.js.Msb2And;

import java.util.Locale;

/**
 * Created by js on 4/5/18.
 */

public class SensorReading {
    int addr;
    int v_class;
    boolean alarm;
    boolean valid;
    long inTime;
    short value;
    short value_min=17000;
    short value_max=-17000;

    public void chkXtrm(){
        if (valid) {
            value_max = (short)Math.max(value, value_max);
            value_min = (short)Math.min(value, value_min);
        }
    }

    public void cp(SensorReading other){
        if (other==null || !other.valid){
            valid=false;
        }else {
            addr=other.addr;
            v_class=other.v_class;
            alarm=other.alarm;
            valid=true;
            value=other.value;
            inTime=other.inTime;
        }
        return;
    }

    public String heading() {
        Character deg='\ufffd';
        switch (v_class) {
            case 0:
                return "?";
            case 1:
                return "V";
            case 2:
                return "A";
            case 3:
                return "m/s";
            case 4:
                return "km/h";
            case 5:
                return "1/min";
            case 6:
                return deg.toString()+"C";
            case 7:
                return deg.toString();
            case 8:
                return "m";
             case 9:
                return "%";
            case 10:
                return "%LQI";
            case 11:
                return "mAh";
            case 12:
                return "ml";
            case 13:
                return "km";
            case 14:
                return "g";
            default:
                return "?"+String.valueOf(v_class);
        }
    }

    public String print() {

            return printv(Locale.ENGLISH, value);
    }

    public String printG(){
        return printv(Locale.GERMAN, value);
    }

     public String print_max(){
        return printv(Locale.ENGLISH, value_max);
     }

     public String print_min(){

         return printv(Locale.ENGLISH, value_min);
     }

     private String printv(Locale X, short x){
        switch (v_class) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
            case 13:
            case 14:
                 return String.format(X, "%.1f",  (float)x / 10);
            case 5:
                 return String.valueOf(x * 100);
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
                 return String.valueOf(x);
             default:
                 return "?"+String.valueOf(x);
         }
     }

     public Float fVal(){
        Double x=Double.valueOf(value);
        switch (v_class){
            case 1:
            case 2:
            case 3:
            case 4:
            case 6:
            case 7:
            case 13:
            case 14:
                x=x/10.0;
                break;
            case 5:
                x=x*100.0;
                break;
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            default:
                break;
        }
        return x.floatValue();
     }
}
