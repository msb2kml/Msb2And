package org.js.Msb2And;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    Context context;
    Common com=new Common();
    WeakReference<MainActivity> mAct;
    private static String testPath;
    String Directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();
        mAct=new WeakReference<MainActivity>(this);
        com.fetchPref(context);
        Directory=com.Directory;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode==com.REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                com.checkMSBlog();
            } else {
                com.writeSD=false;
                com.checkMSBlog();
            }
        }
    }

        public boolean defAddr(){
        try {
            InputStream model=getResources().openRawResource(R.raw.addrsens);
            BufferedReader Model=new BufferedReader(
                    (new InputStreamReader(model)));
            FileWriter ffad=new FileWriter(com.pathAddr);
            String line="";
            while (line!=null){
                line=Model.readLine();
                if (line!=null) ffad.write(line+"\n");
            }
            ffad.close();
            Model.close();
            return (true);
        } catch (Exception e){
            return (false);
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        com.checkStorage(mAct,context);
    }

    public void start1(){
        final Button bSouf=(Button) findViewById(R.id.buttonSouf);
        final Button bRx=(Button) findViewById(R.id.buttonRx);
        final Button bSim=(Button) findViewById(R.id.buttonSim);
        final Button bStop=(Button) findViewById(R.id.buttonStop);
        final CheckBox cNamed=(CheckBox) findViewById(R.id.checkNamed);
        com.getMSBdirs();
        com.named=com.isAddr;
        cNamed.setActivated(com.named);
        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.bye();
            }
        });
        bSouf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (com.named) com.named=cNamed.isChecked();
                Intent intent=new Intent(MainActivity.this,Monitor.class);
                intent.putExtra("from",getResources().getInteger(R.integer.Soufl));
                intent.putExtra("named",com.named);
                intent.putExtra("pathAddr",com.pathAddr);
                intent.putExtra("pathMSBfile",com.sltMSBf);
                intent.putExtra("pathMeta",com.pathMeta);
                intent.putExtra("plane",com.plane);
                intent.putExtra("comment",com.comment);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent,1);
            }
        });
        bRx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (com.named) com.named=cNamed.isChecked();
                Intent intent=new Intent(MainActivity.this,Monitor.class);
                intent.putExtra("from",getResources().getInteger(R.integer.Rx));
                intent.putExtra("named",com.named);
                intent.putExtra("pathAddr",com.pathAddr);
                intent.putExtra("pathMSBfile",com.sltMSBf);
                intent.putExtra("pathMeta",com.pathMeta);
                intent.putExtra("plane",com.plane);
                intent.putExtra("comment",com.comment);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent,1);
            }
        });
        bSim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (com.named) com.named=cNamed.isChecked();
                Intent intent=new Intent(MainActivity.this,Selector.class);
                intent.putExtra("CurrentDir",Directory);
                intent.putExtra("WithDir",false);
                intent.putExtra("Mask","MSB_\\d{4}+\\.csv");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent,2);
            }
        });
    }

    void launchSim(){
        Intent intent=new Intent(MainActivity.this,Monitor.class);
        intent.putExtra("from",getResources().getInteger(R.integer.Simu));
        intent.putExtra("named",com.named);
        intent.putExtra("pathAddr",com.pathAddr);
        intent.putExtra("pathMSBfile",com.sltMSBf);
        intent.putExtra("pathMeta",com.pathMeta);
        intent.putExtra("plane",com.plane);
        intent.putExtra("comment",com.comment);
        intent.putExtra("testPath",testPath);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode==RESULT_OK) {
            switch (requestCode) {
                case 1:
                    com.plane = data.getStringExtra("plane");
                    com.comment = data.getStringExtra("comment");
                    com.putPref(context);
                    com.bye();
                    break;
                case 2:
                    testPath=data.getStringExtra("Path");
                    if (testPath==null || testPath.isEmpty()) com.bye();
                    else {
                        File f=new File(testPath);
                        Directory=f.getParent();
                        com.Directory=Directory;
                        launchSim();
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode,resultCode,data);
    }
}
