package org.js.Msb2And;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Storage extends AppCompatActivity {

    Intent intent;
    public String startTime;
    public String startName;
    public Long Duration;
    public Integer nbData;
    public String pathData;
    public String pathMeta;
    public String plane;
    public String comment;
    public TextView viewStart;
    public TextView viewLoc;
    public TextView viewDuration;
    public TextView viewNb;
    public TextView viewPathData;
    public TextView viewPathMeta;
    public EditText viewPlane;
    public EditText viewComment;
    public Button bForget;
    public Button bSave;
    Location startLoc=null;
    Location prev_Loca=null;
    String pathStartGPS=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        intent=getIntent();
        startTime=intent.getStringExtra("startTime");
        startName=intent.getStringExtra("startName");
        Duration=intent.getLongExtra("Duration",0);
        nbData=intent.getIntExtra("nbData",0);
        pathData=intent.getStringExtra("pathData");
        pathMeta=intent.getStringExtra("pathMeta");
        plane=intent.getStringExtra("plane");
        comment=intent.getStringExtra("comment");
        startLoc=intent.getParcelableExtra("startLoc");
        prev_Loca=intent.getParcelableExtra("prevLoca");
        pathStartGPS=intent.getStringExtra("StartGPS");
        viewStart=(TextView) findViewById(R.id.startTime);
        viewLoc=(TextView) findViewById(R.id.startLoc);
        viewDuration=(TextView) findViewById(R.id.duration);
        viewNb=(TextView) findViewById(R.id.nbData);
        viewPathData=(TextView) findViewById(R.id.dataFile);
        viewPathMeta=(TextView) findViewById(R.id.metaFile);
        viewPlane=(EditText) findViewById(R.id.plane);
        viewComment=(EditText) findViewById(R.id.comment);
        bForget=(Button) findViewById(R.id.forget);
        bSave=(Button) findViewById(R.id.record);
        viewStart.setText(startTime);
        if (startName==null) viewLoc.setText("");
        else viewLoc.setText(startName);
        Float minutes= Float.valueOf(Duration)/60;
        viewDuration.setText(String.format(Locale.ENGLISH,"%.1f",minutes));
        viewNb.setText(String.valueOf(nbData));
        viewPathData.setText(pathData);
        viewPathMeta.setText(pathMeta);
        viewPlane.setText(plane);
        viewComment.setText(comment);
        bForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED,intent);
                ckRescue();
            }
        });
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plane=viewPlane.getText().toString();
                comment=viewComment.getText().toString();
                intent.putExtra("plane",plane);
                intent.putExtra("comment",comment);
                setResult(RESULT_OK,intent);
                ckRescue();
            }
        });
    }

    void ckRescue(){
        if (pathStartGPS==null) finish();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        if (startLoc==null || prev_Loca==null) return;
        Float dist=startLoc.distanceTo(prev_Loca);
        if (dist<50.0f) return;
        Calendar now= Calendar.getInstance();
        final String defName=sdf.format(now.getTime());
        String situation=String.format(Locale.ENGLISH,
                "The distance from the start location to the last known location is %d m",
                dist.intValue());
        String LastKL=String.format(Locale.ENGLISH,"lat. %.6f, lon. %.6f, alt. %.2f",
                prev_Loca.getLatitude(),prev_Loca.getLongitude(),prev_Loca.getAltitude());
        String mesg=situation+"\nDo you want to save the last known location \n"+LastKL+
                "\nunder the name "+defName+" in the StartGPS.gpx file?";
        String geo=String.format(Locale.ENGLISH,"geo:0,0?q=%.6f,%.6f(%s)",
                prev_Loca.getLatitude(),prev_Loca.getLongitude(),defName);
        final Uri uriGeo=Uri.parse(geo);
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        build.setTitle("Retrieval Help");
        build.setMessage(mesg);
        build.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveLKL(defName,prev_Loca);
                        Intent mapIntent=new Intent(Intent.ACTION_VIEW,uriGeo);
                        startActivity(mapIntent);
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        build.show();
    }

    void saveLKL(String name, Location loc){
        StartGPS sGPS=new StartGPS(pathStartGPS);
        Map<String,Location> startPoints=sGPS.readSG();
        startPoints.put(name,loc);
        sGPS.writeSG(startPoints);
    }

}
