package com.damia.blackboxmed.Activities;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.damia.blackboxmed.Helper.DataAdapter;
import com.damia.blackboxmed.Helper.AddDialogClass;
import com.damia.blackboxmed.Helper.DatabaseHelper;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

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
    DataAdapter adapterData;
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

    AddDialogClass cdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        queue = Volley.newRequestQueue(this);
        spinnerbg = findViewById(R.id.spinnerbg);

        adapterData = new DataAdapter(measures, HomeActivity.this);

        ml = findViewById(R.id.measures_list);
        btnAdd = findViewById(R.id.btnAddMeasure);
        btnSettings = findViewById(R.id.btnSettings);
        btnSendData = findViewById(R.id.btnSendData);
        btnCloseRequest = findViewById(R.id.close_request);

        session = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this);
        doctorUsername = session.getString("doctorUsernamePref", "");
        savedPubKey = session.getString("pubKeyPref", "");
        savedUsername = session.getString("usernamePref", "");
        token = session.getString("tokenPref", "");

        //check if the user is logged in
        if (savedUsername.equals("")){
            Intent intentLogin = new Intent(HomeActivity.this, LoginActivity.class);
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
                Intent intentSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intentSettings);
            }
        });

        //add a measurement
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cdd =new AddDialogClass(HomeActivity.this);
                cdd.show();

                cdd.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        displayData();
                        adapterData.notifyDataSetChanged();
                    }
                });

            }
        });



        //upload data
        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (savedPubKey.equals("")){
                    Toast.makeText(HomeActivity.this,
                            "You need to select a doctor first, go to the settings menu!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    spinnerbg.setVisibility(View.VISIBLE);
                    try {
                        pubKey = importPublicKey(savedPubKey);

                        dataToSend = encryptAndParseData(measures);
                        sendData();

                    } catch (CryptoException ce){
                        Toast.makeText(HomeActivity.this,
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
                Toast.makeText(HomeActivity.this, "Request canceled", Toast.LENGTH_SHORT).show();
            }
        });

    }

    //get the data from the db to the adapter
    public void displayData(){
        DatabaseHelper db = new DatabaseHelper(this);
        measures.clear();
        measures = db.getAllMeasurementsByUser(savedUsername);

        adapterData = new DataAdapter(measures, HomeActivity.this);
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
                        Toast.makeText(HomeActivity.this,
                                "Data sent",
                                Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        spinnerbg.setVisibility(View.GONE);
                        Toast.makeText(HomeActivity.this,
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


    //std encryption function
    private static byte[] encryptData (String messageToEncrypt, VirgilPublicKey receiverPublicKey)
            throws EncryptionException {

        VirgilCrypto crypto = new VirgilCrypto();
        byte[] dataToEncrypt = ConvertionUtils.toBytes(messageToEncrypt);
        return crypto.encrypt(dataToEncrypt, receiverPublicKey);
    }

    //get measures and structure the json to send
    public String encryptAndParseData (ArrayList<Measurement> measurements) {
        //creates an array list of strings that contains the measurements data
        JSONObject bundledJson = new JSONObject();
        JSONArray measurementsJson = new JSONArray();
        for(Measurement m : measurements){
            measurementsJson.put(m.toJSON());
        }

        try {
            bundledJson.put("measurements", measurementsJson);
            String dataToEncrypt = bundledJson.toString();
            byte[] encryptedData = (encryptData(dataToEncrypt, pubKey));
            String encryptedDataString = Base64.encode(encryptedData);

            JSONObject body = new JSONObject();
            body.put("targetUsername", doctorUsername);
            body.put("encryptedData", encryptedDataString);
            body.put("comment", "");
            return body.toString();
        } catch (Exception e) {
            System.err.println(e);
            System.out.println("ERROR IN encryptAndParseData");
            return "";
        }
    }

    private VirgilPublicKey importPublicKey(String publicKey) throws CryptoException {
        VirgilCrypto crypto = new VirgilCrypto();

        byte[] publicKeyData = ConvertionUtils.base64ToBytes(publicKey);
        return crypto.importPublicKey(publicKeyData);
    }

}
