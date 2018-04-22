package demeter.gabor.tracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    //Database reference manage Users
    private DatabaseReference mUsersDatabase;

    //Database reference manage Location
    private DatabaseReference mLocationReference;
    private DatabaseReference mLastKnownLocation;
    private Query mLastLocationQuery;


    //EVENT Listenres
    private ValueEventListener locationListener;
    private ChildEventListener userListener;
    private ValueEventListener loadLastknownLocation;
    private ChildEventListener imagesListener;





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

        mImages = FirebaseDatabase.getInstance().getReference(Constants.IMAGES_REF); //filed declared in baseActivity

        Log.d(TAG,"mImages 1: "+ String.valueOf(mImages));
        mLastLocationQuery = mLocationReference.orderByKey().limitToLast(1);


        //CLOUD MESSAGING

        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW));
        }




        //STORRAGE REFERENCE
        mStorageRef = FirebaseStorage.getInstance().getReference(); //filed declared in baseActivity
        mImagesRefecence = mStorageRef.child(Constants.IMAGES_STORAGR_REF); //filed declared in baseActivity

        //Create Listeners
        createLocationListener();
        createUserListener();
        createImageListener();

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
        mImages.addChildEventListener(imagesListener);

        //INIT variables
        tvLoginAs.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //CHECK PERMISSONS
        if (!runtime_permissions())
            enable_buttons();

    }


    private void createImageListener() {

        imagesListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String imageURL = dataSnapshot.getValue(String.class);
                usersAdapter.updateProfileImage(dataSnapshot.getKey(), imageURL);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "imagelistener change: " + dataSnapshot.toString());
                String imageURL = dataSnapshot.getValue(String.class);
                usersAdapter.updateProfileImage(dataSnapshot.getKey(), imageURL);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "imagelistener remove: " + dataSnapshot.toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "imagelistener moved: " + dataSnapshot.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

    }
    private void createUserListener() {
        userListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Log.d(TAG,"AUTHTLISTENER_Added:"+ dataSnapshot.toString());
                User newUser = dataSnapshot.getValue(User.class);
                Log.d(TAG,"AUTHTLISTENER_Added:"+ newUser.toString());
                usersAdapter.addUser(newUser, dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Log.d(TAG,"AUTHTLISTENER_Changed:"+ dataSnapshot.toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
               // Log.d(TAG,"AUTHTLISTENER_Remove:"+ dataSnapshot.toString());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
    private void createLocationListener() {
        locationListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {                ;
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
        Log.d(TAG, "eletciklus  ONSART");

        //ADD DATABASES CHANGES LISTENER
        mLastKnownLocation.addListenerForSingleValueEvent(loadLastknownLocation);
        mLastLocationQuery.addValueEventListener(locationListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "eletciklus ONRESUME");

        isSaveLastData = false;
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

        mLocationReference.removeEventListener(locationListener);
        mLastKnownLocation.removeEventListener(loadLastknownLocation);


        saveLastLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImages.removeEventListener(imagesListener);
        mUsersDatabase.removeEventListener(userListener);
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }

        FirebaseMessaging.getInstance().unsubscribeFromTopic("pushNotifications");
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
        }  else if (i == R.id.uploadProfileImg) {
            selectImageFromGallery();

            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void enable_buttons() {

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MyService.class);
                startService(i);
                sendFCMNotificationToOthers();
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

    private void sendFCMNotificationToOthers()  {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    Log.d(TAG, "MY Post thread started");
                    Thread.sleep(300);
                    URL url = new URL("https://gcm-http.googleapis.com/gcm/send");

                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Authorization", getString(R.string.authorization_key));
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("to", getString(R.string.notificatoin_path));

                    JSONObject dataChield = new JSONObject();
                    dataChield.put(Constants.CURRENTUSER_UID, getUid());
                    User currentUser = usersAdapter.getuserbyId(getUid());
                    dataChield.put(Constants.USERNAME, currentUser.getUsername());
                    dataChield.put(Constants.LATITUDE, currentUser.getLastLocation().getLatitude());
                    dataChield.put(Constants.LONGITUDE, currentUser.getLastLocation().getLongitude());

                    jsonObject.put("data",  dataChield);


                    JSONObject notificationChield = new JSONObject();

                    notificationChield.put("title", "Help for, " + currentUser.getUsername());
                    notificationChield.put("text", "View his position on Map");

                    jsonObject.put("notification", notificationChield);

                    Log.i("JSON", jsonObject.toString());

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonObject.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));

                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if(conn != null){
                        conn.disconnect();
                    }
                }
            }
        });

        thread.start();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null )
        {

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }

            uploadImagetoFireBase(bitmap);

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
