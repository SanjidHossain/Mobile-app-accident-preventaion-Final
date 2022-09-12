package com.example.finalprojectdu.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.finalprojectdu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class ProfileSetupActivity extends AppCompatActivity {

    private Button saveInfo;
    private EditText fullName, phone;
    private ProgressDialog loadingBar;
    private ImageView profileImage;
    private Uri imageUri;

    TextView bloodTV;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileImageRef;

    private String currentUserId;

    final static int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        saveInfo = findViewById(R.id.submit_button);
        fullName = findViewById(R.id.name_id);
        profileImage = findViewById(R.id.profile_image);
        phone = findViewById(R.id.phone_id);
        loadingBar = new ProgressDialog(this);
        bloodTV = findViewById(R.id.blood_id);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance("https://finalproject-d876c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile images");

        saveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfo();
            }
        });

//        profileImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent galleryIntent = new Intent();
//                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                galleryIntent.setType("image/*");
//                startActivityForResult(galleryIntent, GALLERY_PICK);
//            }
//        });

//        userRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
////                    String image = snapshot.child("profileImage").getValue().toString();
//                    try {
//                        Glide.with(getApplicationContext())
//                                .load(snapshot.child("profileImage").getValue().toString())
//                                .placeholder(R.drawable.loading)
//                                .into(profileImage);
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });



    }

    private void saveUserInfo() {
        String fullname = fullName.getText().toString().trim();
        String phn = phone.getText().toString().trim();
        String blood = bloodTV.getText().toString().trim();


        if (TextUtils.isEmpty(fullname) || TextUtils.isEmpty(phn)){
            Toast.makeText(this, "Fill Up All", Toast.LENGTH_SHORT).show();
        }else{
            loadingBar.setTitle("Setting information");
            loadingBar.setMessage("Please Wait");
            loadingBar.setCancelable(false);
//            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();


            HashMap userMap = new HashMap();
            userMap.put("fullName", fullname);
            userMap.put("phone", phn);
            userMap.put("imageUpload", 0);
            if (blood!=null){
                userMap.put("blood", blood);
            }
            userRef.child(mAuth.getUid()).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(ProfileSetupActivity.this, "Your Information successfully Added", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }else {
                        Toast.makeText(ProfileSetupActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!=null ){
            Uri imageUri = data.getData();

//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1,1)
//                    .start(this);

            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we updating your profile image...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

//                Uri resultUri = result.getUri();

            final StorageReference filepath = userProfileImageRef.child(currentUserId+".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){

                        //get Image Uri
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //get Image Uri
                                userRef.child("profileImage").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            loadingBar.dismiss();
                                        }
                                        else {
                                            loadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                        });

                    }
                }
            });



        }

//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (resultCode == RESULT_OK){
//                loadingBar.setTitle("Profile Image");
//                loadingBar.setMessage("Please wait, while we updating your profile image...");
//                loadingBar.show();
//                loadingBar.setCanceledOnTouchOutside(true);
//
//                Uri resultUri = result.getUri();
//
//                final StorageReference filepath = userProfileImageRef.child(currentUserId+".jpg");
//
//                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if (task.isSuccessful()){
//
//                            //get Image Uri
//                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                @Override
//                                public void onSuccess(Uri uri) {
//                                    //get Image Uri
//                                    userRef.child("profileImage").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()){
//                                                loadingBar.dismiss();
//                                            }
//                                            else {
//                                                loadingBar.dismiss();
//                                            }
//                                        }
//                                    });
//                                }
//                            });
//
//                        }
//                    }
//                });
//
//            }
//            else {
//                Toast.makeText(this, "Image Cant be cropped", Toast.LENGTH_SHORT).show();
//                loadingBar.dismiss();
//            }
//        }
    }



}