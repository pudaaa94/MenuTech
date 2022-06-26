package com.menu.menutech;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.MainThread;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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

public class AfterPickingActivity extends AppCompatActivity {

    /* interface elements */
    ImageView venueImageView;
    Button logoutBtn;
    TextView venueNameTextView;
    TextView unknownTextView;
    TextView welcomeTextView;
    TextView descriptionTextView;
    TextView openHoursTextView;
    TextView imageNotProvidedTextView;
    LinearLayout connectionLostAfterPickingLayout;

    /* messaging from other threads handler */
    public Handler afterPickingHandler;

    /* misc */
    int back_allowed = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_after_picking);

        /* interface elements */
        venueImageView = findViewById(R.id.venueImageView);
        logoutBtn = findViewById(R.id.logoutBtn);
        venueNameTextView = findViewById(R.id.venueNameTextView);
        unknownTextView = findViewById(R.id.unknownTextView);
        welcomeTextView = findViewById(R.id.welcomeTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);
        openHoursTextView = findViewById(R.id.openHoursTextView);
        imageNotProvidedTextView = findViewById(R.id.imageNotProvidedTextView);
        connectionLostAfterPickingLayout = findViewById(R.id.connectionLostAfterPickingLayout);

        if (getIntent().hasExtra("image") &&
                getIntent().hasExtra("name") &&
                getIntent().hasExtra("unknown") &&
                getIntent().hasExtra("welcome") &&
                getIntent().hasExtra("description") &&
                getIntent().hasExtra("working_hours"))
        {
            if (getIntent().getStringExtra("name").isEmpty() || getIntent().getStringExtra("name").equals("null")){
                venueNameTextView.setText("Name not provided");
            } else{
                venueNameTextView.setText(getIntent().getStringExtra("name"));
            }

            unknownTextView.setText(getIntent().getStringExtra("unknown"));

            if (getIntent().getStringExtra("welcome").isEmpty() || getIntent().getStringExtra("welcome").equals("null")){
                welcomeTextView.setText("Welcome message not provided");
            } else{
                welcomeTextView.setText(getIntent().getStringExtra("welcome"));
            }

            if (getIntent().getStringExtra("description").isEmpty() || getIntent().getStringExtra("description").equals("null")){
                descriptionTextView.setText("Description message not provided");
            } else{
                descriptionTextView.setText(getIntent().getStringExtra("description"));
            }

            openHoursTextView.setText(getIntent().getStringExtra("working_hours"));

            if (!getIntent().getStringExtra("image").equals("N/A")){
                Glide.with(getApplicationContext())
                        .load(getIntent().getStringExtra("image"))
                        .centerCrop()
                        .into(venueImageView);

                imageNotProvidedTextView.setText(" ");
            }
        }

        /* accept messages from other threads */
        afterPickingHandler = new Handler(Looper.getMainLooper()){
            @UiThread
            @MainThread
            public void handleMessage(Message message) {
                switch (message.what) {

                    case 300: {
                        String content = message.obj.toString();
                        //Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
                        if (content.equals("OK NOK") || content.equals("NOK OK") || content.equals("OK OK")){
                            connectionLostAfterPickingLayout.setVisibility(View.GONE);
                            back_allowed = 1;
                        } else {
                            connectionLostAfterPickingLayout.setVisibility(View.VISIBLE);
                            back_allowed = 0;
                        }

                        break;
                    }
                }
            }
        };

        /* start network monitoring */
        ConnectivityCheckerThread connectivityCheckerThread = new ConnectivityCheckerThread(afterPickingHandler, 300, AfterPickingActivity.this);
        connectivityCheckerThread.start();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(getApplicationContext(), "Logging out..", Toast.LENGTH_LONG).show();
                SharedPreferences preferences = getSharedPreferences("MENU_TECH_APP", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                Intent myIntent = new Intent(AfterPickingActivity.this, MainActivity.class);
                AfterPickingActivity.this.startActivity(myIntent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (back_allowed == 1) {
            super.onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), "Please enable WiFi or mobile data to complete action", Toast.LENGTH_LONG).show();
        }
    }
}
