package org.js.Msb2And;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
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
    public String startDate="";
    public int nMeas=0;
    public long sDuration=0;
    public String plane;
    public String comment;
    public String[] names=new String[16];

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
        if (named){
            pathAddr=intent.getStringExtra("pathAddr");
            File addr=new File(pathAddr);
            try {
                BufferedReader f=new BufferedReader(new FileReader(addr));
                String line="";
                while (line!=null){
                    line=f.readLine();
                    if (line==null) continue;
                    if (line.startsWith("*")) continue;
                    String[] fields=line.split(";");
                    if (fields==null || fields.length<2) continue;
                    if (fields[0].matches(" A:[0-9]{2}")){
                        String sub=fields[0].substring(3);
                        int ind=Integer.valueOf(sub);
                        if (ind>=0 && ind<16) names[ind]=fields[1].trim();
                    }
                }
                f.close();
            } catch (Exception e){
                named=false;
            }
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
                            plane,comment);
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
            startActivityForResult(lastIntent,1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (resultCode){
            case RESULT_CANCELED:
                disp.delData();
                break;
            case RESULT_OK:
                plane=data.getStringExtra("plane");
                comment=data.getStringExtra("comment");
                disp.recMeta(plane,comment);
        }
        setResult(RESULT_OK, intent);
        intent.putExtra("plane",plane);
        intent.putExtra("comment",comment);
        finish();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                finishAndRemoveTask();
//            } else finish();
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

}
