package com.damia.blackboxmed.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.damia.blackboxmed.Helper.DBMeasurements;
import com.damia.blackboxmed.Helper.DBSQLiteHelper;
import com.damia.blackboxmed.Helper.User;
import com.damia.blackboxmed.R;

import org.json.JSONException;
import org.json.JSONObject;

public class ActivityLogin extends AppCompatActivity {


    SharedPreferences session;
    SharedPreferences.Editor editor;

    StringRequest postRequest;

    private ImageButton close_req;
    private Button btnLogin;
    private EditText inputUsername;                // Tv dove mi l'utente inserisce
    private EditText inputPassword;
    private TextView btnGoToRegistration;
    RequestQueue queue;
    String token = "";
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        queue = Volley.newRequestQueue(this);

        inputUsername = (EditText) findViewById(R.id.loginUsername);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.loginSendRequest);
        btnGoToRegistration = (TextView) findViewById(R.id.loginGoToReg);
        close_req=(ImageButton)findViewById(R.id.close_request);
        
        final RelativeLayout spinnerbg;
        spinnerbg = (RelativeLayout)findViewById(R.id.spinnerbg);
        spinnerbg.setVisibility(View.GONE);
        
        session = PreferenceManager.getDefaultSharedPreferences(ActivityLogin.this);
        final String savedUsername = session.getString("usernamePref", "");
        String savedToken = session.getString("tokenPref", "");

        btnGoToRegistration.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentSignUP = new Intent(getApplicationContext(), ActivityRegister.class);
                startActivity(intentSignUP);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // prendi nome e password dello User
                username = inputUsername.getText().toString();
                final String password = inputPassword.getText().toString();
                if("".equals(username) || "".equals(password)){
                    Toast.makeText(ActivityLogin.this, getString(R.string.no_fields), Toast.LENGTH_SHORT).show();
                } else {
                    spinnerbg.setVisibility(View.VISIBLE);
                    btnLogin.setClickable(false);

                    postRequest = new StringRequest(Request.Method.POST, getString(R.string.url_login),
                            new Response.Listener<String>()
                            {

                                @Override
                                public void onResponse(String response) {

                                        try {
                                           JSONObject jsonResponse = new JSONObject(response);
                                           token = jsonResponse.getString("token");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        session = PreferenceManager.getDefaultSharedPreferences(ActivityLogin.this);
                                        SharedPreferences.Editor editor = session.edit();
                                        editor.putString("usernamePref", username);
                                        editor.putString("tokenPref", token);
                                        editor.putString("doctorNamePref", "");
                                        editor.putString("doctorUsernamePref", "");
                                        editor.putString("pubKeyPref", "");
                                        editor.putString("firstOpen", "0");
                                        editor.apply();
                                        editor.commit();

                                        User user = new User(username, token);

                                        Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
                                        startActivity(intent);

                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    //String err = error.sub
                                    Toast.makeText(ActivityLogin.this, error.toString(), Toast.LENGTH_SHORT).show();
                                    System.out.println(error);
                                    spinnerbg.setVisibility(View.GONE);
                                    btnLogin.setClickable(true);
                                }
                            }
                    ) {
                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            return super.parseNetworkResponse(response);
                        }
                        @Override
                        public byte[] getBody() throws com.android.volley.AuthFailureError {
                            String str = "{\"username\":\""+username+"\",\"password\":\""+password+"\"}";
                            return str.getBytes();
                        }

                        public String getBodyContentType()
                        {
                            return "application/json; charset=utf-8";
                        }
                    };
                    queue.add(postRequest);


                }

            }
        });

        close_req.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                spinnerbg.setVisibility(View.GONE);
                btnLogin.setClickable(true);

                Toast.makeText(ActivityLogin.this, R.string.canceled_request, Toast.LENGTH_SHORT).show();
            }

        });
    }


}
