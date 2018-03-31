package demeter.gabor.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import demeter.gabor.tracker.Util.Constants;
import demeter.gabor.tracker.adapters.UserAdapter;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;



  //  private BroadcastReceiver broadcastReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Double currentLongitude = getIntent().getDoubleExtra(Constants.LONGITUDE, 0);
        Double currentLatitude = getIntent().getDoubleExtra(Constants.LATITUDE, 0);
        String username = getIntent().getStringExtra(Constants.USERNAME);

        Log.d("UserMapActivity", currentLongitude.toString());
        Log.d("UserMapActivity", currentLatitude.toString());
        Log.d("UserMapActivity", username.toString());

        // Add a marker in Sydney and move the camera
        LatLng currentUser = new LatLng(currentLatitude, currentLongitude);

        mMap.addMarker(new MarkerOptions().position(currentUser).title(username));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentUser));
    }



//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(broadcastReceiver == null){
//            broadcastReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//
//                    Double latitude = intent.getExtras().getDouble(Constants.LATITUDE);
//                    Double longitude = intent.getExtras().getDouble(Constants.LONGITUDE);
//                    String username = intent.getExtras().getString(Constants.USERNAME);
//
//                    LatLng currentUser = new LatLng(latitude, longitude);
//                    showMarkerOnTheMap(currentUser, username);
//
//                }
//            };
//        }
//        registerReceiver(broadcastReceiver,new IntentFilter(Constants.LOCATION_UPDATE));
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        unregisterReceiver(broadcastReceiver);
//    }
}
