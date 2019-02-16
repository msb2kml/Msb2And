package org.js.Msb2And;

import android.content.Context;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by js on 4/5/18.
 */

public class DispRecord {

    private ListView list;
    private Context contextMoni;
    public Button onAir;
    private int from;
    private int Rx;
    private int Soufl;
    private int Simu;
    public TextView secText;
    private WeakReference<Monitor> mActivity;
    private MyListAdapter mAdapter;
    private String pathMSBfile=null;
    private String pathMeta;
    private FileWriter outMSBfile=null;
    private int nMeas=0;
    private RecordReading lastSensor=new RecordReading();
    private Calendar startTime=null;
    private String plane="";
    private String comment="";
    private boolean named;
    private boolean[] minus=new boolean[16];

    public void setVar(Context context, WeakReference<Monitor> mActivity,
                       int from, String pathMSBfile, String pathMeta,
                       String plane, String comment){
        this.contextMoni=context;
        Rx=context.getResources().getInteger(R.integer.Rx);
        Soufl=context.getResources().getInteger(R.integer.Soufl);
        Simu=context.getResources().getInteger(R.integer.Simu);
        this.mActivity=mActivity;
        this.from=from;
        this.pathMSBfile=pathMSBfile;
        this.pathMeta=pathMeta;
        this.plane=plane;
        this.comment=comment;
        onAir=(Button) mActivity.get().findViewById(R.id.onair);
        secText=(TextView) mActivity.get().findViewById(R.id.when);
        named=mActivity.get().named;
        for (int i=0;i<16;i++){
            if (named){
                minus[i]=mActivity.get().names[i].matches("-");
            } else minus[i]=false;
        }
        return;
    }

    public void dispRecord(RecordReading fullSensor, long old){
        SensorReading[] asmbl=new SensorReading[16];
        ArrayList<SensorReading> objects=new ArrayList<SensorReading>();
        if (startTime==null) startTime=Calendar.getInstance();
        if (list==null){
            list=(ListView) mActivity.get().findViewById(R.id.list);
            for (int i=0; i<16; i++){
                if (fullSensor.record[i]!=null && !minus[i]){
                    if (fullSensor.logTime>2) fullSensor.record[i].chkXtrm();
                    objects.add((fullSensor.record[i]));
                }
            }
            mAdapter=new MyListAdapter(contextMoni,mActivity,objects);
            list.setAdapter(mAdapter);
        } else {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                SensorReading sens = (SensorReading) mAdapter.getItem(i);
                int addr = sens.addr;
                if (sens.inTime<old) {
                    sens.valid=false;
                }
                asmbl[addr] = sens;
            }
            for (int i = 0; i < 16; i++) {
                if (fullSensor.record[i] != null) {
                    if (asmbl[i] == null) asmbl[i] = fullSensor.record[i];
                    else asmbl[i].cp(fullSensor.record[i]);
                }
                if (asmbl[i] != null && !minus[i]) {
                    if (fullSensor.logTime>2) asmbl[i].chkXtrm();
                    objects.add(asmbl[i]);
                }
            }
            mAdapter.getSensor().clear();
            mAdapter.getSensor().addAll(objects);
            mAdapter.notifyDataSetChanged();
        }
        lastSensor.cp(fullSensor);
        try {
            if (nMeas == 0 && pathMSBfile!=null) {
                outMSBfile = new FileWriter(pathMSBfile);
                String line="$SETUP1;Time;";
                String semic="";
                for (int i=0; i<16; i++){
                    if (lastSensor.record[i]!=null) {
                        line=line+semic+String.format(" A:%02d;",i);
                        semic="";
                    }
                    else semic=semic+";";
                }
                outMSBfile.write(line+"\n");
                line="$SETUP2;sec ;";
                semic="";
                for (int i=0; i<16; i++){
                    if (lastSensor.record[i]!=null){
                        line=line+semic+lastSensor.record[i].heading()+";";
                        semic="";
                    } else semic=semic+";";
                }
                outMSBfile.write(line+"\n");
            }
        }
        catch (Exception e){
            outMSBfile=null;
        }
        if (from==Rx) onAir.setText("Rx on");
        else if (from==Soufl) onAir.setText("On Air");
        secText.setText(lastSensor.prTime());
        nMeas++;
        if (outMSBfile!=null){
            try {
                String line="$D;"+lastSensor.prTimeG()+";";
                String semic="";
                for (int i=0; i<16; i++){
                    if (lastSensor.record[i]!=null) {
                        line=line+semic+lastSensor.record[i].printG()+";";
                        semic="";
                    }
                    else semic=semic+";";
                }
                outMSBfile.write(line+"*00\n");
            }
            catch (Exception e){
                outMSBfile=null;
            }
        }
        return;
    }

    public boolean close(){
        String line;
        if (outMSBfile!=null){
            try {
                outMSBfile.close();
                if (pathMeta!=null && startTime!=null && lastSensor!=null){
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mActivity.get().startDate=sdf.format(startTime.getTime());
                    mActivity.get().nMeas=nMeas;
                    mActivity.get().sDuration=lastSensor.logTime/1000;
                    return false;
                } else return true;
            } catch (IOException e) {
                return true;
            }
        } else return true;
    }

    public void delData(){
        File outData=new File(pathMSBfile);
        outData.delete();
    }

    public void recMeta(String nwPlane, String nwComment){
        String line;
        plane=nwPlane;
        comment=nwComment;
        try {
            FileWriter outMeta=new FileWriter(pathMeta);
            outMeta.write("Date: "+mActivity.get().startDate+"\n");
            outMeta.write("Plane: "+mActivity.get().plane+"\n");
            outMeta.write("Comment: "+mActivity.get().comment+"\n");
            line="Time: 0.0000;"+lastSensor.prTime()+"\n";
            outMeta.write(line);
            for (int i=0;i<16;i++){
                if (lastSensor.record[i]!=null){
                    line=String.format(" A:%02d: ",i);
                    line=line+lastSensor.record[i].print_min()+";";
                    line=line+lastSensor.record[i].print_max()+"\n";
                    outMeta.write(line);
                }
            }
            outMeta.close();
            return;
        } catch (IOException e){
            return;
        }
    }


}
