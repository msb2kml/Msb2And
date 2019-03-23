package org.js.Msb2And;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CopyFix extends AppCompatActivity {

    private TextView vTitle;
    private TextView vComment;
    private TextView vFrom;
    private TextView vLat;
    private TextView vLon;
    private TextView vAlt;
    private EditText vName;
    private Button bCancel;
    private Button bChange;
    private Button bEnter;
    private Context context;
    private Intent intent;
    private Location[] loca=new Location[2];
    int which;
    private String name;
    private String MSBname;
    private String MSBcom;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
    private String defName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy_fix);
        context=getApplicationContext();
        intent=getIntent();
        vLat=(TextView) findViewById(R.id.latitudeC);
        vLon=(TextView) findViewById(R.id.longitudeC);
        vAlt=(TextView) findViewById(R.id.altitudeC);
        vTitle=(TextView) findViewById(R.id.titleC);
        vComment=(TextView) findViewById(R.id.commentC);
        vFrom=(TextView) findViewById(R.id.fromC);
        vName=(EditText) findViewById(R.id.nameC);
        bCancel=(Button) findViewById(R.id.cancelC);
        bChange=(Button) findViewById(R.id.changeC);
        bEnter=(Button) findViewById(R.id.acceptC);
        loca[0]=(Location) intent.getParcelableExtra("Location1");
        loca[1]=(Location) intent.getParcelableExtra("Location2");
        which=(Integer) intent.getIntExtra("Which",0);
        MSBname=(String) intent.getStringExtra("MSBname");
        MSBcom=(String) intent.getStringExtra("MSBcom");
        name=(String) intent.getStringExtra("Name");
        if (which<0 || which>1) which=0;
        vTitle.setText("Copy location from "+MSBname);
        vComment.setText(MSBcom);
        if (name!=null) vName.setText(name);
        setVar();
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Cancelled",Toast.LENGTH_LONG).show();
                notAv();
            }
        });
        bEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name=vName.getText().toString();
                if (name!=null) name.trim();
                if (name==null || name.isEmpty()){
                    name=defName;
                }
                intent=new Intent();
                intent.putExtra("Name",name);
                intent.putExtra("Which",which);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        bChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                which++;
                which=which & 1;
                setVar();
            }
        });
    }

    void setVar(){
        Calendar cal=Calendar.getInstance();
        Long tim;
        switch (which){
            case 0: {
                vFrom.setText("From FIRST location:");
                bChange.setText("From End");
                vLat.setText(String.format(Locale.ENGLISH,"%.6f",loca[0].getLatitude()));
                vLon.setText(String.format(Locale.ENGLISH,"%.6f",loca[0].getLongitude()));
                vAlt.setText(String .format(Locale.ENGLISH,"%.2f",loca[0].getAltitude()));
                break;
            }
            case 1:{
                vFrom.setText("From LAST location:");
                bChange.setText("From Start");
                vLat.setText(String.format(Locale.ENGLISH,"%.6f",loca[1].getLatitude()));
                vLon.setText(String.format(Locale.ENGLISH,"%.6f",loca[1].getLongitude()));
                vAlt.setText(String .format(Locale.ENGLISH,"%.2f",loca[1].getAltitude()));
                break;
            }
        }
        tim=loca[which].getTime();
        if (tim!=null) {
            cal.setTimeInMillis(tim);
        }
        defName=sdf.format(cal.getTime());
        vName.setHint(defName);
    }

    void notAv(){
        setResult(RESULT_CANCELED);
        finish();
    }
}
