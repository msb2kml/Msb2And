package org.js.Msb2And;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    Context context;
    Common com=new Common();
    WeakReference<MainActivity> mAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();
        mAct=new WeakReference<MainActivity>(this);
        com.fetchPref(context);
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
        final Button bStop=(Button) findViewById(R.id.buttonStop);
        com.getMSBdirs();
        com.named=com.isAddr;
        bStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.bye();
            }
        });
        bSouf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Monitor.class);
                intent.putExtra("fromRx",false);
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
                Intent intent=new Intent(MainActivity.this,Monitor.class);
                intent.putExtra("fromRx",true);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (resultCode){
            case 1:
                com.plane=data.getStringExtra("plane");
                com.comment=data.getStringExtra("comment");
                com.putPref(context);
                break;
        }
        com.bye();
        super.onActivityResult(requestCode,resultCode,data);
    }
}
