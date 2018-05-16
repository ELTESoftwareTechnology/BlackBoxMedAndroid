package com.damia.blackboxmed.Helper;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.damia.blackboxmed.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddDialogClass extends Dialog {

    public Activity c;
    private Button in_add;
    private EditText in_type, in_value, in_units, in_date, in_time;
    private TextView in_err;
    private CheckBox checkBox;

    private int value_f;
    private String type;
    private String value;
    private String units;
    private String createdAt;
    private String date;
    private String date_p;
    private String time;
    private String savedUsername;
    private String sm;
    private String img_res;

    SharedPreferences session;

    Calendar myCalendar;
    Calendar myTimeCalendar;
    Date rightNow;

    SimpleDateFormat sdfFullDate;
    SimpleDateFormat sdfSecMillis;
    DatePickerDialog.OnDateSetListener date_d;
    TimePickerDialog.OnTimeSetListener time_d;

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

        myCalendar = Calendar.getInstance();
        myTimeCalendar = Calendar.getInstance();
        rightNow = Calendar.getInstance().getTime();

        session = PreferenceManager.getDefaultSharedPreferences(getContext());
        savedUsername = session.getString("usernamePref", "");

        String myFullDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSS";
        String mySecMillisFormat = "ss.SSSS";

        sdfFullDate = new SimpleDateFormat(myFullDateFormat, Locale.ITALY);
        sdfSecMillis = new SimpleDateFormat(mySecMillisFormat);



        checkBox = findViewById(R.id.checkBox);
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


                if(checkBox.isChecked()){

                    sm = sdfFullDate.format(rightNow);
                    createdAt = sm+"Z";

                } else {
                    date = in_date.getText().toString();
                    time = in_time.getText().toString();

                    sm = sdfSecMillis.format(rightNow);
                    createdAt = date+"T"+time+":"+sm+"Z";
                }


                if(type.equals("") || units.equals("") || value.equals("") || createdAt.equals("T"+sm+"Z")){
                    in_err.setText(R.string.input_err_fields);
                }   else if(!checkBox.isChecked() && !validateDateFormat(date)) {
                    in_err.setText(R.string.input_err_date);
                }   else if(!checkBox.isChecked() && !validateTimeFormat(time)) {
                    in_err.setText(R.string.input_err_time);
                } else {
                    try {
                        value_f = Integer.valueOf(value);


                        if(type.toLowerCase().equals("distance") || type.toLowerCase().equals("steps")) {
                            type = "Distance";
                            img_res = "drawable/steps";
                        } else if (type.toLowerCase().equals("weight")) {
                            type = "Weight";
                            img_res = "drawable/scale";
                        } else if (type.toLowerCase().equals("heartrate") ||
                                type.toLowerCase().equals("heart rate") ||
                                type.toLowerCase().equals("heart-rate")) {
                            type = "Heartrate";
                            img_res = "drawable/heart";
                        } else {
                            img_res = "drawable/info";
                        }

                        DatabaseHelper db = new DatabaseHelper(getContext());
                        Measurement d = new Measurement(type, units, value_f, createdAt, img_res, 0);
                        db.createMeasure(d, savedUsername);

                        Toast.makeText(getContext(),
                                "Measure added!",
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                    } catch (Exception e){
                        in_err.setText(R.string.input_err_num);
                    }
                }
            }
        });



        in_time.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);


                TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                in_time.setText(addZero(hourOfDay) + ":" + addZero(minute));
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        date_d = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        in_date.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                new DatePickerDialog(getContext(), date_d, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

               @Override
               public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {

                   if(isChecked){
                       in_date.setVisibility(View.GONE);
                       in_time.setVisibility(View.GONE);
                   } else {
                       in_date.setVisibility(View.VISIBLE);
                       in_time.setVisibility(View.VISIBLE);
                   }
               }
           }
        );

    }

    public static boolean validateTimeFormat(String time){
        return time.matches("([0-9]{2}):([0-9]{2})");
    }
    public static boolean validateDateFormat(String date){
        return date.matches("([0-9]{4})-([0-9]{2})-([0-9]{2})");
    }

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.ITALY);

        in_date.setText(sdf.format(myCalendar.getTime()));
    }

    private String addZero(int time_to_change){
        String res;
        String time_str = String.valueOf(time_to_change);
        if (time_str.length()==1){
            res = "0"+time_str;
        } else {
            res = time_str;
        }
        return res;
    }
}
