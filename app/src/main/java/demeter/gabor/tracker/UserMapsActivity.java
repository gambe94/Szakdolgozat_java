package demeter.gabor.tracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Stack;

import demeter.gabor.tracker.Util.Constants;
import demeter.gabor.tracker.models.MyLocation;

public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = UserMapsActivity.class.getName();
    private GoogleMap mMap;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mLocationReference;
    private DatabaseReference mLastKnownLocation;

    private Query mLastLocationQuery;

    private ValueEventListener locationLisener;
    private Double currentLongitude;
    private Double currentLatitude;
    private String username;
    private String uId;

    private int position = 0;
    private Stack<MarkerOptions> markers;

    //  private BroadcastReceiver broadcastReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "eletciklus ONCREATE");
        setContentView(R.layout.activity_user_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //DATABASES REFERENCE
        mUsersDatabase = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF);
        mLocationReference = FirebaseDatabase.getInstance().getReference(Constants.LOCATIONS_REF);
        mLastKnownLocation = FirebaseDatabase.getInstance().getReference(Constants.LAST_KNOWN_LOCATIONS_REF);

        mLastLocationQuery = mLocationReference.orderByKey().limitToLast(1);

        //GET DATA FROM INENT
        currentLongitude = getIntent().getDoubleExtra(Constants.LONGITUDE, 0);
        currentLatitude = getIntent().getDoubleExtra(Constants.LATITUDE, 0);
        username = getIntent().getStringExtra(Constants.USERNAME);
        uId = getIntent().getStringExtra(Constants.CURRENTUSER_UID);

        markers = new Stack<>();

        //Create Listeners
        createLocationListerner();
        mLastLocationQuery.addValueEventListener(locationLisener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.d(TAG, "eletciklus ONSTART");
    }

    private void createLocationListerner() {
        locationLisener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Log.d(TAG, "Location Query"+ dataSnapshot.toString());
                    MyLocation loc = data.getValue(MyLocation.class);

                    Log.d(TAG, "Location loc"+ String.valueOf(loc));
                    Log.d(TAG, "uid"+ String.valueOf(uId));
                    if(loc != null && uId.equals(loc.getUserId()) && mMap != null){

                        animateMarker(position++,new LatLng(currentLatitude,currentLongitude), new LatLng(loc.getLatitude(),loc.getLongitude()), false);
                        currentLongitude = loc.getLongitude();
                        currentLatitude = loc.getLatitude();


                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
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
    }

//    public void refreshMarker(MarkerOptions userMarker){
//        if(mMap != null){
//            LatLng currentUser = new LatLng(currentLatitude, currentLongitude);
//
//            mMap.addMarker(userMarker.position(currentUser).title(username));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentUser));
//        }
//    }

    //This methos is used to move the marker of each car smoothly when there are any updates of their position
    public void animateMarker(final int position, final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker) {



        MarkerOptions myMarker = new MarkerOptions()
                .position(startPosition)
                .title(username);


        if(!markers.isEmpty()){
            markers.pop().visible(false);
        }
        markers.push(myMarker);

        final Marker marker = mMap.addMarker(myMarker);



        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 1000;
        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;
                LatLng newPosition = new LatLng(lat, lng);
                marker.setPosition(newPosition);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(newPosition));
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
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


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "eletciklus ONPAUSE");
        mLastLocationQuery.removeEventListener(locationLisener);
        mLastKnownLocation.child(uId).setValue(new MyLocation(this.currentLatitude,this.currentLongitude,uId));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "eletciklus ONSTOP");

    }
}
