package com.damia.blackboxmed.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.damia.blackboxmed.R;

import java.util.ArrayList;

public class DoctorsAdapter extends BaseAdapter {
    private ArrayList<Doctor> postList=null;
    private Context context=null;

    public DoctorsAdapter(ArrayList<Doctor> postList, Context context)
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
    public View getView(int position, View v, ViewGroup vg)
    {

        if (v==null) {
            v = LayoutInflater.from(context).inflate(R.layout.item_doctor, null);
        }

        Doctor d=(Doctor) getItem(position);
        TextView txtName = (TextView) v.findViewById(R.id.docName);
        TextView txtSurname = (TextView) v.findViewById(R.id.docSurname);
        TextView txtEmail = (TextView) v.findViewById(R.id.docEmail);

        txtName.setText(d.getFirstName());
        txtSurname.setText(d.getLastName());
        txtEmail.setText(d.getEmail());

        return v;
    }
}