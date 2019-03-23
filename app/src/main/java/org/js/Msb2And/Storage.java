package org.js.Msb2And;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

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
        viewDuration.setText(String.format(Locale.US,"%.1f",minutes));
        viewNb.setText(String.valueOf(nbData));
        viewPathData.setText(pathData);
        viewPathMeta.setText(pathMeta);
        viewPlane.setText(plane);
        viewComment.setText(comment);
        bForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED,intent);
                finish();
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
                finish();
            }
        });
    }

}
