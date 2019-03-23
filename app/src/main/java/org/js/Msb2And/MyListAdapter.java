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
    private List<Object> mObject;
    private Context mContext;
    private WeakReference<Monitor> mActivity;
    private boolean named;
    private String pathAddr;

    public MyListAdapter(final Context context, final WeakReference<Monitor> mActivity,
                         final List<Object> mObject){
//                         final List<SensorReading> mSensor){
        this.mContext=context;
//        this.mSensor=mSensor;
        this.mObject=mObject;
        this.mActivity=mActivity;
        named=mActivity.get().named;
    }

//    public List<SensorReading> getSensor(){ return mSensor; }
    public List<Object> getSensor(){ return mObject;}

    @Override
    public int getCount(){
        if (mObject!=null) return mObject.size();
//        if (mSensor!= null) return mSensor.size();
        return 0;
    }

    @Override
    public long getItemId(int i){
        return i;
    }

    @Override
    public Object getItem(int i){ return mObject !=null ? mObject.get(i) : null; }
//    public Object getItem(int i){ return mSensor != null ? mSensor.get(i) : null; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        String nam;
        Character deg='\ufffd';
        if (convertView==null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }
        ViewHolder viewHolder=(ViewHolder) convertView.getTag();
        if (viewHolder==null){
            viewHolder=new ViewHolder();
            viewHolder.number=(TextView)convertView.findViewById(R.id.number);
            viewHolder.value=(TextView)convertView.findViewById(R.id.value);
            viewHolder.unit=(TextView)convertView.findViewById(R.id.unit);
            viewHolder.minim=(TextView)convertView.findViewById(R.id.minim);
            viewHolder.maxim=(TextView)convertView.findViewById(R.id.maxim);
            convertView.setTag(viewHolder);
        }
        Object o=mObject.get(position);
        if (o.getClass().getSimpleName().matches("SensorReading")) {
            SensorReading obj = (SensorReading) o;
            if (obj != null) {
                if (named && mActivity.get().names[obj.addr] != null) {
                    nam = String.valueOf(obj.addr) + "/" + mActivity.get().names[obj.addr];
                } else nam = String.valueOf(obj.addr);
                viewHolder.number.setText(nam);
                if (obj.alarm) viewHolder.number.setTextColor(Color.RED);
                else viewHolder.number.setTextColor(Color.BLACK);
                if (obj.valid) viewHolder.value.setText(obj.print());
                else viewHolder.value.setText("( " + obj.print() + " )");
                String head = obj.heading().replace(deg.toString(), "°");
                viewHolder.unit.setText(head);
                viewHolder.minim.setText(obj.print_min());
                viewHolder.maxim.setText(obj.print_max());
            }
        } else if (o.getClass().getSimpleName().matches("CompReading")){
            CompReading c=(CompReading) o;
            if (c !=null){
                nam=c.heading;
                viewHolder.number.setText(nam);
                viewHolder.number.setTextColor(Color.BLACK);
                viewHolder.value.setText(c.print());
                viewHolder.unit.setText(c.func);
                viewHolder.maxim.setText(c.printMax());
                viewHolder.minim.setText(c.printMin());
            }
        }
        convertView.setVisibility(View.VISIBLE);
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
