package com.menu.menutech;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> unknown;
    ArrayList<String> distances;
    ArrayList<String> addresses;
    ArrayList<String> is_open;
    ArrayList<String> images;
    ArrayList<String> names;
    ArrayList<String> welcome_messages;
    ArrayList<String> descriptions;
    int num_of_elements;

    public MyAdapter(Context ct, int num_of_el, ArrayList<String> unk, ArrayList<String> dstncs, ArrayList<String> addrss, ArrayList<String> is_opn, ArrayList<String> imgs, ArrayList<String> nms, ArrayList<String> wlc_msgs, ArrayList<String> descs){
        context = ct;
        num_of_elements = num_of_el;
        unknown = unk;
        distances = dstncs;
        addresses = addrss;
        is_open = is_opn;
        images = imgs;
        names = nms;
        welcome_messages = wlc_msgs;
        descriptions = descs;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.venue_item_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        holder.stateTextView.setText(unknown.get(position));
        holder.distanceTextView.setText(distances.get(position));
        holder.addressTextView.setText(addresses.get(position));
        holder.isOpenTextView.setText(is_open.get(position));
        if (is_open.get(position).equals("Closed")){
            holder.stateTextView.setTextColor(Color.parseColor("#b0b0b0"));
            holder.distanceTextView.setTextColor(Color.parseColor("#b0b0b0"));
            holder.addressTextView.setTextColor(Color.parseColor("#b0b0b0"));
            holder.isOpenTextView.setTextColor(Color.parseColor("#b0b0b0"));
        } else{
            holder.stateTextView.setTextColor(Color.parseColor("#000000"));
            holder.distanceTextView.setTextColor(Color.parseColor("#000000"));
            holder.addressTextView.setTextColor(Color.parseColor("#7d7d7f"));
            holder.isOpenTextView.setTextColor(Color.parseColor("#7d7d7f"));
        }
        holder.venueitemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(context, AfterPickingActivity.class);
                myIntent.putExtra("image", images.get(position));
                myIntent.putExtra("name", names.get(position));
                myIntent.putExtra("unknown", unknown.get(position));
                myIntent.putExtra("welcome", welcome_messages.get(position));
                myIntent.putExtra("description", descriptions.get(position));
                myIntent.putExtra("working_hours", is_open.get(position));
                context.startActivity(myIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        /*
        int i = 0;
        if (restaurants != null){
            while(restaurants[i] != null){
                i++;
            }
        } else{
            i = restaurants.length;
        }

        return i;
        */
        return num_of_elements;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView stateTextView;
        TextView distanceTextView;
        TextView addressTextView;
        TextView isOpenTextView;
        LinearLayout venueitemLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            stateTextView = itemView.findViewById(R.id.stateTextView);
            distanceTextView = itemView.findViewById(R.id.distanceTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            isOpenTextView = itemView.findViewById(R.id.isOpenTextView);
            venueitemLayout = itemView.findViewById(R.id.venueItemLayout);
        }
    }
}
