package demeter.gabor.tracker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import demeter.gabor.tracker.Util.BaseActivity;
import demeter.gabor.tracker.models.MyLocation;

public class DebugActivity extends BaseActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private StorageReference mImagesRefecence;

    private ImageView myimageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mImagesRefecence = mStorageRef.child("images");

        myimageView = findViewById(R.id.userProfileImage);

        initListeners();
    }

    private void initListeners() {



        mDatabase.child("images").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("DebugActivity datasn: ", dataSnapshot.toString());
                Log.d("DebugActivity snkey: ", dataSnapshot.getKey());
                Log.d("DebugActivity sndatava ", dataSnapshot.getValue().toString());

                List<MyLocation> list = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uId = ds.getKey();


//                    Log.d("DebugActivity ds: ", ds.toString());
//                    Log.d("DebugActivity uid: ", ds.getKey());
//                    Log.d("DebugActivity dschield:", ds.child(uId).toString()); //off value= null
//                    Log.d("DebugActivity dsValue:", ds.getValue().toString()); //ez jó visszaadja az adott listát
                    for (DataSnapshot dschield : ds.getChildren()) {
                        Log.d("evntListener dschield:", dschield.toString());
                        Log.d("evntListener dschield2:", dschield.getValue(String.class));
                        String pofileImgUri = dschield.getValue(String.class);

                        String currentUri = Uri.encode(pofileImgUri);
                        Log.d("evntListener currentUr:", currentUri);
                        downloadimges(currentUri);

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void downloadimges(String currentUri) {
        //StorageReference gsReference = mStorageRef.getReferenceFromUrl("gs://bucket/images/stars.jpg");
    }

    public void onDebug(View view){

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
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
