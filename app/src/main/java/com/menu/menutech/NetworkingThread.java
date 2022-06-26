package com.menu.menutech;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkingThread extends Thread {

    public static int VENUE_RECEIVER_MESSAGE_ID = 100;
    public static int LOGIN_MESSAGE_ID = 200;
    public Handler handler;
    public int message_id;
    Request request;

    NetworkingThread(Request req, Handler han, int mess_id){
        request = req;
        handler = han;
        message_id = mess_id;
    }

    public void run() {
        OkHttpClient client = new OkHttpClient();
        Response response = null;
        try {
            handler.sendMessage(Message.obtain(handler, message_id, "before execution req"));
            response = client.newCall(request).execute();
            handler.sendMessage(Message.obtain(handler, message_id, "after execution req - success"));
        } catch (IOException e) {
            //handler.sendMessage(Message.obtain(handler, message_id, "after execution req - fail"));
            e.printStackTrace();
        }
        if (response.isSuccessful()) {
            handler.sendMessage(Message.obtain(handler, message_id, "resp received"));
            String responseBody = null;
            try {
                handler.sendMessage(Message.obtain(handler, message_id, "resp received - before extraction"));
                responseBody = response.body().string();
                handler.sendMessage(Message.obtain(handler, message_id, "resp received - after extraction"));
                if (responseBody != null && !responseBody.isEmpty() && !responseBody.equals("null")){
                    handler.sendMessage(Message.obtain(handler, message_id, "forwarding after extraction"));
                    handler.sendMessage(Message.obtain(handler, message_id, responseBody));
                    handler.sendMessage(Message.obtain(handler, message_id, String.valueOf(responseBody.length())));
                } else{
                    handler.sendMessage(Message.obtain(handler, message_id, "empty after extraction"));
                }
            } catch (IOException e) {
                //handler.sendMessage(Message.obtain(handler, message_id, "extraction failed"));
                e.printStackTrace();
            }

            //Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_LONG).show();
        } else{
            //Toast.makeText(getApplicationContext(), "Fail", Toast.LENGTH_LONG).show();
            handler.sendMessage(Message.obtain(handler, message_id, "fail"));
        }
    }
}
