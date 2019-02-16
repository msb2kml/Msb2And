package org.js.Msb2And;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by js on 4/5/18.
 */

public class MyListAdapter extends BaseAdapter {

    private List<SensorReading> mSensor;
    private Context mContext;
    private WeakReference<Monitor> mActivity;
    private boolean named;
    private String pathAddr;
//    public String[] names=new String[16];

    public MyListAdapter(final Context context, final WeakReference<Monitor> mActivity,
                         final List<SensorReading> mSensor){
        this.mContext=context;
        this.mSensor=mSensor;
        this.mActivity=mActivity;
        named=mActivity.get().named;
/*
        if (named){
            pathAddr=mActivity.get().pathAddr;
            File addr=new File(pathAddr);
            try {
                BufferedReader f=new BufferedReader(new FileReader(addr));
                String line="";
                while (line!=null){
                    line=f.readLine();
                    if (line==null) continue;
                    if (line.startsWith("*")) continue;
                    String[] fields=line.split(";");
                    if (fields==null || fields.length<2) continue;
                    if (fields[0].matches(" A:[0-9]{2}")){
                        String sub=fields[0].substring(3);
                        int ind=Integer.valueOf(sub);
                        if (ind>=0 && ind<16) names[ind]=fields[1].trim();
                    }
                }
                f.close();
            } catch (Exception e){
                named=false;
            }
        }
*/
    }

    public List<SensorReading> getSensor(){
        return mSensor;
    }

    @Override
    public int getCount(){
        if (mSensor!= null) return mSensor.size();
        return 0;
    }

    @Override
    public long getItemId(int i){
        return i;
    }

    @Override
    public Object getItem(int i){
        return mSensor != null ? mSensor.get(i) : null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        ViewHolder viewHolder;
        String nam;
        Character deg='\ufffd';
        if (convertView==null){
            LayoutInflater inflater=((Activity) mContext).getLayoutInflater();
            convertView=inflater.inflate(R.layout.list_item,parent,false);
            viewHolder=new ViewHolder();
            viewHolder.number=(TextView)convertView.findViewById(R.id.number);
            viewHolder.value=(TextView)convertView.findViewById(R.id.value);
            viewHolder.unit=(TextView)convertView.findViewById(R.id.unit);
            viewHolder.minim=(TextView)convertView.findViewById(R.id.minim);
            viewHolder.maxim=(TextView)convertView.findViewById(R.id.maxim);
            convertView.setTag(viewHolder);
        } else {
            viewHolder=(ViewHolder) convertView.getTag();
        }
        SensorReading obj=mSensor.get(position);
        if (obj != null){
            if (named && mActivity.get().names[obj.addr]!=null){
                nam=String.valueOf(obj.addr)+"/"+mActivity.get().names[obj.addr];
            } else nam=String.valueOf(obj.addr);
            viewHolder.number.setText(nam);
            if (obj.alarm) viewHolder.number.setTextColor(Color.RED);
            else viewHolder.number.setTextColor(Color.BLACK);
            if (obj.valid) viewHolder.value.setText(obj.print());
            else viewHolder.value.setText("( "+obj.print()+" )");
            String head=obj.heading().replace(deg.toString(),"°");
            viewHolder.unit.setText(head);
            viewHolder.minim.setText(obj.print_min());
            viewHolder.maxim.setText(obj.print_max());
        }
        return convertView;
    }

    static class ViewHolder{
        TextView number;
        TextView value;
        TextView unit;
        TextView minim;
        TextView maxim;
    }
}
