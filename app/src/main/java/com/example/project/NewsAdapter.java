package com.example.project;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyViewHolder> {

    Context context;
    ArrayList<User> list;

    public NewsAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.news_item_row,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsAdapter.MyViewHolder holder, int position) {
        User user = list.get(position);
        holder.location.setText(user.getLocation());
        holder.name.setText(user.getName());
        holder.title.setText(user.getTitle());
        holder.description.setText(user.getDescription());

        // Format and set date and time
        String formattedTime = user.getTime();
        holder.time.setText(formattedTime);

        String formattedDate = user.getDate();
        holder.date.setText(formattedDate);

        // Set text for "Get Direction" and hide latitude and longitude
        String getDirectionText = "Get Direction";
        holder.location.setText(getDirectionText);
        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Convert double values to String before passing them to openGoogleMaps
                String latitudeString = String.valueOf(user.getLatitude());
                String longitudeString = String.valueOf(user.getLongitude());
                openGoogleMaps(latitudeString, longitudeString);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView location, name, title, description, time, date;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.textlocation);
            name = itemView.findViewById(R.id.textname2);
            title = itemView.findViewById(R.id.texttitle);
            description = itemView.findViewById(R.id.textdescription);
            time = itemView.findViewById(R.id.textViewTime);
            date = itemView.findViewById(R.id.textViewDate);
        }
    }

    private void openGoogleMaps(String latitude, String longitude) {
        // Create an Intent to open Google Maps
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Verify that the intent will resolve to an activity
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }
    }
}
