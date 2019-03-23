package org.js.Msb2And;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HandFix extends AppCompatActivity {

    private EditText vLat;
    private EditText vLon;
    private EditText vAlt;
    private EditText vName;
    private Button bEnter;
    private Button bCancel;
    private Context context;
    private Intent intent;
    private Location loc=new Location("");
    private String name;
    private NumberFormat nfe=NumberFormat.getInstance(Locale.ENGLISH);
    private Number num;
    private String defName;
    private SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_fix);
        context=getApplicationContext();
        intent=getIntent();
        vLat=(EditText) findViewById(R.id.h_latitude);
        vLon=(EditText) findViewById(R.id.h_longitude);
        vAlt=(EditText) findViewById(R.id.h_altitude);
        vName=(EditText) findViewById(R.id.h_name);
        bEnter=(Button) findViewById(R.id.h_enter);
        bCancel=(Button) findViewById(R.id.h_cancel);
        loc=(Location) intent.getParcelableExtra("Location");
        name=(String ) intent.getStringExtra("Name");
        if (loc!=null){
            vLat.setText(String.format(Locale.ENGLISH,"%.6f",loc.getLatitude()));
            vLon.setText(String.format(Locale.ENGLISH,"%6f",loc.getLongitude()));
            vAlt.setText(String.format(Locale.ENGLISH,"%.2f",loc.getAltitude()));
        }
        Calendar startTime=Calendar.getInstance();
        defName=sdf.format(startTime.getTime());
        if (name!=null) {
            vName.setText(name);
        }
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Cancelled",Toast.LENGTH_LONG);
                notAv();
            }
        });
        bEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Double latitude;
                Double longitude;
                Double altitude;
                name = vName.getText().toString();
                if (name != null) {
                    name.trim();
                }
                if (name == null || name.isEmpty()) {
                    name = defName;
                }
                String field;
                field = vLat.getText().toString();
                if (field != null) field.trim();
                if (field == null || field.isEmpty()) {
                    notValid("Latitude");
                    return;
                }
                try {
                    num = nfe.parse(field);
                    latitude = num.doubleValue();
                } catch (ParseException e) {
                    notValid("Latitude");
                    return;
                }
                if (latitude > 180 || latitude < -180) {
                    notValid("Latitude");
                    return;
                }
                field = vLon.getText().toString();
                if (field != null) field.trim();
                if (field == null || field.isEmpty()) {
                    notValid("Longitude");
                    return;
                }
                try {
                    num = nfe.parse(field);
                    longitude = num.doubleValue();
                } catch (ParseException e) {
                    notValid("Longitude");
                    return;
                }
                if (longitude > 180 || longitude < -180) {
                    notValid("Longitude");
                    return;
                }
                field = vAlt.getText().toString();
                if (field != null) field.trim();
                if (field == null || field.isEmpty()) {
                    notValid("Altitude");
                    return;
                }
                try {
                    num = nfe.parse(field);
                    altitude = num.doubleValue();
                } catch (ParseException e) {
                    notValid("Altitude");
                    return;
                }
                if (altitude > 5000 || altitude < -1000) {
                    notValid("Altitude");
                    return;
                }
                loc = new Location("");
                loc.setLatitude(latitude);
                loc.setLongitude(longitude);
                loc.setAltitude(altitude);
                intent = new Intent();
                intent.putExtra("Location", loc);
                intent.putExtra("Name", name);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    void notValid(String which){
        Toast.makeText(context,"Not valid: "+which,Toast.LENGTH_LONG).show();
    }

    void notAv() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
