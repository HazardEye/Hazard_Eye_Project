package com.example.project;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {
    private String location;
    private String name;
    private String title;
    private String description;
    private String time; // Add date and time field
    private String date;
    private double latitude;
    private double longitude;

    public User(){
    }

    public User(String location, String name, String title, String description, String time, String date){
        this.location = location;
        this.name = name;
        this.title = title;
        this.description = description;
        this.time = time; // Set date and time
        this.date = date;

        if (location != null && !location.isEmpty()) {
            String[] locationArray = location.split(",");
            if (locationArray.length == 2) {
                this.latitude = Double.parseDouble(locationArray[0].trim());
                this.longitude = Double.parseDouble(locationArray[1].trim());
            }
        }
    }

    public String getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getTime() {return time;}
    public String getDate() {return date;}
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public void setDate(String date) {
        this.date = date;
    }
}
