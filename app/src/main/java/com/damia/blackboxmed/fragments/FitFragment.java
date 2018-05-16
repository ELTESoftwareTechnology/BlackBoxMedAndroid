package com.damia.blackboxmed.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.damia.blackboxmed.Helper.FitDataAdapter;
import com.damia.blackboxmed.Helper.Measurement;
import com.damia.blackboxmed.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.virgilsecurity.sdk.crypto.VirgilCrypto;
import com.virgilsecurity.sdk.crypto.VirgilPublicKey;
import com.virgilsecurity.sdk.crypto.exceptions.CryptoException;
import com.virgilsecurity.sdk.crypto.exceptions.EncryptionException;
import com.virgilsecurity.sdk.utils.Base64;
import com.virgilsecurity.sdk.utils.ConvertionUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getDateInstance;

public class FitFragment extends Fragment {

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;

    View view;

    SharedPreferences session;
    String doctorUsername;
    String savedUsername;


    //request
    RequestQueue queue;
    StringRequest postRequest;

    RelativeLayout spinnerbg;
    ListView ml;
    FitDataAdapter adapterData;
    EditText inputSearch;

    VirgilPublicKey pubKey;
    String savedPubKey;
    String token;
    String dataToSend;

    ImageButton btnSendData;
    ArrayList<Measurement> measures = new ArrayList<>();


    public FitFragment() {
        // Required empty public constructor
    }

    public static FitFragment newInstance() {
        FitFragment fragment = new FitFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fit, container, false);

        queue = Volley.newRequestQueue(getContext());

        btnSendData = view.findViewById(R.id.fab_upload);
        spinnerbg = view.findViewById(R.id.spinnerbg);
        inputSearch = (EditText) getActivity().findViewById(R.id.inputSearch);

        session = PreferenceManager.getDefaultSharedPreferences(getContext());
        doctorUsername = session.getString("doctorUsernamePref", "");
        savedPubKey = session.getString("pubKeyPref", "");
        savedUsername = session.getString("usernamePref", "");
        token = session.getString("tokenPref", "");

        measures.clear();

        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_READ)
                        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(getContext()),
                    fitnessOptions);
        } else {
            System.out.println("Caling readHistoryData");
            readHistoryOtherData();
            readHistoryStepsData();
        }

        ml = view.findViewById(R.id.measures_list);

        adapterData = new FitDataAdapter(measures, getContext());
        adapterData.notifyDataSetChanged();

        //upload data
        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (savedPubKey.equals("")){
                    Toast.makeText(getContext(),
                            "You need to select a doctor first, go to the settings menu!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    spinnerbg.setVisibility(View.VISIBLE);
                    try {
                        pubKey = importPublicKey(savedPubKey);

                        dataToSend = encryptAndParseData(measures);
                        sendData();

                    } catch (CryptoException ce){
                        Toast.makeText(getContext(),
                                "Failed to set the public key", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if(cs.toString().equals("")){
                    measures.clear();
                    adapterData = new FitDataAdapter(measures, getContext());
                    readHistoryOtherData();
                    readHistoryStepsData();
                    adapterData.notifyDataSetChanged();
                } else {

                    adapterData.getFilter().filter(cs);
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

        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {

                new Continuation<Void, Task<DataReadResponse>>() {
                    @Override
                    public Task<DataReadResponse> then(@NonNull Task<Void> task) throws Exception {
                        return readHistoryOtherData();
                    }
                };

                new Continuation<Void, Task<DataReadResponse>>() {
                    @Override
                    public Task<DataReadResponse> then(@NonNull Task<Void> task) throws Exception {
                        return readHistoryStepsData();
                    }
                };

            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private Task<DataReadResponse> readHistoryStepsData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryFitnessStepsData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {

                                    System.out.println("Reading buckets");
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {

                                            printData(dataSet);
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {

                                    System.out.println("Reading dataSets");
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        printData(dataSet);
                                    }
                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("There was a problem reading the data.");
                                Log.e("Google Fit" , "There was a problem reading the data.", e);

                            }
                        });
    }

    private Task<DataReadResponse> readHistoryOtherData() {
        // Begin by creating the query.
        DataReadRequest readRequest = queryFitnessOtherData();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                if (dataReadResponse.getBuckets().size() > 0) {

                                    System.out.println("Reading buckets");
                                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                                        List<DataSet> dataSets = bucket.getDataSets();
                                        for (DataSet dataSet : dataSets) {

                                            printData(dataSet);
                                        }
                                    }
                                } else if (dataReadResponse.getDataSets().size() > 0) {

                                    System.out.println("Reading dataSets");
                                    for (DataSet dataSet : dataReadResponse.getDataSets()) {
                                        printData(dataSet);
                                    }
                                }

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("There was a problem reading the data.");
                                Log.e("Google Fit" , "There was a problem reading the data.", e);

                            }
                        });
    }

    public static DataReadRequest queryFitnessStepsData() {

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i("Google Fit", "Range Start: " + dateFormat.format(startTime));
        Log.i("Google Fit", "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        //
                        .read(DataType.TYPE_WEIGHT)
                        .read(DataType.TYPE_HEART_RATE_BPM)
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();

        return readRequest;
    }

    public static DataReadRequest queryFitnessOtherData() {

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = getDateInstance();
        Log.i("Google Fit", "Range Start: " + dateFormat.format(startTime));
        Log.i("Google Fit", "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest =
                new DataReadRequest.Builder()
                        //
                        .read(DataType.TYPE_WEIGHT)
                        .read(DataType.TYPE_HEART_RATE_BPM)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();

        return readRequest;
    }


    private void printData(DataSet dataSet){
        DateFormat dateFormat = getDateInstance();
        adapterData = new FitDataAdapter(measures, getContext());
        ml.setAdapter(adapterData);
        String unit ="";
        String type ="";
        String date="";
        int value=0;
        String img_res="";


        for (DataPoint dp : dataSet.getDataPoints()) {
            date =  dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));

            if(dp.getDataType().getName().equals("com.google.step_count.delta")) {
                type = "Distance";
                img_res = "drawable/steps";
                unit = "Steps";
            } else if (dp.getDataType().getName().equals("com.google.weight")) {
                type = "Weight";
                img_res = "drawable/scale";
                unit = "Kg";
            } else if (dp.getDataType().getName().equals("com.google.heart_rate.bpm")) {
                type = "Heartrate";
                img_res = "drawable/heart";
                unit = "bpm";
            } else {
                type = dp.getDataType().getName();
                img_res = "drawable/info";
            }
            for (Field field : dp.getDataType().getFields()) {
                if(unit.equals("")){
                    unit = field.getName();
                }
                float value_f = Float.parseFloat(dp.getValue(field).toString());
                value = Math.round(value_f);
            }
        }

        Measurement m = new Measurement(type, unit, value, date, img_res, 0);

        System.out.println(m.toJSON());
        measures.add(m);
        Collections.sort(measures);
        adapterData.notifyDataSetChanged();
    }

    //send POST request
    public void sendData() {
        postRequest = new StringRequest(Request.Method.POST, getString(R.string.url_senddata),
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        spinnerbg.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
                                "Data sent",
                                Toast.LENGTH_LONG).show();


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        spinnerbg.setVisibility(View.GONE);
                        Toast.makeText(getContext(),
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
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
