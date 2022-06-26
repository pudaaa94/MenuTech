package com.menu.menutech;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.lang.Boolean.FALSE;

public class MainActivity extends AppCompatActivity {

    /* misc */
    public Handler loginHandler;
    String userName;
    String password;
    int connectionOk = 0;

    /* progress indication */
    LoadingDialog loadingDialog;
    AlphaAnimation loadingAnimation;

    /* interface */
    EditText userNameEditText;
    EditText passwordEditText;
    Button signInButton;
    LinearLayout connectionLostLayout;
    LinearLayout mainLayout;

    /* target URL */
    public static final String loginUrl = "https://api-playground.menu.app/api/customers/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* interface elements */
        mainLayout = findViewById(R.id.mainLayout);
        userNameEditText = findViewById(R.id.userNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signInButton = findViewById(R.id.signInButton);
        connectionLostLayout = findViewById(R.id.connectionLostLayout);

        /* progress indication */
        loadingAnimation = new AlphaAnimation(1F, 0.8F);
        loadingDialog = new LoadingDialog(MainActivity.this);

        /* check if token exists */
        SharedPreferences preferences = MainActivity.this.getSharedPreferences("MENU_TECH_APP",Context.MODE_PRIVATE);
        String retrivedToken = preferences.getString("TOKEN","null");
        if (!retrivedToken.equals("null")){
            mainLayout.setVisibility(View.GONE);
            Intent myIntent = new Intent(MainActivity.this, AfterLoginActivity.class);
            MainActivity.this.startActivity(myIntent);
        }

        /* accept messages from other threads */
        loginHandler = new Handler(Looper.getMainLooper()){
            @UiThread
            @MainThread
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 200: {

                        loadingDialog.dismissDialog();
                        String content = message.obj.toString();

                        /* check if login was successful */
                        if (content.equals("fail")){
                            Toast.makeText(getApplicationContext(), "Wrong credentials. Please try again", Toast.LENGTH_LONG).show();
                        } else{
                            if (content.length() < 40){
                                //Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
                            } else{
                                //Toast.makeText(getApplicationContext(), "processing content..", Toast.LENGTH_LONG).show();

                                JSONObject obj = null;
                                try {
                                    obj = new JSONObject(content);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                /* obj contains status, code and data */

                                /*
                                if (obj != null){
                                    JSONArray keys = obj.names();
                                    for (int j = 0; j < keys.length(); j++){
                                        try {
                                            Toast.makeText(getApplicationContext(), "obj parsed: " + keys.getString(j), Toast.LENGTH_LONG).show();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else{
                                    Toast.makeText(getApplicationContext(), "resp null", Toast.LENGTH_LONG).show();
                                }
                                */

                                /* data contains token JSONObject, which contains token string */

                                /* login was successful, extract token, save it and show all venues */
                                JSONObject obj_data = null;
                                try {
                                    /* extraction */
                                    obj_data = obj.getJSONObject("data");
                                    JSONObject obj_token = obj_data.getJSONObject("token");
                                    String token = obj_token.getString("value");
                                    //Toast.makeText(getApplicationContext(), "Token is: " + token, Toast.LENGTH_LONG).show();

                                    /* saving token */
                                    SharedPreferences preferences = MainActivity.this.getSharedPreferences("MENU_TECH_APP", Context.MODE_PRIVATE);
                                    preferences.edit().putString("TOKEN", token).apply();

                                    /* initiate showing of all venues */
                                    Intent myIntent = new Intent(MainActivity.this, AfterLoginActivity.class);
                                    MainActivity.this.startActivity(myIntent);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        break;
                    }

                    case 300: {
                        String content = message.obj.toString();
                        //Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
                        if (content.equals("OK NOK") || content.equals("NOK OK") || content.equals("OK OK")){
                            connectionLostLayout.setVisibility(View.GONE);
                            connectionOk = 1;

                        } else {
                            connectionLostLayout.setVisibility(View.VISIBLE);
                            connectionOk = 0;
                        }

                        break;
                    }
                }
            }
        };

        /* start network monitoring */
        ConnectivityCheckerThread connectivityCheckerThread = new ConnectivityCheckerThread(loginHandler, 300, MainActivity.this);
        connectivityCheckerThread.start();

        /* setting button listener */
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (connectionOk == 1){
                    userName = userNameEditText.getText().toString();
                    if (userName != null && !userName.isEmpty() && !userName.equals("null")){
                        password = passwordEditText.getText().toString();
                        if (password != null && !password.isEmpty() && !password.equals("null")){
                            JSONObject jsonBody = new JSONObject();
                            try {
                                jsonBody.put("email", userName);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                jsonBody.put("password", password);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
                            Request request = new Request.Builder()
                                    .url(loginUrl)
                                    .post(body)
                                    .addHeader("application", "mobile-application")
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("Device-UUID", "123456")
                                    .addHeader("Api-Version", "3.7.0")
                                    .build();

                            view.startAnimation(loadingAnimation);
                            loadingDialog.startLoadingDialog();

                            NetworkingThread networkingThread = new NetworkingThread(request, loginHandler, 200);
                            networkingThread.start();
                        } else{
                            Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter user name", Toast.LENGTH_LONG).show();
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please enable WiFi or mobile data first", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Closing...", Toast.LENGTH_LONG).show();
        finishAffinity();
        finish();
    }
}
