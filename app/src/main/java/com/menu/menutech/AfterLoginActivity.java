package com.menu.menutech;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class AfterLoginActivity extends AppCompatActivity {

    /* misc */
    int requestSent = 0;

    /* messaging from other threads handler */
    public Handler venueReceiverHandler;

    /* containers for extracted data */
    ArrayList<String> names = new ArrayList<String>();
    ArrayList<String> countries = new ArrayList<String>();
    ArrayList<String> descriptions = new ArrayList<String>();
    ArrayList<String> welcome_messages = new ArrayList<String>();
    ArrayList<String> states = new ArrayList<String>();
    ArrayList<String> is_open = new ArrayList<String>();
    ArrayList<String> distances = new ArrayList<String>();
    ArrayList<String> unknown_key = new ArrayList<String>();
    ArrayList<String> addresses_plus_cities = new ArrayList<String>();
    ArrayList<String> images = new ArrayList<String>();

    /* progress indication */
    Button dummyBtn;
    LoadingDialog loadingDialog;
    AlphaAnimation loadingAnimation;

    /* interface elements */
    MyAdapter myAdapter = null;
    LinearLayout venuesListLayout;
    RecyclerView venuesListRecyclerView;
    LinearLayout connectivityLostLayout;

    /* target URL */
    public static final String searchUrl = "https://api-playground.menu.app/api/directory/search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_login);

        /* interface elements */
        venuesListLayout = findViewById(R.id.venuesListLayout);
        venuesListRecyclerView = findViewById(R.id.venuesListRecyclerView);
        connectivityLostLayout = findViewById(R.id.connectivityLostLayout);

        /* progress indication */
        loadingAnimation = new AlphaAnimation(1F, 0.8F);
        loadingDialog = new LoadingDialog(AfterLoginActivity.this);
        dummyBtn = findViewById(R.id.dummyBtn);
        dummyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(loadingAnimation);
                loadingDialog.startLoadingDialog();
            }
        });

        /* accept messages from other threads */
        venueReceiverHandler = new Handler(Looper.getMainLooper()){
            @UiThread
            @MainThread
            public void handleMessage(Message message) {
                switch (message.what) {
                    case 100: {

                        String content = message.obj.toString();

                        if (content.length() < 40){
                            //Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
                        } else{
                            //Toast.makeText(getApplicationContext(), "processing content..", Toast.LENGTH_LONG).show();
                            loadingDialog.dismissDialog();

                            JSONObject obj = null;
                            try {
                                obj = new JSONObject(content);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JSONObject obj_data = null;
                            try {
                                obj_data = (JSONObject) obj.get("data");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            JSONArray obj_venues_and_distances = null;
                            try {
                                obj_venues_and_distances = obj_data.getJSONArray("venues");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            // venue_el consists of distance, distance_in_miles, venue
                            //Toast.makeText(getApplicationContext(), String.valueOf(obj_venues_and_distances.length()), Toast.LENGTH_LONG).show();
                            int is_open_call_counter = 0;
                            for (int i = 0; i < obj_venues_and_distances.length(); i++){
                                try {
                                    JSONObject venue_plus_distances = obj_venues_and_distances.getJSONObject(i);
                                    unknown_key.add("unknown");
                                    String distance_formatted = convertToProperRepresentation(venue_plus_distances.getString("distance"));
                                    distances.add(distance_formatted);
                                    JSONObject venue = venue_plus_distances.getJSONObject("venue");
                                    names.add(venue.getString("name"));
                                    countries.add(venue.getString("country"));
                                    descriptions.add(venue.getString("description"));
                                    welcome_messages.add(venue.getString("welcome_message"));
                                    //Toast.makeText(getApplicationContext(), welcome_messages.get(i), Toast.LENGTH_LONG).show();
                                    //JSONObject image_obj = venue.getJSONObject("image"); /* try catch or null checking must be done */
                                    String image_str = venue.getString("image");
                                    if (image_str.equals("null")){
                                        images.add("N/A");
                                    } else{
                                        JSONObject image_obj = venue.getJSONObject("image");
                                        images.add(image_obj.getString("thumbnail_medium"));
                                    }
                                    states.add(venue.getString("state"));
                                    String s = venue.getString("address") + ", " + venue.getString("city");
                                    if (s.equals(", ")){
                                        addresses_plus_cities.add("address and city not provided");
                                    } else{
                                        addresses_plus_cities.add(venue.getString("address") + ", " + venue.getString("city"));
                                    }

                                    if (venue.getString("is_open").equals("false")){
                                        is_open.add("Closed");
                                    } else{
                                        String serving_times = venue.getString("serving_times");
                                        if(serving_times.equals("[]")){
                                            is_open.add("Open (working hours not provided)");
                                        } else{
                                            Calendar calendar = Calendar.getInstance();
                                            int currentDayInt = calendar.get(Calendar.DAY_OF_WEEK);
                                            //int currentDayInt = Calendar.MONDAY;
                                            String currentDay = "";
                                            switch (currentDayInt){
                                                case Calendar.MONDAY:
                                                case Calendar.TUESDAY:
                                                case Calendar.WEDNESDAY:
                                                case Calendar.THURSDAY:
                                                case Calendar.FRIDAY:
                                                    currentDay = "WeekDays";
                                                    break;
                                                case Calendar.SATURDAY:
                                                case Calendar.SUNDAY:
                                                    currentDay = "WeekendDays";
                                                    break;
                                            }
                                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                            String currentTime_str = sdf.format(new Date());
                                            //String currentTime_str = "06:00";
                                            Date currentTime = sdf.parse(currentTime_str);
                                            JSONArray serving_times_arr = venue.getJSONArray("serving_times");
                                            JSONObject serving_times_obj = serving_times_arr.getJSONObject(serving_times_arr.length()-1); // only one object in array
                                            String working_days = serving_times_obj.getString("reference_type");
                                            String time_from_str = serving_times_obj.getString("time_from");
                                            String time_to_str = serving_times_obj.getString("time_to");
                                            if (time_from_str.equals("null")){
                                                time_from_str = "00:00";
                                            }
                                            if (time_to_str.equals("null")){
                                                time_to_str = "00:00";
                                            }
                                            Date time_from = sdf.parse(time_from_str);
                                            Date time_to = sdf.parse(time_to_str);

                                            if(working_days.equals("WeekDays") && currentDay.equals("WeekendDays")){
                                                is_open.add("Opens at Monday at " + time_from_str);
                                            } else{
                                                /* working_days different than WeekDays not provided currently */
                                                if (time_from_str.equals("00:00") && time_to_str.equals("00:00")){
                                                    is_open.add("Today open 24");
                                                } else{
                                                    if (currentTime.after(time_to)){
                                                        is_open.add("Opens next weekday at " + time_from_str);
                                                    } else {
                                                        if (currentTime.before(time_from)){
                                                            is_open.add("Opens at " + time_from_str);
                                                        } else {
                                                            is_open.add("Today " + time_from_str + " - " + time_to_str);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (JSONException | ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                            myAdapter = new MyAdapter(AfterLoginActivity.this, obj_venues_and_distances.length(), unknown_key, distances, addresses_plus_cities, is_open, images, names, welcome_messages, descriptions);
                            venuesListRecyclerView.setAdapter(myAdapter);
                            venuesListRecyclerView.setLayoutManager(new LinearLayoutManager(AfterLoginActivity.this));
                            venuesListLayout.setVisibility(View.VISIBLE);
                        }

                        break;
                    }

                    case 300: {
                        String content = message.obj.toString();
                        //Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
                        if (content.equals("OK NOK") || content.equals("NOK OK") || content.equals("OK OK")){
                            connectivityLostLayout.setVisibility(View.GONE);
                            if (requestSent == 0){ // because thread shouldn't be started multiple times, only once
                                sendRequest();
                            }
                        } else {
                            connectivityLostLayout.setVisibility(View.VISIBLE);
                        }

                        break;
                    }
                }
            }
        };

        /* start network monitoring */
        ConnectivityCheckerThread connectivityCheckerThread = new ConnectivityCheckerThread(venueReceiverHandler, 300, AfterLoginActivity.this);
        connectivityCheckerThread.start();
    }

    private void sendRequest(){

        requestSent = 1;

        dummyBtn.performClick();

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("latitude", "44.001783");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            jsonBody.put("longitude", "21.26907");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(searchUrl)
                .post(body)
                .addHeader("application", "mobile-application")
                .addHeader("Content-Type", "application/json")
                .addHeader("Device-UUID", "123456")
                .addHeader("Api-Version", "3.7.0")
                .build();

        NetworkingThread networkingThread = new NetworkingThread(request, venueReceiverHandler, 100);
        networkingThread.start();
    }

    private String convertToProperRepresentation(String s){
        double d = Double.parseDouble(s);
        d = d / 1000;
        String retVal = String.format("%.1f", d);
        retVal = retVal + " km";
        return retVal;
    }

    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Closing...", Toast.LENGTH_LONG).show();
        finishAffinity();
        finish();
    }
}
