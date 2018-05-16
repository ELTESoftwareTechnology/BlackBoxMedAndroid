package com.damia.blackboxmed.Helper;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import com.damia.blackboxmed.R;

import java.util.ArrayList;

public class FitDataAdapter extends BaseAdapter implements Filterable {
    private ArrayList<Measurement> postList=null;
    private Context context=null;
    private ArrayList<Measurement> filtList;

    public FitDataAdapter(ArrayList<Measurement> postList, Context context)
    {
        this.postList=postList;
        this.context=context;
        this.filtList=null;
    }

    @Override
    public int getCount()
    {
        if(filtList==null){
            return postList.size();
        } else {
            return filtList.size();
        }
    }

    public ArrayList<Measurement> getMeasList() {
        if(filtList==null){
            return postList;
        } else {
            return filtList;
        }
    }

    @Override
    public Object getItem(int position)
    {
        if(filtList==null){
            return postList.get(position);
        } else {
            return filtList.get(position);
        }
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
        ImageButton imgRes = (ImageButton) v.findViewById(R.id.img);

        txtType.setText(p.getType());
        String m = " "+String.valueOf(p.getValue())+" "+p.getUnit()+" ";
        txtMeasure.setText(m);


        int id_img = context.getResources().getIdentifier(p.getImg_res(), null, context.getPackageName());
        imgRes.setImageResource(id_img);

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

        ImageButton btnDelete = (ImageButton) v.findViewById(R.id.img);
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

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            ArrayList<Measurement> tempList = new ArrayList<Measurement>();
            if (constraint != null && postList != null) {
                int length = postList.size();
                int i = 0;
                while (i < length) {
                    Measurement item = postList.get(i);

                    if (item.getType().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            item.getUnit().toLowerCase().contains(constraint.toString().toLowerCase())) {

                        tempList.add(item);
                    }
                    i++;
                }

                filterResults.values = tempList;
                filterResults.count = tempList.size();
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence contraint, FilterResults results) {
            filtList = (ArrayList<Measurement>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
}