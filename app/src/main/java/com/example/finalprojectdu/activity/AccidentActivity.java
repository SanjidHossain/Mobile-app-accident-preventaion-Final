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
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class AccidentActivity extends AppCompatActivity {

    //Initialize variables
    private EditText etLocation, etRoadCondition, etTime, etDate, etiv;
    private ImageView roadImage;
    private Button btSubmit;
    private ProgressDialog loadingBar;
    private String stEmail = "";
    private String roadImageUri = null;

    String KEY = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();;

    //Initialize Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference accidentRef;
    private StorageReference roadImageRef;
    private String currentUserId;
    private String currentUserEmail;

    final static int GALLERY_PICK = 1;

    double lat, lan;

    String userName = "";
    String userPhone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident);

        etLocation = findViewById(R.id.etLocation);
        etRoadCondition = findViewById(R.id.etRoadCondition);
        etTime = findViewById(R.id.etTime);
        btSubmit = findViewById(R.id.btSent);
        etDate = findViewById(R.id.etDate);
        etiv = findViewById(R.id.etInvolvedVehicle);

        loadingBar = new ProgressDialog(this);
        loadingBar.setCancelable(false);

        roadImage = findViewById(R.id.image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserEmail = mAuth.getCurrentUser().getEmail();
        accidentRef = FirebaseDatabase.getInstance("https://finalproject-d876c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Accidents");
        roadImageRef = FirebaseStorage.getInstance().getReference().child("accident images").child(mAuth.getCurrentUser().getUid());

        Intent intent = this.getIntent();
        lat = intent.getDoubleExtra("lat", 0.0);
        lan = intent.getDoubleExtra("lan", 0.0);

        etDate.setText(getCurrentDate());
        etTime.setText(getCurrentTime());
        etLocation.setText(lat + ", " + lan);

        roadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });

        //Submit Data
        btSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInfo();
            }
        });
    }

    private void saveUserInfo() {

        String roadCondition = etRoadCondition.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String name = userName;
        String phone = userPhone;
        String iv = etiv.getText().toString().trim();
        String uid = FirebaseAuth.getInstance().getUid();

        String location = etLocation.getText().toString().trim();
        String[] arrOfStr = location.split(",");
        double lat = Double.parseDouble(arrOfStr[0]);
        double lan = Double.parseDouble(arrOfStr[1]);

        if (TextUtils.isEmpty(roadCondition) || TextUtils.isEmpty(time) || TextUtils.isEmpty(date)){
            Toast.makeText(this, "Fill Up All", Toast.LENGTH_SHORT).show();
        }else {
            loadingBar.show();

            HashMap userMap = new HashMap();
            userMap.put("lat", lat);
            userMap.put("lan", lan);
            userMap.put("time", time);
            userMap.put("date", date);
            userMap.put("roadCondition", roadCondition);
            userMap.put("uid", uid);
            userMap.put("name", name);
            userMap.put("phone", phone);
            userMap.put("image", roadImageUri);
            userMap.put("iv", iv);

            accidentRef.child(KEY).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){

//                        //Save Done
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                        loadingBar.dismiss();

                    }else {
                        Toast.makeText(AccidentActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Image 0
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!=null ){

            Uri imageUri = data.getData();

            loadingBar.setTitle("Accident Image");
            loadingBar.setMessage("Please wait, while we updating your accident image...");
            loadingBar.show();

            final StorageReference filepath = roadImageRef.child(KEY+".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){

                        //get Image Uri
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //get Image Uri
                                roadImage.setImageURI(imageUri);
                                //Firebase image location uri
                                roadImageUri = uri.toString();
                                loadingBar.dismiss();
                            }
                        });

                    }else{
                        loadingBar.dismiss();
                        Toast.makeText(AccidentActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public static String getCurrentDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = simpleDateFormat.format(c);
        return formattedDate;
    }

    public static String getCurrentTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm a");
        String formattedTime = simpleDateFormat.format(c);
        return formattedTime;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getUserData();
    }

    public void getUserData(){
        FirebaseAuth mAuth;
        DatabaseReference userRef;
        StorageReference userProfileImageRef;

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance("https://finalproject-d876c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile images");

        userRef.child(Objects.requireNonNull(mAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
//                    String image = snapshot.child("profileImage").getValue().toString();
                    if (snapshot.hasChild("fullName")) {
                        String name = snapshot.child("fullName").getValue().toString();
                        userName = name;
                    }
                    if (snapshot.hasChild("phone")) {
                        String phone1 = snapshot.child("phone").getValue().toString();
                        userPhone = phone1;
                    }


                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}