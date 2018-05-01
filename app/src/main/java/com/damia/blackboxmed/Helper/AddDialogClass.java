package com.damia.blackboxmed.Helper;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.damia.blackboxmed.R;

import java.util.Calendar;

public class AddDialogClass extends Dialog {

    public Activity c;
    private Button in_add;
    private EditText in_type, in_value, in_units, in_date, in_time;
    private TextView in_err;
    private int value_f;
    private String type;
    private String value;
    private String units;
    private String createdAt;
    private String date;
    private String time;


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

        in_add = findViewById(R.id.in_add);

        in_date = findViewById(R.id.in_date);
        in_time = findViewById(R.id.in_time);
        in_type = findViewById(R.id.in_type);
        in_value = findViewById(R.id.in_value);
        in_units = findViewById(R.id.in_units);
        in_err = findViewById(R.id.in_err);
        in_err.setText("");


        in_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                type = in_type.getText().toString();
                units = in_units.getText().toString();
                value = in_value.getText().toString();
                date = in_date.getText().toString();
                time = in_time.getText().toString();
                createdAt = date+"T"+time+":00.0000Z";

                if(type.equals("") || units.equals("") || value.equals("") || createdAt.equals("T:00.0000Z")){

                    in_err.setText("Please fill all the fields");

                }   else if(!date.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})")) {

                    in_err.setText("Invalid date format, yyyy-mm-dd");

                }   else if(!time.matches("([0-9]{2}):([0-9]{2})")) {

                    in_err.setText("Invalid time format, HH:mm");

                } else {
                    try {
                        value_f = Integer.valueOf(value);
                        DBSQLiteHelper database = new DBSQLiteHelper(getContext());
                        Measurement d = new Measurement(type, units, value_f, createdAt);
                        database.addHandler(d);

                        Toast.makeText(getContext(),
                                "Measure added!",
                                Toast.LENGTH_SHORT).show();
                        dismiss();

                    } catch (Exception e){
                        in_err.setText("Value input should be a number");
                    }


                }
            }
        });

    }
}
