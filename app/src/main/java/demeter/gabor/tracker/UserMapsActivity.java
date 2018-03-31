package demeter.gabor.tracker;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import demeter.gabor.tracker.adapters.UserAdapter;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Double currentLongitude;
    private Double currentLatitude;
    private String username;

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

        this.currentLongitude = getIntent().getDoubleExtra(UserAdapter.LONGITUDE, 0);
        this.currentLatitude = getIntent().getDoubleExtra(UserAdapter.LATITUDE, 0);
        this.username = getIntent().getStringExtra(UserAdapter.USERNAME);

        Log.d("UserMapActivity", this.currentLongitude.toString());
        Log.d("UserMapActivity", this.currentLatitude.toString());
        Log.d("UserMapActivity", this.username.toString());

        // Add a marker in Sydney and move the camera
        LatLng currentUser = new LatLng(currentLatitude, currentLongitude);
        mMap.addMarker(new MarkerOptions().position(currentUser).title(this.username));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentUser));
    }
}
