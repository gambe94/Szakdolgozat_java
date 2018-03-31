package demeter.gabor.tracker.adapters;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import demeter.gabor.tracker.R;
import demeter.gabor.tracker.UserMapsActivity;
import demeter.gabor.tracker.models.MyLocation;
import demeter.gabor.tracker.models.User;

/**
 * Created by demet on 2018. 03. 24..
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {


    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String USERNAME = "username";



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvUserName;
        public TextView tvUserEmail;
        public TextView tvLongitude;
        public TextView tvLangitude;
        public TextView tvAddress;
        public ImageView userProfileImage;

        public ViewHolder(View itemView) {
            super(itemView);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvUserEmail = (TextView) itemView.findViewById(R.id.tvUserEmail);
            tvLongitude = (TextView) itemView.findViewById(R.id.tvLongitude);
            tvLangitude = (TextView) itemView.findViewById(R.id.tvLangitude);
            tvAddress = (TextView) itemView.findViewById(R.id.tvAddress);
            userProfileImage = (ImageView) itemView.findViewById(R.id.userProfileImage);
        }
    }

    private Context context;
    private List<User> userList;
    private Map<String, User> userMap;
    private int lastPosition = -1;

    public UserAdapter(Context context) {
        this.context = context;
        this.userList = new ArrayList<>();
        this.userMap = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_item, viewGroup, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final User tempUser = userList.get(position);
        viewHolder.tvUserName.setText(tempUser.getUsername());
        viewHolder.tvUserEmail.setText(tempUser.getEmail());

        //viewHolder.userProfileImage.setImageURI(Uri.parse(tempUser.getProfileImageURL()));
        //Glide.with(context).load(tempUser.getProfileImageURL()).into(viewHolder.userProfileImage);

        if(tempUser.getLastLocation() == null){
            viewHolder.tvLongitude.setText("Nem Ismert");
            viewHolder.tvLangitude.setText("Nem ismert");
            viewHolder.tvAddress.setText("Nem ismert");
        }else{


            viewHolder.tvLongitude.setText(String.valueOf(tempUser.getLastLocation().getLongitude()));
            viewHolder.tvLangitude.setText(String.valueOf(tempUser.getLastLocation().getLatitude()));


            viewHolder.tvAddress.setText(getAddressFromMyLocation(tempUser));

        }


        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(context!= null) {
                    Intent showUserdetails = new Intent(context,UserMapsActivity.class);
                    showUserdetails.putExtra(LONGITUDE, tempUser.getLastLocation().getLongitude());
                    showUserdetails.putExtra(LATITUDE, tempUser.getLastLocation().getLatitude());
                    showUserdetails.putExtra(USERNAME, tempUser.getUsername());

                    context.startActivity(showUserdetails);
                }
            }
        });


/*
      if (!TextUtils.isEmpty(tempUser.getProfileImageURL())) {
            Glide.with(context).load(tempUser.getProfileImageURL()).into(viewHolder.userProfileImage);
            viewHolder.userProfileImage.setVisibility(View.VISIBLE);
        } else {
            viewHolder.userProfileImage.setVisibility(View.GONE);
        }

        setAnimation(viewHolder.itemView, position);
*/
    }

    private String getAddressFromMyLocation(User tempUser) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(tempUser.getLastLocation().getLatitude(), tempUser.getLastLocation().getLongitude(), 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder sb = new StringBuilder("");
                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    sb.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = sb.toString();
                Log.w("Current loction address", sb.toString());
            } else {
                Log.w("Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UserAdapter", String.valueOf(e.getStackTrace()));
        }
        return strAdd;
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
    public void updateLastLocation(Map<String, Stack<MyLocation>> locationMap) {
        for(String uid :locationMap.keySet()){
            userMap.get(uid).setStackLocation(locationMap.get(uid));
        }

    }
    public void updateLastLocation(String uId, MyLocation myLocation) {
        userMap.get(uId).setLastLocations(myLocation);
    }

    public Set<String> getUserUIds(){
        return this.userMap.keySet();
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
