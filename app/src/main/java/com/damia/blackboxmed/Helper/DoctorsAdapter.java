package com.damia.blackboxmed.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.damia.blackboxmed.R;

import java.util.ArrayList;

public class DoctorsAdapter extends BaseAdapter implements Filterable {
    private ArrayList<Doctor> postList;
    private ArrayList<Doctor> filtList;
    private Context context=null;

    public DoctorsAdapter(ArrayList<Doctor> postList, Context context)
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

    public ArrayList<Doctor> getDocList() {
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
    public View getView(int position, View v, ViewGroup vg)
    {

        if (v==null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_doctor, null);
        }

        Doctor d=(Doctor) getItem(position);
        TextView txtName = (TextView) v.findViewById(R.id.docName);
        TextView txtEmail = (TextView) v.findViewById(R.id.docEmail);

        txtName.setText(d.getFirstName()+" "+d.getLastName());
        txtEmail.setText(d.getEmail());

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
            ArrayList<Doctor> tempList = new ArrayList<Doctor>();
            if (constraint != null && postList != null) {
                int length = postList.size();
                int i = 0;
                while (i < length) {
                    Doctor item = postList.get(i);

                    if (item.getFirstName().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            item.getEmail().toLowerCase().contains(constraint.toString().toLowerCase()) ||
                            item.getLastName().toLowerCase().contains(constraint.toString().toLowerCase())) {

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
            filtList = (ArrayList<Doctor>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };
}