package org.js.Msb2And;

import java.util.Locale;

public class CompReading {
    Float value;
    Float minimum=null;
    Float maximum=null;
    String heading;
    String func;

    void setValue(Float val){
        value=val;
        if (minimum==null) minimum=value;
        else minimum=(Float)Math.min(minimum,value);
        if (maximum==null) maximum=value;
        else maximum=(Float)Math.max(maximum,value);
    }

    String print(){
        return String.format(Locale.ENGLISH,"%g",value);
    }

    String printMax(){
        if (maximum==null) return "";
        else return String.format(Locale.ENGLISH,"%g",maximum);
    }

    String printMin(){
        if (minimum==null) return "";
        else return String.format(Locale.ENGLISH,"%g",minimum);
    }
}
