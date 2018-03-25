package demeter.gabor.tracker.models;


import android.location.Location;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class MyLocation{

    private Double longitude;
    private Double latitude;
    //private Timestamp timestamp;

    public MyLocation() {

    }

    public MyLocation(Location loc) {
        this.latitude =loc.getLatitude();
        this.longitude = loc.getLongitude();
       // this.timestamp = timestamp;


    }

    public MyLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
      // this.timestamp = timestamp;

    }


    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

//    public Timestamp getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(Timestamp timestamp) {
//        this.timestamp = timestamp;
//    }


    @Override
    public String toString() {
        return "longitude: " + this.getLongitude()+ " latitude"+ getLatitude();
    }
}