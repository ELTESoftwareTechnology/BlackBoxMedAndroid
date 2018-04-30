package com.damia.blackboxmed.Helper;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.damia.blackboxmed.Activities.ActivityHome;
import com.damia.blackboxmed.R;

public class AddDialogClass extends Dialog {

    public Activity c;
    public Dialog d;
    //dialog
    Button in_add;
    EditText in_type, in_value, in_units, in_date, in_time;
    int value_f;
    String type, value, units, createdAt;


    public AddDialogClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_measure);

        in_add = (Button) findViewById(R.id.in_add);

        in_date = (EditText) findViewById(R.id.in_date);
        in_time = (EditText) findViewById(R.id.in_time);
        in_type = (EditText) findViewById(R.id.in_type);
        in_value = (EditText) findViewById(R.id.in_value);
        in_units = (EditText) findViewById(R.id.in_units);



        in_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = in_type.getText().toString();
                units = in_units.getText().toString();
                value = in_value.getText().toString();
                createdAt = in_date.getText().toString()+"T"+in_time.getText().toString()+":00.0000Z";

                if(type == "" || units == "" || value == "" || createdAt == "T:00.0000Z"){

                    Toast.makeText(getContext(),
                            "Missing fields",
                            Toast.LENGTH_SHORT).show();
                } else {
                    value_f = Integer.valueOf(value);
                    System.out.println(type+" "+units+" "+value_f+" "+createdAt);
                    DBSQLiteHelper database = new DBSQLiteHelper(getContext());
                    Measurement d = new Measurement(type, units, value_f, createdAt);
                    database.addHandler(d);

                    Toast.makeText(getContext(),
                            "Measure added!",
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        });

    }
}
