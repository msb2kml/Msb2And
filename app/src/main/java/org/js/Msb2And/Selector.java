package org.js.Msb2And;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Selector extends AppCompatActivity {

    navDir nav=new navDir();
    String theList[];
    int theSelected=-1;
    String exPath=Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selector);
        Intent intent=getIntent();
        String currentDir=intent.getStringExtra("CurrentDir");
        if (currentDir==null) currentDir=exPath;
        nav.setCurDir(currentDir);
        String Mask=intent.getStringExtra("Mask");
        nav.setMask(Mask);
        Boolean WithDir=intent.getBooleanExtra("WithDir",false);
        if (WithDir) selWtDir();
        else selNoDir();
    }

    void selWtDir() {
        nav.setNoDir(false);
        theList = nav.get();
        theSelected = -1;
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        build.setTitle(nav.getDir())
           .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                })
           .setSingleChoiceItems(theList, theSelected, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        theSelected=which;
                    }
                })
           .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                })
           .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            show();
                    }
                })
           .setNegativeButton("Follow Directory", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (theList[theSelected].endsWith("/")){
                            if (theSelected==0){
                                nav.upDir();
                            }
                            else {
                                nav.dnDir(theList[theSelected]);
                            }
                        }
                        selWtDir();
                    }
                });
        build.show();
    }

    void selNoDir(){
        nav.setNoDir(false);
        theList=nav.get();
        theSelected=-1;
        AlertDialog.Builder build=new AlertDialog.Builder(this);
        build.setTitle(nav.getDir())
           .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                })
           .setItems(theList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        theSelected=which;
                        if (which==0){
                            nav.upDir();
                            selNoDir();
                        } else if (theList[which].endsWith("/")){
                            nav.dnDir(theList[which]);
                            selNoDir();
                        } else {
                            show();
                        }
                    }
                })
           .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                });
        build.show();
    }

    void show(){
       if (! (theSelected<0)) {
            String path;
            if (theSelected==0) {
                 path = nav.upDir();
            }
            else {
                path=nav.getDir()+"/"+theList[theSelected];
            }
            Intent result=new Intent();
            result.putExtra("Path",path);
           setResult(RESULT_OK,result);
       }
       finish();
    }

}
