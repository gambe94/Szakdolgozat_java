package demeter.gabor.tracker.adapters;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import demeter.gabor.tracker.R;
import demeter.gabor.tracker.UserMapsActivity;
import demeter.gabor.tracker.Util.Constants;
import demeter.gabor.tracker.models.MyLocation;
import demeter.gabor.tracker.models.User;

/**
 * Created by demet on 2018. 03. 24..
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = UserAdapter.class.getName();

    private final Context context;
    private final List<User> userList;
    private final Map<String, User> userMap;
    private int lastPosition = -1;

    public UserAdapter(Context context) {
        this.context = context;
        this.userList = new ArrayList<>();
        this.userMap = new HashMap<>();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvUserName;
        final TextView tvUserEmail;
        final TextView tvLongitude;
        final TextView tvLatitude;
        final TextView tvAddress;

        final ImageView userProfileImage;



        ViewHolder(View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvLongitude = itemView.findViewById(R.id.tvLongitude);
            tvLatitude = itemView.findViewById(R.id.tvLangitude);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
        }
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_item, viewGroup, false);
        return new ViewHolder(myView);

    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final User tempUser = userList.get(position);
        viewHolder.tvUserName.setText(tempUser.getUsername());
        viewHolder.tvUserEmail.setText(tempUser.getEmail());


        if(tempUser.getLastLocation() == null){
            viewHolder.tvLongitude.setText(R.string.unknown);
            viewHolder.tvLatitude.setText(R.string.unknown);
            viewHolder.tvAddress.setText(R.string.unknown);
        } else {
            viewHolder.tvLongitude.setText(String.valueOf(tempUser.getLastLocation().getLongitude()));
            viewHolder.tvLatitude.setText(String.valueOf(tempUser.getLastLocation().getLatitude()));
            viewHolder.tvAddress.setText(getAddressFromMyLocation(tempUser));
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(context!= null) {
                    Intent showUserdata = new Intent(context,UserMapsActivity.class);
                    showUserdata.putExtra(Constants.LONGITUDE, tempUser.getLastLocation().getLongitude());
                    showUserdata.putExtra(Constants.LATITUDE, tempUser.getLastLocation().getLatitude());
                    showUserdata.putExtra(Constants.USERNAME, tempUser.getUsername());
                    showUserdata.putExtra(Constants.CURRENTUSER_UID, tempUser.getuId());

                    context.startActivity(showUserdata);
                }
            }
        });

        //SET PROFILE IMAGE
      if (!TextUtils.isEmpty(tempUser.getProfileImageURL())) {
            Glide.with(context).load(tempUser.getProfileImageURL()).into(viewHolder.userProfileImage);
            viewHolder.userProfileImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.userProfileImage.setVisibility(View.GONE);
        }

        //setAnimation(viewHolder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void addUser(User user, String key) {
        userList.add(user);
        userMap.put(key,user);
        notifyDataSetChanged();
    }

    public void updateLastLocation(MyLocation myLocation) {


        Log.d(TAG, "myLocation: " +  String.valueOf(myLocation));
        Log.d(TAG,"UserMAP: " + userMap.toString());


       if(myLocation != null && userMap.containsKey(myLocation.getUserId())){
           userMap.get(myLocation.getUserId()).setLastLocations(myLocation);
           notifyDataSetChanged();
       }

    }
    public void updateLastLocation(List<MyLocation> locations) {
        for(MyLocation loc :locations){
            if(loc != null && userMap.containsKey(loc.getUserId())){
                userMap.get(loc.getUserId()).setLastLocations(loc);
            }
        }
        notifyDataSetChanged();
    }


    public User getUserbyId(String uId){
        return userMap.get(uId);
    }


    public void updateProfileImage(String uId, String profileImgURL){
        if(userMap.containsKey(uId)){
            userMap.get(uId).setProfileImageURL(profileImgURL);
        }
        notifyDataSetChanged();
    }

    private String getAddressFromMyLocation(User tempUser) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(tempUser.getLastLocation().getLatitude(), tempUser.getLastLocation().getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address returnedAddress = addresses.get(0);
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    sb.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = sb.toString();

            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, String.valueOf(e.getStackTrace()));
        }

        return strAdd;
    }



    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context,
                    android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }




}
