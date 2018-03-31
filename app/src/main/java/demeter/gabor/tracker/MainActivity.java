package demeter.gabor.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import demeter.gabor.tracker.Util.BaseActivity;
import demeter.gabor.tracker.Util.Constants;
import demeter.gabor.tracker.adapters.UserAdapter;
import demeter.gabor.tracker.models.MyLocation;
import demeter.gabor.tracker.models.User;
import demeter.gabor.tracker.services.MyService;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getName();



    private Button startBtn, stopBtn;
    private RecyclerView recyclerViewUsers;
    private UserAdapter usersAdapter;
    private TextView tvLoginAs;

    private DatabaseReference mUsersDatabase;
    private DatabaseReference mLocationReference;
    private DatabaseReference mLastKnownLocation;

    private Query mLastLocationQuery;

    private ValueEventListener locationLisener;
    private ChildEventListener userListener;
    private ValueEventListener loadLastknownLocation;




    private BroadcastReceiver broadcastReceiver;
    private boolean isSaveLastData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DATABASES REFERENCE
        mUsersDatabase = FirebaseDatabase.getInstance().getReference(Constants.USERS_REF);
        mLocationReference = FirebaseDatabase.getInstance().getReference(Constants.LOCATIONS_REF);
        mLastKnownLocation = FirebaseDatabase.getInstance().getReference(Constants.LAST_KNOWN_LOCATIONS_REF);

        mLastLocationQuery = mLocationReference.orderByKey().limitToLast(1);

        //Create Listeners
        createLocationListerner();
        createUserListerner();

        //SET VIEWS
        tvLoginAs = (TextView) findViewById(R.id.loginAs);

        usersAdapter = new UserAdapter(getApplicationContext());
        recyclerViewUsers = (RecyclerView) findViewById(
                R.id.recyclerViewUsers);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerViewUsers.setLayoutManager(layoutManager);

        recyclerViewUsers.setAdapter(usersAdapter);



        startBtn = findViewById(R.id.startService);
        stopBtn = findViewById(R.id.stopService);

        //INIT DATABASES CHANGES LISTENER
        mUsersDatabase.addChildEventListener(userListener);
        mLastLocationQuery.addValueEventListener(locationLisener);

        //INIT FIREBASE EVENT LISTENERS When actevity is loaded
        mLastKnownLocation.addListenerForSingleValueEvent(loadLastknownLocation);


        //INIT variables

        tvLoginAs.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //CHECK PERMISSONS
        if (!runtime_permissions())
            enable_buttons();

    }



    private void createUserListerner() {
        userListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"AUTHTLISTENER_Added:"+ dataSnapshot.toString());
                User newUser = dataSnapshot.getValue(User.class);
                Log.d(TAG,"AUTHTLISTENER_Added:"+ newUser);
                usersAdapter.addUser(newUser, dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG,"AUTHTLISTENER_Changed:"+ dataSnapshot.toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG,"AUTHTLISTENER_Remove:"+ dataSnapshot.toString());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private void createLocationListerner() {
        locationLisener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                MyLocation loc;
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    Log.d(TAG, "Location Query"+ dataSnapshot.toString());
                    loc = data.getValue(MyLocation.class);
                    usersAdapter.updateLastLocation(loc);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        loadLastknownLocation = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "LastKnownLoc out "+ dataSnapshot.toString());

                List<MyLocation> locations = new ArrayList<>();
//
//                Log.d(TAG, "LastKnownLoc: "+ dataSnapshot);
//                Log.d(TAG, "LastKnownLoc  value: "+ dataSnapshot.getValue(MyLocation.class));
//                locations.add(dataSnapshot.getValue(MyLocation.class));

                for(DataSnapshot data : dataSnapshot.getChildren()){

                    Log.d(TAG, "LastKnownLoc: "+ data);
                    Log.d(TAG, "LastKnownLoc  value: "+ data.getValue(MyLocation.class));
                    locations.add(data.getValue(MyLocation.class));
                }
                if(!locations.isEmpty()) {
                    usersAdapter.updateLastLocation(locations);
                 }


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();


        isSaveLastData = false;

        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Double latitude = intent.getExtras().getDouble(Constants.LATITUDE);
                    Double longitude = intent.getExtras().getDouble(Constants.LONGITUDE);

                    //usersAdapter.updateLastLocation(getUid(), new MyLocation(latitude,longitude));
                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter(Constants.LOCATION_UPDATE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUsersDatabase.removeEventListener(userListener);
        mLocationReference.removeEventListener(locationLisener);
        mLastKnownLocation.removeEventListener(loadLastknownLocation);
        saveLastLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.action_logout) {
            saveLastLocation();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void enable_buttons() {

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(getApplicationContext(),MyService.class);
                startService(i);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(),MyService.class);
                stopService(i);

            }
        });

    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }


    public  void saveLastLocation(){
        Log.d(TAG, "saveLastLocation: "+ isSaveLastData);
        if(!isSaveLastData){
            mLastKnownLocation.child(getUid()).setValue(usersAdapter.getuserbyId(getUid()).getLastLocation());
            isSaveLastData = true;
        }

    }



}
