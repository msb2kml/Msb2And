package org.js.Msb2And;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static java.util.Arrays.fill;

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
    private String plane="";
    private String comment="";
    private String startName=null;
    private boolean named;
    private boolean[] minus=new boolean[16];
    int[] recClass=new int[16];

    public void setVar(Context context, WeakReference<Monitor> mActivity,
                       int from, String pathMSBfile, String pathMeta,
                       String plane, String comment, String startName){
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
        this.startName=startName;
        onAir=(Button) mActivity.get().findViewById(R.id.onair);
        secText=(TextView) mActivity.get().findViewById(R.id.when);
        named=mActivity.get().named;
        return;
    }

    public void dispRecord(RecordReading fullSensor, long old){
        boolean changed=false;
        boolean empty=true;
        ArrayList<Object> ob=new ArrayList<>();
        ArrayList<CompReading> cr=null;
        lastSensor.logTime=fullSensor.logTime;
        for (int i=0;i<16;i++){
            if (fullSensor.record[i]!=null){
                if (lastSensor.record[i]==null){
                    changed=true;
                    lastSensor.record[i]=fullSensor.record[i];
                } else lastSensor.record[i].cp(fullSensor.record[i]);
            }
            if (lastSensor.record[i]!=null){
                empty=false;
            }
        }
        if (empty) return;
        if (changed) mActivity.get().reCmpl(lastSensor);
        if (mActivity.get().startTime==null) mActivity.get().startTime=Calendar.getInstance();
        for (int i=0;i<16;i++){
            if (lastSensor.record[i]!=null){
                if (lastSensor.logTime>2) lastSensor.record[i].chkXtrm();
                if (lastSensor.record[i].inTime<old) lastSensor.record[i].valid=false;
                if (!mActivity.get().named || !mActivity.get().names[i].matches("-"))
                    ob.add(lastSensor.record[i]);
            }
        }
        if (named){
            mActivity.get().stDirect(lastSensor);
            cr=mActivity.get().stCalc();
            if (cr!=null && !cr.isEmpty()) ob.addAll(cr);
        }
        if (list==null){
            list=(ListView) mActivity.get().findViewById(R.id.list);
            mAdapter=new MyListAdapter(contextMoni,mActivity,ob);
            list.setAdapter(mAdapter);
            if (mActivity.get().intentMap!=null){
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        mActivity.get().runMap(position);
                    }
                });
            }
        } else {
            mAdapter.getSensor().clear();
            mAdapter.getSensor().addAll(ob);
            mAdapter.notifyDataSetChanged();
        }
        if (mActivity.get().runningMap) setBubble(mActivity.get().objPosMap);
        try {
            if (pathMSBfile!=null){
                if (outMSBfile==null) {
                    outMSBfile = new FileWriter(pathMSBfile);
                    fill(recClass, 0);
                }
                boolean setup=false;
                String line="$D;"+lastSensor.prTimeG()+";";
                String semic="";
                for (int i=0; i<16; i++){
                    if (lastSensor.record[i]!=null) {
                        if (lastSensor.record[i].v_class != recClass[i]){
                            recClass[i]=lastSensor.record[i].v_class;
                            setup=true;
                        }
                        line=line+semic+lastSensor.record[i].printG()+";";
                        semic="";
                    }
                    else semic=semic+";";
                }
                if (setup) {
                    String lineSetup = "$SETUP1;Time;";
                    semic = "";
                    for (int i = 0; i < 16; i++) {
                        if (recClass[i]!=0) {
                            lineSetup = lineSetup + semic +
                                    String.format(Locale.ENGLISH, " A:%02d;", i);
                            semic = "";
                        } else semic = semic + ";";
                    }
                    outMSBfile.write(lineSetup + "\n");
                    lineSetup = "$SETUP2;sec ;";
                    semic = "";
                    SensorReading s=new SensorReading();
                    for (int i = 0; i < 16; i++) {
                        if (recClass[i]!=0) {
                            s.v_class=recClass[i];
                            lineSetup = lineSetup + semic + s.heading() + ";";
                            semic = "";
                        } else semic = semic + ";";
                    }
                    outMSBfile.write(lineSetup + "\n");
                }
                outMSBfile.write(line+"*00\n");
            }
        }
        catch (Exception e){
            outMSBfile=null;
        }
        if (from==Rx) onAir.setText("Rx on");
        else if (from==Soufl) onAir.setText("On Air");
        secText.setText(lastSensor.prTime());
        nMeas++;
        return;
    }

    public boolean close(){
        String line;
        if (outMSBfile!=null){
            try {
                outMSBfile.close();
                if (pathMeta!=null && mActivity.get().startTime!=null && lastSensor!=null){
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    mActivity.get().startDate=sdf.format(mActivity.get().startTime.getTime());
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
            if (startName!=null) outMeta.write("StartName: "+startName+"\n");
            line="Time: 0.0000;"+lastSensor.prTime()+"\n";
            outMeta.write(line);
            for (int i=0;i<16;i++){
                if (lastSensor.record[i]!=null){
                    line=String.format(Locale.ENGLISH," A:%02d: ",i);
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

    public void setBubble( Integer position){
        String nam="";
        String measure="";
        Object o=mAdapter.getItem(position);
        if (o==null) return;
        if (o.getClass().getSimpleName().matches("SensorReading")){
            SensorReading obj=(SensorReading) o;
            if (named && mActivity.get().names[obj.addr] !=null){
                nam=String.valueOf(obj.addr)+"/"+mActivity.get().names[obj.addr];
            } else nam=String.valueOf(obj.addr);
            if (obj.valid) measure=obj.print();
            else measure="("+obj.print()+")";
        } else if (o.getClass().getSimpleName().matches("CompReading")){
            CompReading c=(CompReading) o;
            nam=c.heading;
            measure=c.print();
        }
        mActivity.get().bubbleMap=nam+": "+measure;
    }


}
