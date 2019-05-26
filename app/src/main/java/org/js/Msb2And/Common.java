package org.js.Msb2And;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by js on 4/29/18.
 */

public class Common {

    public WeakReference<MainActivity> mActivity;
    public Context context;
    public boolean mountedSD;
    public boolean writeSD;
    public String pathMSBlog;
    public String pathAddr;
    public String pathStartGPS;
    public boolean isMSBlog;
    public boolean isAddr;
    public boolean named;
    public int minMSBnb=5000;
    public String plane="";
    public String comment="";
    public String startName="";
    public String Directory;
    public String sltMSBf=null;
    public String pathMeta=null;
    public int REQUEST_WRITE_STORAGE=100;
    String exPath=Environment.getExternalStorageDirectory().getAbsolutePath();

    public void fetchPref(Context context){
        SharedPreferences pref=context.getSharedPreferences(
                context.getString(R.string.PrefName),0);
        minMSBnb=pref.getInt("minMSBnb",5000);
        plane=pref.getString("Plane","");
        comment=pref.getString("Comment","");
        startName=pref.getString("StartName",null);
        Directory=pref.getString("Directory",exPath);
        return;
    }

    public void putPref(Context context){
        SharedPreferences pref=context.getSharedPreferences(
                context.getString(R.string.PrefName),0);
        SharedPreferences.Editor edit=pref.edit();
        edit.putInt("minMSBnb",minMSBnb);
        edit.putString("Plane",plane);
        edit.putString("Comment",comment);
        edit.putString("StartName",startName);
        edit.putString("Directory",Directory);
        edit.apply();
        return;
    }

    public void checkStorage(WeakReference<MainActivity> mActivity, Context context){
        this.mActivity=mActivity;
        this.context=context;
        String exPath= Environment.getExternalStorageDirectory().getAbsolutePath();
        pathMSBlog=exPath+"/"+context.getString(R.string.MSBlog);
        String state=Environment.getExternalStorageState();
        mountedSD=state.contains(Environment.MEDIA_MOUNTED);
        if (!mountedSD){
            Toast.makeText(context,exPath+" not mounted: no recording.",
                    Toast.LENGTH_LONG).show();
            checkMSBlog();
        }
        writeSD=!Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
        if (!writeSD){
            Toast.makeText(context,exPath+" not writable: no recording.",
                    Toast.LENGTH_LONG).show();
            checkMSBlog();
        }
        boolean hasPermission= (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED);
        if (!hasPermission){
            Toast.makeText(context,"This application need to write to "+
            exPath+".",Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(mActivity.get(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else checkMSBlog();
    }

    public void bye(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            mActivity.get().finishAndRemoveTask();
        } else mActivity.get().finish();
    }

    public void checkMSBlog(){
        if (!writeSD) {
            mActivity.get().start1();
            return;
        }
        File storageDir=new File(pathMSBlog);
        isMSBlog=storageDir.exists();
        if (!isMSBlog) isMSBlog=storageDir.mkdirs();
        if (!isMSBlog) {
            Toast.makeText(context, "The directory " + pathMSBlog + " is not created!",
                    Toast.LENGTH_LONG).show();
            mActivity.get().start1();
        } else {
            pathAddr=pathMSBlog+"/AddrSens.txt";
            pathStartGPS=pathMSBlog+"/StartGPS.gpx";
            File faddr=new File(pathAddr);
            isAddr=faddr.exists();
            if (!isAddr){
                AlertDialog.Builder build=new AlertDialog.Builder(mActivity.get());
                build.setMessage("Create default AddrSens.txt?")
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                bye();
                            }
                        })
                        .setTitle("Naming Sensors")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isAddr=mActivity.get().defAddr();
                                mActivity.get().start1();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isAddr=false;
                                mActivity.get().start1();
                            }
                        });
                build.show();
            } else {
                mActivity.get().start1();
            }
        }
    }

    public void getMSBdirs(){
        if (!writeSD) return;
        File dirM=new File(pathMSBlog);
        while (sltMSBf==null){
            if (minMSBnb>9999) return;
            String dirAll=String.format(Locale.ENGLISH,"Alle%02dxx",(int)(minMSBnb/100));
            File fAlle=new File(dirM,dirAll);
            if (fAlle.exists() && !fAlle.isDirectory()) continue;
            if (!fAlle.exists()){
                if (!fAlle.mkdirs()) continue;
            }
            String candMSB=String.format(Locale.ENGLISH,"MSB_%04d",minMSBnb)+".csv";
            File fMSB=new File(fAlle,candMSB);
            if (fMSB.exists()){
                minMSBnb++;
                continue;
            }
            sltMSBf=fMSB.getPath();
            String candMeta=String.format(Locale.ENGLISH,"MSB_%04d",minMSBnb)+".txt";
            File fMeta=new File(dirM,candMeta);
            if (!fMeta.exists()) pathMeta=fMeta.getPath();
            return;
        }
    }
}
