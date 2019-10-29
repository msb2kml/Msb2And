package org.js.Msb2And;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


public class Monitor extends AppCompatActivity {

    private Context context;
    public Button onAir;
    public TextView secText;
    private boolean Started=false;
    private boolean fromRx=false;
    private int from;
    private DispRecord disp;
    private Intent intent;
    private MyHandler mHandler;
    public comRx Rx;
    public comSim Sim;
    public comSoufl Souf;
    public String testPath=null;
    public boolean paused;
    public boolean named=false;
    public String pathAddr;
    public String pathMSBfile=null;
    public String pathMeta=null;
    public String pathStartGPS;
    public String startName=null;
    public String startDate="";
    public Calendar startTime=null;
    public int nMeas=0;
    public long sDuration=0;
    public String plane;
    public String comment;
    public String[] names=new String[16];
    boolean useDist=false;
    Location startLoc;
    Location prevLoca=null;
    Haversine haver=null;
    ArrayList<Float> totDist=new ArrayList<Float>();
    ArrayList <Float> fData=new ArrayList<Float>();
    Integer varTime=null;
    ArrayList <Float> compData=new ArrayList<Float>();
    ArrayList<tool> compTool=new ArrayList<>();
    ArrayList<String> compHead=new ArrayList<>();
    ArrayList<CompReading> listComp=new ArrayList<>();
    Intent intentMap=null;
    Boolean runningMap=false;
    Double zoom=17.0;
    Integer reqCdStore=1;
    Integer objPosMap=null;
    String bubbleMap=null;
    Var colorVar=null;
    Integer[] lineColor={ Color.rgb(0x00,0x00,0xFF),
                          Color.rgb(0x00,0x63,0xF3),
                          Color.rgb(0x00,0x92,0xDE),
                          Color.rgb(0x00,0xB7,0xC2),
                          Color.rgb(0x00,0xD6,0xA0),
                          Color.rgb(0x54,0xDD,0x74),
                          Color.rgb(0x85,0xE0,0x46),
                          Color.rgb(0xAD,0xE1,0x00),
                          Color.rgb(0xD9,0xC6,0x00),
                          Color.rgb(0xFF,0xA5,0x00),
                          Color.rgb(0xFF,0x78,0x00),
                          Color.rgb(0xFF,0x00,0x00)};
    int nColor=lineColor.length;
    SensorReading[] seenAddr=new SensorReading[16];
    tb t=new tb();

    public static WeakReference<Monitor> myAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myAct=new WeakReference<Monitor>(Monitor.this);
        setContentView(R.layout.activity_monitor);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        context=getApplicationContext();
        intent=getIntent();
        from=intent.getIntExtra("from",0);
        if (from==0) termin();
        fromRx=intent.getBooleanExtra("fromRx",false);
        named=intent.getBooleanExtra("named", false);
        pathMSBfile=intent.getStringExtra("pathMSBfile");
        pathMeta=intent.getStringExtra("pathMeta");
        plane=intent.getStringExtra("plane");
        comment=intent.getStringExtra("comment");
        testPath=intent.getStringExtra("testPath");
        startName=intent.getStringExtra("startName");
        startLoc=intent.getParcelableExtra("Location");
        if (named){
            for (int i=0;i<16;i++){
                fData.add(0.0f);
                names[i]=String.format(Locale.ENGLISH," A:%02d",i);
            }
            pathAddr=intent.getStringExtra("pathAddr");
            pathStartGPS=intent.getStringExtra("pathStartGPS");
            if (!readAddr(pathAddr)) named=false;
            varTime=16;
            fData.add(0.0f);
        }
        final Button bStart=(Button) findViewById(R.id.start);
        secText=(TextView) findViewById(R.id.when);
        onAir=(Button) findViewById(R.id.onair);
        if (from==getResources().getInteger(R.integer.Rx)){
            secText.setText("from Rx");
        } else if (from==getResources().getInteger(R.integer.Soufl)){
            secText.setText("from Souffleur");
        } else if (from==getResources().getInteger(R.integer.Simu)){
            if (testPath==null) termin();
            File f=new File(testPath);
            String nameSim=f.getName();
            secText.setText("Simulating "+nameSim);
            onAir.setText("Pause");
            paused=false;
            onAir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Sim!=null){
                        if (paused){
                            onAir.setText("Pause");
                            paused=false;
                            skipChoice();
                        } else {
                            onAir.setText("Play");
                            Sim.pause();
                            paused=true;
                        }
                    }
                }
            });

        } else termin();
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Started) termin();
                else {
                    bStart.setText("Stop");
                    Started=true;
                    disp=new DispRecord();
                    disp.setVar(Monitor.this,myAct,from,pathMSBfile,pathMeta,
                            plane,comment,startName);
//                    disp.dispRecord(getRandomReading(),System.currentTimeMillis());
                    mHandler=new MyHandler(Monitor.this);
                    if (pathMSBfile==null){
                        Toast.makeText(context,"NO recording",Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(context,"Recording: "+pathMSBfile,
//                                Toast.LENGTH_LONG).show();
                    }
                    if (from==getResources().getInteger(R.integer.Rx)){
                        Rx=new comRx();
                        Rx.Rx(context,mHandler,disp);
                    } else if (from==getResources().getInteger(R.integer.Soufl)){
                        Souf=new comSoufl();
                        Souf.Soufl(context,mHandler,disp);
                    } else if (from==getResources().getInteger(R.integer.Simu)) {
                        Sim = new comSim();
                        Sim.Sim(context,mHandler,disp,testPath);
                    }
                }
            }
        });
    }


/*      For testing
    FileWriter outGpx=null;
    gpxGen g=new gpxGen();
*/

    Exception addPoint(Location loc){
        Float ft=fData.get(varTime)*1000.0f;
        Long cT=ft.longValue();
        cT+=startTime.getTimeInMillis();
        loc.setTime(cT);
        Double Lat=loc.getLatitude();
        Double Lon=loc.getLongitude();
        Double Alt=loc.getAltitude();
        if (prevLoca!=null && (Lat.compareTo(prevLoca.getLatitude())==0) &&
                (Lon.compareTo(prevLoca.getLongitude())==0) &&
                (Alt.compareTo(prevLoca.getAltitude())==0)) return null;
        if (useDist){
            if (prevLoca!=null){
                Double Dist=haver.lHaversine(prevLoca,loc)+totDist.get(0);
                totDist.set(0,Dist.floatValue());
            }
        }
        if (prevLoca==null) prevLoca=new Location("");
        prevLoca.set(loc);
/*   For testing
        if (pathMSBfile!=null) {
            try {
                if (outGpx == null) {
                    String exPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String pathMSBlog = exPath + "/" + context.getString(R.string.MSBlog);
                    String pathGpx = pathMSBlog + "/" + "xxxx.gpx";
                    outGpx=new FileWriter(pathGpx);
                    g.beginGpx(outGpx,"xxxx");
                }
                g.pointGpx(loc);
            } catch (Exception e) {
                return e;
            }
        }
*/
        if (runningMap) {
            Intent nt = new Intent();
            nt.setAction("org.js.LOC");
            nt.putExtra("LOC", prevLoca);
            if (bubbleMap!=null) nt.putExtra("BUBBLE",bubbleMap);
            if (colorVar!=null){
                ArrayList<Float> zz=(ArrayList<Float>) colorVar.thing;
                Float percent=zz.get(colorVar.index)/100f;
                int v=Math.round(percent*nColor);
                v=Math.max(1,Math.min(nColor,v))-1;
                int color=lineColor[v];
                nt.putExtra("COLOR",color);
            }
            sendBroadcast(nt);
        }
        return null;
    }

    public void skipChoice(){
        String theList[]={"Skip 20 s ","Skip 120 s","Skip 10 min"};
        final Long toSkip[]={20000L,120000L,600000L};
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        build.setTitle("Skip some time?")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        Sim.restart();
                    }
                })
                .setItems(theList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Sim.skip(toSkip[which]);
                    }
                })
                .setNeutralButton("No skip", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Sim.restart();
                    }
                });
        build.show();
    }
    
    public void termin(){
/*   For testing
        if (outGpx!=null){
            try {
                g.tailGpx();
                outGpx.close();
            } catch (Exception e){

            }
        }
*/
        if (Rx != null) Rx.close();
        if (Souf != null) Souf.close();
        if (Sim != null) Sim.close();
        if (disp==null || disp.close()) {
            setResult(RESULT_CANCELED, intent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else finish();
        } else {
            Intent lastIntent=new Intent(this,Storage.class);
            lastIntent.putExtra("startTime",startDate);
            lastIntent.putExtra("Duration",sDuration);
            lastIntent.putExtra("nbData",nMeas);
            lastIntent.putExtra("pathData",pathMSBfile);
            lastIntent.putExtra("pathMeta",pathMeta);
            lastIntent.putExtra("plane",plane);
            lastIntent.putExtra("comment",comment);
            if (startName!=null) lastIntent.putExtra("startName",startName);
            startActivityForResult(lastIntent,reqCdStore);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode==reqCdStore) {
            switch (resultCode) {
                case RESULT_CANCELED:
                    disp.delData();
                    break;
                case RESULT_OK:
                    plane = data.getStringExtra("plane");
                    comment = data.getStringExtra("comment");
                    disp.recMeta(plane, comment);
            }
            intent.putExtra("plane", plane);
            intent.putExtra("comment", comment);
            if (startName != null) intent.putExtra("startName", startName);
            setResult(RESULT_OK, intent);
            finish();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                finishAndRemoveTask();
//            } else finish();
        }
        super.onActivityResult(requestCode,resultCode,data);
    }

    public void onOff(boolean newState){
        if (newState && from==getResources().getInteger(R.integer.Rx)) onAir.setText("Rx on");
        else if (newState && from==getResources().getInteger(R.integer.Soufl))
            onAir.setText("On Air");
        else if (!newState && from==getResources().getInteger(R.integer.Simu)){
            onAir.setEnabled(false);
            onAir.setText("The End");
        }
        return;
    }

    private static class MyHandler extends Handler {
        public final WeakReference<Monitor> mActivity;

        public MyHandler(Monitor activity){
            mActivity=new WeakReference<Monitor>(activity);
        }

        @Override
        public void handleMessage(Message msg){
            int code=msg.what;
            switch (code){
                case 0:
                    mActivity.get().termin();
                    break;
                case 1:
                    boolean newState=(boolean)msg.obj;
                    mActivity.get().onOff(newState);
                    break;
                default:
                    break;
            }
        }
    }
/*
    private RecordReading getRandomReading(){
        RecordReading rec=new RecordReading();
        Random random=new Random();
        for (int i=0; i<16 ; i++){
            SensorReading sens=new SensorReading();
            sens.addr=i;
            sens.value= (short) (random.nextInt(16000)-8000);
            sens.v_class=random.nextInt(14);
            sens.alarm=random.nextBoolean();
            sens.valid=random.nextBoolean();
            sens.chkXtrm();
            rec.record[i]=sens;
        }
        Integer t=random.nextInt(1000);
        rec.logTime=t.longValue();
        return rec;
    }
*/
    private class triplet{
        String addr=null;
        String subst=null;
        Character var=null;
    }

    ArrayList<triplet> translate=new ArrayList<>();

// AddrSens to triplet
    boolean readAddr(String pathAddr){
        if (pathAddr==null) return false;
        File addr=new File(pathAddr);
        if (! addr.exists()) return false;
        int n=0;
        try {
            BufferedReader f = new BufferedReader(new FileReader(addr));
            String line="";
            while (line!=null){
                line=f.readLine();
                if (line==null) continue;
                if (line.startsWith("*")) continue;
                String[] fields = line.split(";");
                if (fields==null || fields.length<2) continue;
                triplet t=new triplet();
                t.addr=fields[0];
                t.subst=fields[1];
                if (fields.length>2){
                    String sign=fields[2].trim();
                    if (!sign.isEmpty()) t.var=sign.charAt(0);
                }
                translate.add(t);
            }
            f.close();
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public class Var{
        Character id=null;
        Object thing=null;
        Integer index=null;
    }

    ArrayList<Var> variables=new ArrayList<>();

    boolean setVar(Character c, Object O, Integer ind){
        if (variables!=null) {
            for (Var v : variables) if (v.id == c) return false;
        }
        Var v=new Var();
        v.id=c;
        v.thing=O;
        v.index=ind;
        variables.add(v);
        return true;
    }

    public Var getVar(Character c){
        if (variables==null) return null;
        for (Var v : variables) {
            if (v.id.equals(c)) return v;
        }
        if (c.equals('#') && startLoc!=null){
            useDist=true;
            totDist.add(0f);
            if (!setVar(c,(Object)totDist,0)) return null;
            return  getVar(c);
        }
        return null;
    }

    public boolean delVar(char c){
        if (variables==null) return false;
        for (Var v : variables) if (v.id.equals(c)){
            variables.remove(v);
            return true;
        }
        return false;
    }

    public void reCmpl(RecordReading fullSensor){
        if (named){
            fData.clear();
            variables.clear();
            compHead.clear();
            compTool.clear();
            totDist.clear();
            listComp.clear();
            if (haver==null) haver=new Haversine();
// var for direct data
            for (int i=0;i<16;i++){
                fData.add(0.0f);
                names[i]=String.format(Locale.ENGLISH," A:%02d",i);
            }
            varTime=16;
            fData.add(0.0f);
            for (int i=0; i<16; i++){
                if (fullSensor.record[i]==null) continue;
                for (triplet tr : translate){
                    if (names[i].matches(tr.addr)){
                        if (tr.subst!=null) names[i]=tr.subst;
                        if (tr.var!=null) setVar(tr.var,(Object)fData,i);
                        break;
                    }
                }
            }
// var for functions
            for (triplet tr : translate) {
                if (tr.addr.startsWith("=")) {
                    fData.add(0.0f);
                    if (tr.var != null) setVar(tr.var, (Object) fData, fData.size() - 1);
                } else if (tr.addr.startsWith("Time") && tr.var != null) {
                    setVar(tr.var, (Object) fData, varTime);
                }
            }
// identify functions
            for (triplet tr : translate) {
                if (tr.addr.startsWith("=")) {
                    tool x = t.toolBox(this, tr.addr, tr.var);
                    compTool.add(x);
                    compHead.add(tr.subst);
                }
            }
// check for availibilty of var
            boolean ok=(compTool.size()<1);
            while (!ok){
                ok=true;
                for (int it=0;it<compTool.size();it++){
                    if (compTool.get(it)!=null){
                        if (!compTool.get(it).checkMore()){
                            compTool.set(it,null);
                            ok=false;
                        }
                    }
                }
            }
// compose list
            for (int i=0;i<compTool.size();i++){
                if (compTool.get(i)!=null && !compHead.get(i).matches("-")){
                    CompReading cr=new CompReading();
                    cr.heading=compHead.get(i);
                    cr.func=compTool.get(i).getClass().getSimpleName();
                    listComp.add(cr);
                }
            }
            checkMap();
            colorVar=getVar('%');
        }
        return;
    }

    public void stDirect(RecordReading fullSensor){
        for (int i = 0; i < 16; i++) {
                if (fullSensor.record[i] != null)
                    fData.set(i, fullSensor.record[i].fVal());
            }
        if (varTime!=null) fData.set(varTime,fullSensor.fTime());
    }

    public ArrayList<CompReading> stCalc(){
        int ind=varTime+1;
        int iList=0;
        for (int i=0;i<compTool.size();i++){
            tool t=compTool.get(i);
            if (t!=null){
                fData.set(ind+i,t.compute());
                if (!compHead.get(i).matches("-")) {
                    listComp.get(iList).setValue(fData.get(ind+i));
                    iList++;
                }
            }
        }
        return listComp;
    }

    public void checkMap(){
        if (startName==null) return;
        PackageManager Pm=getPackageManager();
        List<PackageInfo> allPack=Pm.getInstalledPackages(0);
        for (PackageInfo AI :allPack) {
            String zz=AI.packageName;
            if (zz.matches("org.js.Msb2Map")){
                intentMap=Pm.getLaunchIntentForPackage(zz);
                break;
            }
        }
        return;
    }

    public void runMap(Integer position){
        if (intentMap==null) return;
        objPosMap=position;
        Intent nt=(Intent) intentMap.clone();
        nt.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        nt.putExtra("CALLER","Msb2And");
        nt.putExtra("CENTER",startLoc);
        if (zoom!=null) nt.putExtra("ZOOM",zoom);
        zoom=null;
        runningMap=true;
        startActivity(nt);
    }

    public Location nameToLoc(String pylone){
        Location loc=null;
        if (pathStartGPS==null) return null;
        StartGPS sGPS=new StartGPS(pathStartGPS);
        Map<String,Location> startPoints=sGPS.readSG();
        if (startPoints.isEmpty()) return null;
        if (!startPoints.containsKey(pylone)) return null;
        loc=startPoints.get(pylone);
        return loc;
    }



}
