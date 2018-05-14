package com.damia.blackboxmed.Helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.damia.blackboxmed.Activities.HomeActivity;
import com.damia.blackboxmed.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DataAdapter extends BaseAdapter {
    private ArrayList<Measurement> postList=null;
    private Context context=null;


    public DataAdapter(ArrayList<Measurement> postList, Context context)
    {
        this.postList=postList;
        this.context=context;
    }

    @Override
    public int getCount()
    {
        return postList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return postList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(final int position, View v, ViewGroup vg)
    {

        if (v==null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_data, null);
        }

        Measurement p =(Measurement) getItem(position);
        TextView txtType = (TextView) v.findViewById(R.id.type);
        TextView txtMeasure = (TextView) v.findViewById(R.id.measurement);
        TextView txtDate = (TextView) v.findViewById(R.id.date);
        TextView txtID = (TextView) v.findViewById(R.id.idField);

        txtType.setText(p.getType());
        String m = " "+String.valueOf(p.getValue())+" "+p.getUnit()+" ";
        txtMeasure.setText(m);

        try{
            String sub1, sub12, sub2;
            String[] parts1, parts2;
            parts1 = p.getCreatedAt().split("T");

            sub1 = parts1[0];
            sub12 = parts1[1];
            parts2 = sub12.split("\\.");
            sub2 = parts2[0];

            txtDate.setText(sub1+" at "+sub2);

        } catch (Exception e) {
            txtDate.setText(p.getCreatedAt());
        }

        ImageButton btnDelete = (ImageButton) v.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Measurement m =(Measurement) getItem(position);

                new android.app.AlertDialog.Builder(context)
                        .setTitle("Delete this measure?")
                        .setMessage(""+m.getType().toUpperCase()+": "+m.getValue()+" "+m.getUnit())
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {

                                DatabaseHelper db = new DatabaseHelper(context);
                                db.deleteMeasurement(m.getCreatedAt());
                                postList.remove(m);
                                notifyDataSetChanged();
                            }
                        }).create().show();
            }
        });
        return v;
    }

}
