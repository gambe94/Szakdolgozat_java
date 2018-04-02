package demeter.gabor.tracker.Util;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();

    private ProgressDialog mProgressDialog;


    protected StorageReference mStorageRef;
    protected  StorageReference mImagesRefecence;


    protected DatabaseReference mDatabase;
    protected DatabaseReference  mImages;


    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(message);
        }

        mProgressDialog.show();
    }

    public void showProgressDialogPercentage(double percentage) {
        if (mProgressDialog == null) {
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage("Uploaded" +(int)percentage+"%");
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    protected    void selectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), Constants.PICK_IMAGE_REQUEST);
    }

    protected void uploadImagetoFireBase(Bitmap bitmapToUpload) {
        Log.d(TAG, "uploadImagetoFireBase: ");


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapToUpload.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mImagesRefecence.child(getUid()).child(Constants.PROFILE_IMG).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("FireBase Upload", exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(getApplicationContext(), "Image Uploaded Successfully ", Toast.LENGTH_LONG).show();
                Log.d("FireBase Upload",downloadUrl.toString());

                mImages.child(getUid()).setValue(downloadUrl.toString());
            }
        });

    }



}