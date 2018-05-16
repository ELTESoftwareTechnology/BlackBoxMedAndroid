package com.damia.blackboxmed.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.damia.blackboxmed.Helper.DoctorsAdapter;
import com.damia.blackboxmed.Helper.Doctor;
import com.damia.blackboxmed.R;

import org.json.JSONArray;

import java.io.SyncFailedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    ImageButton btnHome;
    ImageButton btnLogout;

    ListView dl;
    ArrayList<Doctor> doctors;
    DoctorsAdapter adapterDoctors;

    CheckBox cb;
    TextView current_doc;
    String savedToken;
    String email;
    String firstName;
    String lastName;
    String pubKey;
    String username;

    EditText inputSearch;
    RelativeLayout spinnerbg;
    SharedPreferences session;
    SharedPreferences.Editor editor;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        session = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        savedToken = session.getString("tokenPref", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        queue = Volley.newRequestQueue(this);
        doctors = new ArrayList<>();

        adapterDoctors = new DoctorsAdapter(doctors, SettingsActivity.this);

        inputSearch = (EditText) findViewById(R.id.inputSearch);
        dl = findViewById(R.id.doctors_list);
        btnHome = findViewById(R.id.btnHome);
        btnLogout = findViewById(R.id.btnLogout);
        current_doc = findViewById(R.id.current_doctor);
        cb = findViewById(R.id.google_fit_checkbox);

        spinnerbg = (RelativeLayout)findViewById(R.id.spinnerbg);
        spinnerbg.setVisibility(View.VISIBLE);

        if("".equals(session.getString("doctorNamePref", ""))){
            current_doc.setText("No doctor selected, choose one!");
        } else {
            current_doc.setText(session.getString("doctorNamePref", ""));

        }

        String check = session.getString("fitPref", "");

        if(check.equals("0")){
            cb.setChecked(false);
        } else {
            cb.setChecked(true);
        }
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SharedPreferences.Editor editor = session.edit();
                if(cb.isChecked()){
                    editor.putString("fitPref", "1");
                } else {
                    editor.putString("fitPref", "0");
                }
                editor.apply();
                editor.commit();
            }
        });


        getDoctors();
        adapterDoctors.notifyDataSetChanged();

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSettings = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intentSettings);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                finish();

                                Intent intentLogout = new Intent(SettingsActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                intentLogout.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intentLogout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                finish();
                                startActivity(intentLogout);
                            }
                        }).create().show();
            }
        });


        dl.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ArrayList<Doctor> temp = adapterDoctors.getDocList();
                Doctor d = temp.get(position);

                SharedPreferences.Editor editor = session.edit();
                editor.putString("pubKeyPref", d.getPublicKey());
                editor.putString("doctorNamePref", d.getFirstName()+" "+d.getLastName());
                editor.putString("doctorUsernamePref", d.getUsername());
                editor.apply();
                editor.commit();
                current_doc.setText(" "+session.getString("doctorNamePref", ""));
                Toast.makeText(SettingsActivity.this, "Doctor changed, current: "+d.getFirstName()+" "+d.getLastName(), Toast.LENGTH_SHORT).show();

            }
        });

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if(cs.toString().equals("")){
                    doctors.clear();
                    adapterDoctors = new DoctorsAdapter(doctors, SettingsActivity.this);
                    getDoctors();
                    adapterDoctors.notifyDataSetChanged();
                } else {

                    SettingsActivity.this.adapterDoctors.getFilter().filter(cs);
                }

            }


            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });
    }

    public void getDoctors(){
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, getString(R.string.url_getdoctors), null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {;
                        spinnerbg.setVisibility(View.GONE);
                        try {

                            for(int i=0; i<response.length(); i++){
                                firstName = response.getJSONObject(i).getString("firstName");
                                lastName =  response.getJSONObject(i).getString("lastName");
                                email =  response.getJSONObject(i).getString("email");
                                pubKey = response.getJSONObject(i).getString("publicKey");
                                username = response.getJSONObject(i).getString("username");

                                Doctor d = new Doctor(email, username, pubKey, firstName, lastName);

                                doctors.add(d);
                            }

                            dl.setAdapter(adapterDoctors);
                        } catch (Exception e){
                            System.err.println(e);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        spinnerbg.setVisibility(View.GONE);
                        System.err.println(error);
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", savedToken);
                return headers;
            }
        };
        queue.add(getRequest);
    }
}
