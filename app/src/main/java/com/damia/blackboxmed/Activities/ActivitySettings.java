package com.damia.blackboxmed.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.damia.blackboxmed.Helper.AdapterDoctors;
import com.damia.blackboxmed.Helper.Doctor;
import com.damia.blackboxmed.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivitySettings extends AppCompatActivity {

    ImageButton btnHome;
    ImageButton btnLogout;

    ListView dl;
    ArrayList<Doctor> doctors;
    private static AdapterDoctors adapterDoctors;

    TextView current_doc;
    String savedToken;
    String email;
    String firstName;
    String lastName;
    String pubKey;
    String username;

    SharedPreferences session;
    SharedPreferences.Editor editor;

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        session = PreferenceManager.getDefaultSharedPreferences(ActivitySettings.this);
        savedToken = session.getString("tokenPref", "");

        queue = Volley.newRequestQueue(this);
        doctors = new ArrayList<>();
        adapterDoctors = new AdapterDoctors(doctors, ActivitySettings.this);

        dl = findViewById(R.id.doctors_list);
        btnHome = findViewById(R.id.btnHome);
        btnLogout = findViewById(R.id.btnLogout);
        current_doc = findViewById(R.id.current_doctor);

        if("".equals(session.getString("doctorNamePref", ""))){
            current_doc.setText("No doctor selected, choose one!");
        } else {
            current_doc.setText(getString(R.string.choose_doctor)+session.getString("doctorNamePref", ""));
        }

        getDoctors();
        adapterDoctors.notifyDataSetChanged();

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSettings = new Intent(getApplicationContext(), ActivityHome.class);
                startActivity(intentSettings);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(ActivitySettings.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure?")
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface arg0, int arg1) {
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ActivitySettings.this);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.clear();
                                editor.commit();
                                finish();

                                Intent intentLogout = new Intent(ActivitySettings.this, ActivityLogin.class)
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
                Doctor d = doctors.get(position);


                SharedPreferences.Editor editor = session.edit();
                editor.putString("pubKeyPref", d.getPublicKey());
                editor.putString("doctorNamePref", d.getFirstName()+" "+d.getLastName());
                editor.putString("doctorUsernamePref", d.getUsername());
                editor.apply();
                editor.commit();
                current_doc.setText(getString(R.string.choose_doctor)+" "+session.getString("doctorNamePref", ""));
                Toast.makeText(ActivitySettings.this, "Doctor changed, current: "+d.getFirstName()+" "+d.getLastName(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void getDoctors(){
        JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, getString(R.string.url_getdoctors), null,
                new Response.Listener<JSONArray>()
                {
                    @Override
                    public void onResponse(JSONArray response) {
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
