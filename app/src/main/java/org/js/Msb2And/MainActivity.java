package org.js.Msb2And;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Context context;
    Common com=new Common();
    WeakReference<MainActivity> mAct;
    private static String testPath;
    String Directory;
    Integer From=null;
    Location startLoc=null;
    StartGPS sGPS=null;
    Map<String,Location> startPoints=new HashMap<>();
    ArrayList<Location> biLoc=null;
    String[] ar=new String[0];
    Integer preselect=null;
    String refPath =null;
    String refDirectory =null;
    final int activGetFix=10;
    final int activHandFix=11;
    final int activCopyFix=12;
    final int selRefGpx =15;
    Button bRef =null;
    Button bStLo=null;
    CheckBox ckUp;
    Boolean firstStart=true;

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

    @Override
    protected void onResume(){
        super.onResume();
    }

    public void start1(){
        if (firstStart) {
            final String pathStartGPS = com.pathStartGPS;
            final Button bSouf = (Button) findViewById(R.id.buttonSouf);
            final Button bRx = (Button) findViewById(R.id.buttonRx);
            final Button bSim = (Button) findViewById(R.id.buttonSim);
            final Button bStop = (Button) findViewById(R.id.buttonStop);
            final CheckBox cNamed = (CheckBox) findViewById(R.id.checkNamed);
            bRef = (Button) findViewById(R.id.bRef);
            bStLo=(Button) findViewById(R.id.bStLo);
            ckUp=(CheckBox) findViewById(R.id.upCheck);
            refPath = null;
            File f = new File(pathStartGPS);
            refDirectory=f.getParent();
            bRef.setText("-none-");
            com.getMSBdirs();
            com.named = com.isAddr;
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
                    if (com.named) com.named = cNamed.isChecked();
                    From = getResources().getInteger(R.integer.Soufl);
                    launchMonitor();
                }
            });
            bRx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (com.named) com.named = cNamed.isChecked();
                    From = getResources().getInteger(R.integer.Rx);
                    launchMonitor();
                }
            });
            bSim.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (com.named) com.named = cNamed.isChecked();
                    From = getResources().getInteger(R.integer.Simu);
                    Intent intent = new Intent(MainActivity.this, Selector.class);
                    intent.putExtra("CurrentDir", Directory);
                    intent.putExtra("WithDir", false);
                    intent.putExtra("Mask", "MSB_\\d{4}+\\.csv");
                    intent.putExtra("Title", "Select a log file from ");
                    if (testPath != null) intent.putExtra("Previous", testPath);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, 2);
                }
            });
            bRef.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectRef();
                }
            });
            String st=com.startName;
            if (st==null) bStLo.setText("-none-");
            else bStLo.setText(com.startName);
            bStLo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    methodGPS();
                }
            });
            firstStart=false;
        }
    }

    void selectRef(){
        Intent intent=new Intent(MainActivity.this, Selector.class);
        if (refDirectory !=null) intent.putExtra("CurrentDir", refDirectory);
        intent.putExtra("WithDir",false);
        intent.putExtra("Mask","(?i).+\\.gpx");
        intent.putExtra("Title","Reference GPX?      ");
        if (refPath !=null) intent.putExtra("Previous", refPath);
        else intent.putExtra("Previous",com.pathStartGPS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent, selRefGpx);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
            switch (requestCode) {
                case 1:
                    if (resultCode==RESULT_OK) {
                        com.plane = data.getStringExtra("plane");
                        com.comment = data.getStringExtra("comment");
                        com.startName = data.getStringExtra("startName");
                        com.putPref(context);
                    }
                    com.bye();
                    break;
                case 2:
                    if (resultCode==RESULT_OK) {
                        testPath = data.getStringExtra("Path");
                        if (testPath == null || testPath.isEmpty()) com.bye();
                        else {
                            File f = new File(testPath);
                            Directory = f.getParent();
                            com.Directory = Directory;
                            From=getResources().getInteger(R.integer.Simu);
                            launchMonitor();
                        }
                    } else com.bye();
                    break;
                case activGetFix:
                    if (resultCode==RESULT_OK){
                        startLoc=data.getExtras().getParcelable("Location");
                        com.startName=data.getStringExtra("Name");
                        dupLoc(requestCode,0);
                    } else {
                        com.startName=null;
                        bStLo.setText("-none-");
                    }
                    break;
                case activHandFix:
                    if (resultCode==RESULT_OK){
                        startLoc=data.getExtras().getParcelable("Location");
                        com.startName=data.getStringExtra("Name");
                        dupLoc(requestCode,0);
                    } else {
                        com.startName=null;
                        bStLo.setText("-none-");
                    }
                    break;
                case activCopyFix:
                    if (resultCode==RESULT_OK){
                        com.startName=data.getStringExtra("Name");
                        int which=data.getIntExtra("Which",0);
                        startLoc=biLoc.get(which);
                        dupLoc(requestCode,which);
                    } else {
                        com.startName=null;
                        bStLo.setText("-none-");
                    }
                    break;
                case selRefGpx:
                    if (resultCode==RESULT_OK){
                        refPath =data.getStringExtra("Path");
                        if (refPath ==null || refPath.isEmpty()) refPath =null;
                    } else refPath =null;
                    if (refPath ==null) bRef.setText("-none-");
                    else {
                       File f=new File(refPath);
                       String bGname = (f.getName());
                       refDirectory =f.getParent();
                       bRef.setText(bGname);
                    }
                break;
            }
    }

    void dupLoc(int requestCode, final int selWhich){
        if (sGPS==null) sGPS=new StartGPS(com.pathStartGPS);
        startPoints=sGPS.readSG();
        if (startLoc!=null && com.startName!=null && !com.startName.isEmpty()) {
            if (startPoints.containsKey(com.startName)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Duplicate name")
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                com.startName = null;
                                bStLo.setText("-none-");
                            }
                        })
                        .setTitle(com.startName)
                        .setPositiveButton("Overwrite", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startPoints.remove(com.startName);
                                startPoints.put(com.startName, startLoc);
                                sGPS.writeSG(startPoints);
                                bStLo.setText(com.startName);
                            }
                        })
                        .setNegativeButton("Change", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCopy(selWhich);
                            }
                        });
                builder.show();
            } else {
                startPoints.put(com.startName, startLoc);
                sGPS.writeSG(startPoints);
                bStLo.setText(com.startName);
            }
        }
    }

    void launchMonitor(){
        final String startName=com.startName;
        final String pathStartGPS=com.pathStartGPS;
        Intent intent=new Intent(MainActivity.this,Monitor.class);
        intent.putExtra("from",From);
        intent.putExtra("named",com.named);
        intent.putExtra("pathAddr",com.pathAddr);
        intent.putExtra("pathMSBfile",com.sltMSBf);
        intent.putExtra("pathMeta",com.pathMeta);
        intent.putExtra("plane",com.plane);
        intent.putExtra("comment",com.comment);
        intent.putExtra("pathStartGPS",pathStartGPS);
        intent.putExtra("refPath", refPath);
        intent.putExtra("Orient",ckUp.isChecked());
        if (testPath!=null) intent.putExtra("testPath",testPath);
        if (startName!=null){
            intent.putExtra("startName",startName);
            sGPS=new StartGPS(com.pathStartGPS);
            startPoints=sGPS.readSG();
            startLoc=startPoints.get(com.startName);
        }
        if (startLoc!=null) intent.putExtra("Location",startLoc);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(intent,1);
    }

    void methodGPS(){
        final String startName=com.startName;
        String[] items;
        Integer sz;
        sGPS=new StartGPS(com.pathStartGPS);
        startPoints=sGPS.readSG();
        if (startPoints.isEmpty()){
            items=new String[3];
            sz=0;
        } else {
            SortedSet<String> keys=new TreeSet<>();
            keys.addAll(startPoints.keySet());
            sz=keys.size();
            Iterator<String> itr=((TreeSet<String>) keys).descendingIterator();
            ar=new String[sz];
            items=new String[sz+3];
            String here;
            for (int i=0;i<sz;i++) {
                here = itr.next();
                ar[i] = here;
                if (startName != null && here.contentEquals(startName)) preselect = i;
                items[i] = ar[i] + ": ";
                items[i] += String.format(Locale.ENGLISH, "lat=%.6f ",
                        startPoints.get(here).getLatitude());
                items[i] += String.format(Locale.ENGLISH, "lon=%.6f ",
                        startPoints.get(here).getLongitude());
                items[i] += String.format(Locale.ENGLISH, "alt=%.1f",
                        startPoints.get(here).getAltitude());
            }
        }
        items[sz]="Use device GPS";
        items[sz+1]="Enter a known location";
        items[sz+2]="Copy location from a previous flight";
        if (preselect==null) preselect=sz;
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        build.setTitle("Selection of Start Location");
        build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (preselect==null || preselect<0){
                    com.startName=null;
                    bStLo.setText("-none-");
                } else if (preselect<ar.length){
                    com.startName=ar[preselect];
                    startLoc=startPoints.get(com.startName);
                    bStLo.setText(com.startName);
                } else if (preselect==ar.length){
                    startLocate();
                } else if (preselect==ar.length+1){
                    startEnter();
                } else {
                    selGPX();
                }
            }
        })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        com.startName=null;
                        bStLo.setText("-none-");
                    }
                })
           .setSingleChoiceItems(items, preselect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preselect=which;
                    }
                })
           .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        com.startName=null;
                        bStLo.setText("-none-");
                    }
                });
        build.show();

    }

    void startLocate(){
        Intent intent=new Intent(this,GetFix.class);
        if (com.startName!=null) intent.putExtra("Name",com.startName);
        if (startLoc!=null) intent.putExtra("Location",startLoc);
        startActivityForResult(intent,activGetFix);
    }

    void startEnter(){
        Intent intent=new Intent(this,HandFix.class);
        if (com.startName!=null) intent.putExtra("Name",com.startName);
        if (startLoc!=null) intent.putExtra("Location",startLoc);
        startActivityForResult(intent,activHandFix);
    }

    void startCopy(int which){
        metaData m=new metaData();
        String name=FileListe.get(which);
        m.extract(name);
        String pathGps=m.getPathGpx();
        Toast toast=Toast.makeText(context,"Reading "+name,Toast.LENGTH_LONG);
        toast.show();
        if (sGPS==null) sGPS=new StartGPS(com.pathStartGPS);
        biLoc=sGPS.readTrack(pathGps);
        if (biLoc!=null && biLoc.size()==2) {
            Intent intent=new Intent(context,CopyFix.class);
            intent.putExtra("MSBcom",m.getComment());
            intent.putExtra("MSBname",FileListe.get(which));
            intent.putExtra("Location1",biLoc.get(0));
            intent.putExtra("Location2",biLoc.get(1));
            intent.putExtra("Which",0);
            if (com.startName!=null) intent.putExtra("Name",com.startName);
            if (startLoc!=null) intent.putExtra("Location",startLoc);
            startActivityForResult(intent,activCopyFix);
        } else {
            com.startName=null;
            bStLo.setText("-none-");
        }

    }

    ArrayList<String> FileListe=new ArrayList<>();
    void selGPX(){
        ArrayList<String> Liste=new ArrayList<>();
        File f1=new File(com.pathMSBlog);
        FilenameFilter meta=new filterMeta("MSB_\\d{4}+\\.txt");
        if (f1.isDirectory()){
            String s[] = f1.list(meta);
            if (s.length>0){
                Arrays.sort(s);
                metaData m=new metaData();
                for (int i = 0; i < s.length; i++) {
                    String sName = s[i].replace(".txt", "");
                    if (!m.extract(sName)) continue;
                    File g = new File(m.getPathGpx());
                    if (!g.exists()) continue;
                    String line = sName + " / " + m.getDay() + " / " + m.getPlane() + " / " + m.getComment();
                    Liste.add(line);
                    FileListe.add(sName);
                }
            }
        }
        ar=Liste.toArray(new String[0]);
        if (ar.length>0){
            final AlertDialog.Builder build=new AlertDialog.Builder(this);
            build.setTitle("Choose a flight for the GPX");
            build.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    com.startName=null;
                    launchMonitor();
                }
            })
                    .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            com.startName=null;
                            launchMonitor();
                        }
                    })
                    .setItems(ar, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startCopy(which);
                        }
                    });
            build.show();
        } else {
            Toast toast=Toast.makeText(this,"No file in the directory!",
                    Toast.LENGTH_LONG);
            toast.show();
            com.startName=null;
            launchMonitor();
        }
    }

    class filterMeta implements FilenameFilter {

        private Pattern mask;

        public filterMeta(String m) {
            mask = Pattern.compile(m);
        }

        public boolean accept(File dir, String name) {
            Matcher mat = mask.matcher(name);
            return mat.matches();
        }
    }

    class metaData {

        String date;
        String plane;
        String comment;
        String startName = null;
        String MsbName;
        String pathCsv;
        String pathHtml;
        String pathGpx;
        String pathKml;
        String pathTXT;
        Calendar startTime = Calendar.getInstance();

        public boolean extract(String name) {
            pathTXT = com.pathMSBlog + "/" + name + ".txt";
            pathHtml = com.pathMSBlog + "/" + name + ".html";
            pathGpx = com.pathMSBlog + "/" + name + ".gpx";
            pathKml = com.pathMSBlog + "/" + name + ".kml";
            try {
                BufferedReader f = new BufferedReader(new FileReader(pathTXT));
                for (int i = 0; i < 3; i++) {
                    String line = f.readLine();
                    if (line.startsWith("Date: ")) {
                        date = line.replaceFirst("Date: ", "");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        try {
                            Date dd = sdf.parse(date, new ParsePosition(0));
                            startTime.setTime(dd);
                        } catch (Exception e) {
                            startTime = Calendar.getInstance();
                        }
                    } else if (line.startsWith("Plane: ")) {
                        plane = line.replaceFirst("Plane: ", "");
                    } else if (line.startsWith("Comment: ")) {
                        comment = line.replaceFirst("Comment: ", "");
                    } else if (line.startsWith("StartName: ")) {
                        startName = line.replace("StartName: ", "");
                        startName.trim();
                        if (startName.isEmpty()) startName = null;
                    }
                }
            } catch (IOException e) {
                return false;
            }
            return ((date != null) && (plane != null) && (comment != null));
        }

        public String getDay (){
            Pattern pat=Pattern.compile("[ ]");
            String fields[]=pat.split(date);
            return fields[0];
        }

        public String getPlane (){
            return plane;
        }

        public String getComment (){
            return comment;
        }

        public String getPathGpx(){
            return pathGpx;
        }
    }
}
