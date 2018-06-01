package org.js.Msb2And;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import java.lang.ref.WeakReference;
import java.util.Random;


public class Monitor extends AppCompatActivity {

    private Context context;
    public Button onAir;
    public TextView secText;
    private boolean Started=false;
    private boolean fromRx=false;
    private DispRecord disp;
    private Intent intent;
    private MyHandler mHandler;
    public comRx Rx;
    public comSoufl Souf;
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
        fromRx=intent.getBooleanExtra("fromRx",false);
        named=intent.getBooleanExtra("named", false);
        if (named){
            pathAddr=intent.getStringExtra("pathAddr");
        }
        pathMSBfile=intent.getStringExtra("pathMSBfile");
        pathMeta=intent.getStringExtra("pathMeta");
        plane=intent.getStringExtra("plane");
        comment=intent.getStringExtra("comment");

        final Button bStart=(Button) findViewById(R.id.start);
        secText=(TextView) findViewById(R.id.when);
        onAir=(Button) findViewById(R.id.onair);
        if(fromRx){
            secText.setText("from Rx");
        } else secText.setText("from Souffleur");
        bStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Started) termin();
                else {
                    bStart.setText("Stop");
                    Started=true;
                    disp=new DispRecord();
                    disp.setVar(Monitor.this,myAct,fromRx,pathMSBfile,pathMeta,
                            plane,comment);
//                    disp.dispRecord(getRandomReading(),System.currentTimeMillis());
                    mHandler=new MyHandler(Monitor.this);
                    if (pathMSBfile==null){
                        Toast.makeText(context,"NO recording",Toast.LENGTH_LONG).show();
                    } else {
//                        Toast.makeText(context,"Recording: "+pathMSBfile,
//                                Toast.LENGTH_LONG).show();
                    }
                    if (fromRx) {
                        Rx = new comRx();
                        Rx.Rx(context, mHandler, disp);
                    } else {
                        Souf=new comSoufl();
                        Souf.Soufl(context,mHandler,disp);
                    }
                }
            }
        });
    }
    
    public void termin(){
        if (Rx != null) Rx.close();
        if (Souf != null) Souf.close();
        if (disp==null || disp.close()) {
            setResult(0, intent);
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
            case 0:
                disp.delData();
                break;
            case 1:
                plane=data.getStringExtra("plane");
                comment=data.getStringExtra("comment");
                disp.recMeta(plane,comment);
        }
        setResult(resultCode, intent);
        intent.putExtra("plane",plane);
        intent.putExtra("comment",comment);
        finish();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                finishAndRemoveTask();
//            } else finish();
        super.onActivityResult(requestCode,resultCode,data);
    }

    public void onOff(boolean newState){
        if (newState && fromRx) onAir.setText("Rx on");
        else if (newState) onAir.setText("On Air");
        else onAir.setText("");
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
