package com.damia.blackboxmed.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.damia.blackboxmed.Helper.User;
import com.damia.blackboxmed.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    StringRequest postRequest;

    private ImageButton close_req;
    private Button btnRegister;
    private EditText inputUsername;
    private EditText inputPassword;
    private EditText inputName;
    private EditText inputSurname;
    private EditText inputEmail;
    private TextView btnGoToLogin;

    RequestQueue queue;
    String token;

    SharedPreferences session;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        queue = Volley.newRequestQueue(this);

        inputUsername = (EditText) findViewById(R.id.regUsername);
        inputPassword = (EditText) findViewById(R.id.regPassword);
        inputEmail = (EditText) findViewById(R.id.regEmail);
        inputName = (EditText) findViewById(R.id.regName);
        inputSurname = (EditText) findViewById(R.id.regSurname);

        btnRegister = (Button) findViewById(R.id.regSendRequest);
        btnGoToLogin = (TextView) findViewById(R.id.regGoToLogin);

        close_req=(ImageButton)findViewById(R.id.close_request);

        final RelativeLayout spinnerbg;
        spinnerbg = (RelativeLayout)findViewById(R.id.spinnerbg);
        spinnerbg.setVisibility(View.GONE);

        btnGoToLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentSignUP = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intentSignUP);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                final String username = inputUsername.getText().toString();
                final String name = inputName.getText().toString();
                final String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();
                final String surname = inputSurname.getText().toString();

                if(email.equals("")||password.equals("")||name.equals("")
                        ||surname.equals("")||username.equals(""))
                {
                    Toast.makeText(RegisterActivity.this, R.string.no_fields, Toast.LENGTH_LONG).show();
                    return;
                } else if(isEmailValid(email)) {
                    Toast.makeText(RegisterActivity.this,"Email not valid", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    spinnerbg.setVisibility(View.VISIBLE);
                    btnRegister.setClickable(false);

                    postRequest = new StringRequest(Request.Method.POST, getString(R.string.url_register),
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
                                    session = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                                    SharedPreferences.Editor editor = session.edit();
                                    editor.putString("usernamePref", username);
                                    editor.putString("tokenPref", token);
                                    editor.apply();
                                    editor.commit();

                                    User user = new User(username, token);

                                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                    startActivity(intent);

                                }
                            },
                            new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(RegisterActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                                    System.out.println(error);
                                    spinnerbg.setVisibility(View.GONE);
                                    btnRegister.setClickable(true);
                                }
                            }
                    ) {
                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            return super.parseNetworkResponse(response);
                        }
                        @Override
                        public byte[] getBody() throws com.android.volley.AuthFailureError {
                            String str = "{\"username\":\""+username+"\",\"email\":\""+email+"\",\"password\":\""+
                                    password+"\",\"firstName\":\""+name+"\",\"lastName\":\""+surname+"\",\"roleType\":\"ADMIN\" }";
                            System.out.println(str);
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
                btnRegister.setClickable(true);

                Toast.makeText(RegisterActivity.this, R.string.canceled_request, Toast.LENGTH_SHORT).show();
            }

        });
    }

    public static boolean isEmailValid(CharSequence email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
