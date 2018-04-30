package com.damia.blackboxmed.Activities;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.damia.blackboxmed.Helper.AdapterData;
import com.damia.blackboxmed.Helper.AddDialogClass;
import com.damia.blackboxmed.Helper.DBMeasurements;
import com.damia.blackboxmed.Helper.DBSQLiteHelper;
import com.damia.blackboxmed.Helper.Measurement;
import com.damia.blackboxmed.R;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.crypto.exceptions.CryptoException;
import com.virgilsecurity.sdk.crypto.exceptions.EncryptionException;
import com.virgilsecurity.sdk.utils.Base64;
import com.virgilsecurity.sdk.utils.ConvertionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ActivityHome extends AppCompatActivity {

    //shared pref variables
    SharedPreferences session;
    String doctorUsername;
    String savedUsername;

    //request
    RequestQueue queue;
    StringRequest postRequest;

    //layout
    ImageButton btnRefresh;
    ImageButton btnSendData;
    ImageButton btnAdd;
    ImageButton btnSettings;
    ImageButton btnCloseRequest;
    ListView ml;
    AdapterData adapterData;
    RelativeLayout spinnerbg;

    //measurement data
    String type;
    String unit;
    String createdAt;
    int value;

    ArrayList<Measurement> measures = new ArrayList<>();
    ArrayList<String> convertedMeasures = new ArrayList<>();

    //virgil
    VirgilPublicKey pubKey;
    String savedPubKey;
    String token;
    String dataToSend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        queue = Volley.newRequestQueue(this);
        spinnerbg = findViewById(R.id.spinnerbg);

        adapterData = new AdapterData(measures, ActivityHome.this);

        ml = findViewById(R.id.measures_list);
        btnAdd = findViewById(R.id.btnAddMeasure);
        btnSettings = findViewById(R.id.btnSettings);
        btnSendData = findViewById(R.id.btnSendData);
        btnCloseRequest = findViewById(R.id.close_request);

        session = PreferenceManager.getDefaultSharedPreferences(ActivityHome.this);
        doctorUsername = session.getString("doctorUsernamePref", "");
        savedPubKey = "MCowBQYDK2VwAyEAt75fQ6Ji2Aq9VcdkeqS/2XppuXouSRPUd/Vj8R5T2yk=";//session.getString("pubKeyPref", "");  <---change to this
        savedUsername = session.getString("usernamePref", "");
        token = session.getString("tokenPref", "");

        //check if the user is logged in
        if (savedUsername.equals("")){
            Intent intentLogin = new Intent(ActivityHome.this, ActivityLogin.class);
            startActivity(intentLogin);
            finish();
        } else {
            displayData();
            adapterData.notifyDataSetChanged();
        }

        //go to settings
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSettings = new Intent(getApplicationContext(), ActivitySettings.class);
                startActivity(intentSettings);
            }
        });

        //add a measurement
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDialogClass cdd=new AddDialogClass(ActivityHome.this);
                cdd.show();
            }
        });

        //upload data
        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (savedPubKey.equals("")){
                    Toast.makeText(ActivityHome.this,
                            "You need to select a doctor first, go to the settings menu!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    spinnerbg.setVisibility(View.VISIBLE);
                    try {
                        pubKey = importPublicKey(savedPubKey);

                        dataToSend = encryptAndParseData(measures);
                        sendData();

                    } catch (CryptoException ce){
                        Toast.makeText(ActivityHome.this,
                                "Failed to set the public key", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //close request
        btnCloseRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerbg.setVisibility(View.GONE);
                Toast.makeText(ActivityHome.this, "Request canceled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //get the data from the db to the adapter
    public void displayData(){
        DBSQLiteHelper database = new DBSQLiteHelper(this);
        measures.clear();
        measures = database.findAllHandler();
        adapterData = new AdapterData(measures, ActivityHome.this);
        ml.setAdapter(adapterData);
        adapterData.notifyDataSetChanged();
        spinnerbg.setVisibility(View.GONE);
    }

    //send POST request
    public void sendData() {
        postRequest = new StringRequest(Request.Method.POST, getString(R.string.url_senddata),
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        spinnerbg.setVisibility(View.GONE);
                        Toast.makeText(ActivityHome.this,
                                "Data sent",
                                Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        spinnerbg.setVisibility(View.GONE);
                        Toast.makeText(ActivityHome.this,
                                error.toString(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
            @Override
            public byte[] getBody() {
                String str = dataToSend;
                return str.getBytes();
            }
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", token);
                return headers;
            }

            public String getBodyContentType()
            {
                return "application/json; charset=utf-8";
            }
        };
        queue.add(postRequest);

    }


    public String escapeJsonCharacter(String jsonToEncode){
        String encodedJson = jsonToEncode.replaceAll("\"", "\\"+"\"");
        return encodedJson;
    }

    //std encryption function
    private static byte[] encryptData (String messageToEncrypt, VirgilPublicKey receiverPublicKey)
            throws EncryptionException {

        VirgilCrypto crypto = new VirgilCrypto();
        byte[] dataToEncrypt = ConvertionUtils.toBytes(messageToEncrypt);
        return crypto.encrypt(dataToEncrypt, receiverPublicKey);
    }

    //get measures and structure the json to send
    public String encryptAndParseData (ArrayList<Measurement> measurements) {
        String start = "{ \"targetUsername\":\""+doctorUsername+"\",";
        String me_start = "\"encryptedData\":\"";
        String content = "";
        String full_content;

        //creates an array list of strings that contains the measurements data
        for(Measurement m : measurements){
            String objToString = m.toJSON();
            convertedMeasures.add(objToString);
        }
        //turn is into a string adding commas
        String string_convertedMeasures = TextUtils.join(", ", convertedMeasures);

        //encrypt, convert it to ISO-8859-1 and escape ' " ' character
        try {
            byte[] encryptedData = (encryptData(string_convertedMeasures, pubKey));
            String contentToEscape = new String(encryptedData, "ISO-8859-1");
            content = escapeJsonCharacter(contentToEscape);
        } catch (Exception e) {
            
            System.err.println(e);
            System.out.println("ERROR IN encryptAndParseData");
            
        }

        full_content = start+me_start+content+"\", \"comment\":\"\" }";
        System.out.println("Request body: "+full_content);
        return full_content;
    }

    private VirgilPublicKey importPublicKey(String publicKey) throws CryptoException {
        VirgilCrypto crypto = new VirgilCrypto();

        byte[] publicKeyData = ConvertionUtils.base64ToBytes(publicKey);
        return crypto.importPublicKey(publicKeyData);
    }

}
