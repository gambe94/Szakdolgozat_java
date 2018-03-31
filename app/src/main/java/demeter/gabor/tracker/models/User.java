package demeter.gabor.tracker.models;



import android.location.Location;
import android.net.Uri;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;
import java.util.Stack;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {


    private String username;
    private String email;
    private Stack<MyLocation> locations = new Stack<>();
    private Uri profileImageURI;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) { // for login/sign in
        this.username = username;
        this.email = email;

    }
    public User(String username, String email, MyLocation loc) {
        this.username = username;
        this.email = email;
        this.locations.push(loc);

    }

    public MyLocation getLastLocation(){
        if(!this.locations.empty()){
            return this.locations.peek();
        }
        return null;
    }

    public Uri getProfileImageURI() {
        return profileImageURI;
    }

    public void setProfileImageURI(Uri profileImageURI) {
        this.profileImageURI = profileImageURI;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }



    public void setLastLocations(MyLocation loc) {
        if(this.locations == null){
            locations = new Stack<>();
            locations.push(loc);
        }else{
            this.locations.push(loc);
        }

    }

    public void setStackLocation(Stack<MyLocation> stackLocation) {
        this.locations = stackLocation;
    }


}