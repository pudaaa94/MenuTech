package com.menu.menutech;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static androidx.core.content.ContextCompat.getSystemService;

public class ConnectivityCheckerThread extends Thread {

    public static int CHECKER_ID = 300;
    public Context context;
    public Handler handler;
    public int message_id;

    Timer timer;

    ConnectivityCheckerThread(Handler han, int mess_id, Context cnt) {
        handler = han;
        message_id = mess_id;
        context = cnt;
    }

    public void run() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                TimerMethod();
            }
        }, 0, 1000);
    }

    private void TimerMethod() {
        handler.sendMessage(Message.obtain(handler, message_id, LookForWifiOnAndConnected() + " " + MobileDataCheck()));
    }

    private String LookForWifiOnAndConnected() {
        WifiManager wifi_m = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        /*
        if (wifi_m.isWifiEnabled()) { // if user opened wifi

            WifiInfo wifi_i = wifi_m.getConnectionInfo();

            if (wifi_i.getNetworkId() == -1) {
                return "Not connected to any wifi device";
            }
            return "Connected to some wifi device";
        } else {
            return "user turned off wifi";
        }
        */

        if (wifi_m.isWifiEnabled()) {
            return "OK";
        } else{
            return "NOK";
        }

    }

    private String MobileDataCheck(){
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }

        if (mobileDataEnabled){
            return "OK";
        } else{
            return "NOK";
        }
    }
}
