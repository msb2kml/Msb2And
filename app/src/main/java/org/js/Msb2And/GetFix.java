package org.js.Msb2And;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class GetFix extends AppCompatActivity {

    private TextView vLat;
    private TextView vLon;
    private TextView vAlt;
    private TextView vPrec;
    private TextView vHPrec;
    private TextView vStat;
    private EditText vName;
    private Button bAccept;
    private Button bCancel;
    private Context context;
    private Double latitude;
    private Double longitude;
    private Double altitude=101.0;
    private Float precision;
    private Location loc=new Location("");
    private String name;
    private Intent intent;
    private LocationManager lm;
    private LocationProvider gps;
    private boolean granted=false;
    private boolean enabled=false;
    private boolean listen=false;
    private boolean rejectPerm=false;
    private boolean rejectEnbl=false;
    private boolean waitOK=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_fix);
        context = getApplicationContext();
        intent = getIntent();
        vLat = (TextView) findViewById(R.id.f_latitude);
        vLon = (TextView) findViewById(R.id.f_longitude);
        vAlt = (TextView) findViewById(R.id.f_altitude);
        vPrec = (TextView) findViewById(R.id.f_preci);
        vHPrec=(TextView) findViewById(R.id.f_hprec);
        vStat = (TextView) findViewById(R.id.status);
        vName = (EditText) findViewById(R.id.f_name);
        bAccept = (Button) findViewById(R.id.accept);
        bCancel = (Button) findViewById(R.id.cancel);
        loc = (Location) intent.getParcelableExtra("Location");
        name = (String) intent.getStringExtra("Name");
        if (loc == null) {
            bAccept.setEnabled(false);
            vLat.setText("-");
            vLon.setText("-");
            vAlt.setText("-");
            vPrec.setText("-");
            vHPrec.setText("-");
        } else {
            vLat.setText(String.format(Locale.ENGLISH,"%.6f",loc.getLatitude()));
            vLon.setText(String.format(Locale.ENGLISH,"%6f",loc.getLongitude()));
            vAlt.setText(String.format(Locale.ENGLISH,"%.2f",loc.getAltitude()));
            vPrec.setText(String.format(Locale.ENGLISH,"%.1f",loc.getAccuracy()));
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                vHPrec.setText(String.format(Locale.ENGLISH, "%.1f",
                        loc.getVerticalAccuracyMeters()));
            }
            bAccept.setEnabled(true);
            vStat.setText("Preset");
        }
        if (name!=null){
            vName.setText(name);
        }
        bAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listen) lm.removeUpdates(listener);
                name=vName.getText().toString();
                if (name!=null){
                    name.trim();
                }
                if (name==null || name.isEmpty()){
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                    Calendar startTime=Calendar.getInstance();
                    name=sdf.format(startTime.getTime());
                }
                intent=new Intent();
                intent.putExtra("Location",loc);
                intent.putExtra("Name",name);
                setResult(RESULT_OK,intent);
                finish();
            }
        });
        bCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context,"Cancelled",Toast.LENGTH_LONG).show();
                notAv();
            }
        });
        lm=(LocationManager) getSystemService(LOCATION_SERVICE);
        checkPerm();

    }

    @Override
    protected void onResume(){
        super.onResume();
        if (!waitOK) checkPerm();
    }

    void checkPerm() {
        granted = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            gps=lm.getProvider(LocationManager.GPS_PROVIDER);
            if (gps.equals(null)){
                vStat.setText("No GPS!");
                Toast.makeText(context,"GPS is not available!",Toast.LENGTH_LONG).show();
                notAv();
            }
            enabled=lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        if (granted && enabled) follow2();
        else {
            vStat.setText("No access to GPS");
            rejectPerm=true;
            waitOK=true;
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("GPS for "+getString(R.string.app_name))
                    .setMessage("Please enable GPS location and allow access for "+
                        getString(R.string.app_name))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                waitOK = false;
                                Intent intent;
                                if (granted){
                                    intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                                else {
                                    ActivityCompat.requestPermissions(GetFix.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                            100);
                                }
                            }
                        })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                notAv();
                            }
                        });
            builder.show();
        }
    }

    void follow2(){
        if (!listen) {
            vStat.setText("Waiting for GPS");
            try {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                        0, listener);
                listen = true;
            }
            catch (SecurityException e){
                notAv();
            }
        }
    }

    void notAv(){
        if (listen) lm.removeUpdates(listener);
        setResult(RESULT_CANCELED);
        finish();
    }

    private LocationListener listener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            loc = location;
            Integer nSat = loc.getExtras().getInt("satellites", -1);
            vLat.setText(String.format(Locale.ENGLISH, "%.6f", loc.getLatitude()));
            vLon.setText(String.format(Locale.ENGLISH, "%6f", loc.getLongitude()));
            vAlt.setText(String.format(Locale.ENGLISH, "%.2f", loc.getAltitude()));
            vPrec.setText(String.format(Locale.ENGLISH, "%.1f", loc.getAccuracy()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vHPrec.setText(String.format(Locale.ENGLISH, "%.1f",
                        loc.getVerticalAccuracyMeters()));
            }
            bAccept.setEnabled(true);
            vStat.setText("nb. satellites: " + nSat.toString());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status == LocationProvider.OUT_OF_SERVICE) {
                vStat.setText("GPS out of service");
            } else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                vStat.setText("GPS temporarily unavailable");
            } else if (status == LocationProvider.AVAILABLE) {
                GpsStatus gstat = lm.getGpsStatus(null);
                Integer tot_sat = 0;
                Integer fix_sat = 0;
                for (GpsSatellite sat : gstat.getSatellites()) {
                    if (sat.usedInFix()) fix_sat++;
                    tot_sat++;
                }
                vStat.setText("Satellites in use: " +
                        fix_sat.toString() + "/" + tot_sat.toString());
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            vStat.setText("GPS enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            vStat.setText("GPS disbled");
        }
    };
}
