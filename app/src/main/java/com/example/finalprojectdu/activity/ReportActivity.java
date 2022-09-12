package com.example.finalprojectdu.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalprojectdu.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ReportActivity extends AppCompatActivity {

    private static final int CAMERA_PICK = 2;
    private static final int CAMERA_PERMISSION_CODE = 101;
    String currentPhotoPath;

    //Initialize variables
    private EditText etLocation, etRoadCondition, etTime, etDate;
    private ImageView roadImage;
    private Button btSubmit;
    private ProgressDialog loadingBar;
    private String stEmail = "";
    private String roadImageUri = null;

    String KEY = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();;

    //Initialize Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference reportRef;
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
        setContentView(R.layout.activity_report);

        etLocation = findViewById(R.id.etLocation);
        etRoadCondition = findViewById(R.id.etRoadCondition);
        etTime = findViewById(R.id.etTime);
        btSubmit = findViewById(R.id.btSent);
        etDate = findViewById(R.id.etDate);

        loadingBar = new ProgressDialog(this);
        loadingBar.setCancelable(false);

        roadImage = findViewById(R.id.image);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        currentUserEmail = mAuth.getCurrentUser().getEmail();
        reportRef = FirebaseDatabase.getInstance("https://finalproject-d876c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Reports");
        roadImageRef = FirebaseStorage.getInstance().getReference().child("report images").child(mAuth.getCurrentUser().getUid());

        Intent intent = this.getIntent();
        lat = intent.getDoubleExtra("lat", 0.0);
        lan = intent.getDoubleExtra("lan", 0.0);

        etDate.setText(getCurrentDate());
        etTime.setText(getCurrentTime());
        etLocation.setText(lat + ", " + lan);

        roadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReportActivity.this);

                // set title
                alertDialogBuilder.setTitle("Note !");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Want to sent photo with camera or gallery?")
                        .setCancelable(false)
                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent galleryIntent = new Intent();
                                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                galleryIntent.setType("image/*");
                                startActivityForResult(galleryIntent, GALLERY_PICK);
                            }
                        })
                        .setNegativeButton("Camera",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                askCameraPermission();
                            }
                        })
                        .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();







//                AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        //set title
//                        .setTitle("Note")
//                        //set message
//                        .setMessage("Want to sent photo with camera or gallery?")
//                        //set positive button
//                        .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                Intent galleryIntent = new Intent();
//                                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
//                                galleryIntent.setType("image/*");
//                                startActivityForResult(galleryIntent, GALLERY_PICK);
//                            }
//                        })
//                        .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                askCameraPermission();
//                            }
//                        })
//                        .show();
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

    private void askCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }else {
            dispatchTakePictureIntent();
        }
    }

    //Image File Creation
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES); //It save in private, not showing in gallery
        //File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); //You
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.finalprojectdu.android.fileprovider",//It may conflict for many app in phone(Menifest Profider)
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_PICK);
            }
        }
    }

    private void saveUserInfo() {

        String roadCondition = etRoadCondition.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String name = userName;
        String phone = userPhone;
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

            reportRef.child(KEY).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
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
                        Toast.makeText(ReportActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

            loadingBar.setTitle("Report Image");
            loadingBar.setMessage("Please wait, while we updating your report image...");
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
                        Toast.makeText(ReportActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


        //Camera Image Pick
        if (requestCode == CAMERA_PICK && resultCode == RESULT_OK){
            File file = new File(currentPhotoPath);
            roadImage.setImageURI(Uri.fromFile(file));
            Log.d("EASY_MAIN", "onActivityResult: Url " + Uri.fromFile(file));

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);



            //uploadImageToFirebase(file.getName(), contentUri);

            loadingBar.setTitle("Report Image");
            loadingBar.setMessage("Please wait, while we updating your report image...");
            loadingBar.show();

            final StorageReference filepath = roadImageRef.child(KEY+".jpg");

            filepath.putFile(contentUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){

                        //get Image Uri
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //get Image Uri
                                roadImage.setImageURI(contentUri);
                                //Firebase image location uri
                                roadImageUri = uri.toString();
                                loadingBar.dismiss();
                            }
                        });

                    }else{
                        loadingBar.dismiss();
                        Toast.makeText(ReportActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

//    private void uploadImageToFirebase(String name, Uri contentUri) {
//        StorageReference image = storageReference.child("Product images/" + mAuth.getUid() + "/" + name);
//        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                    @Override
//                    public void onSuccess(Uri uri) {
//                        productImageLink = uri.toString();
//                        Log.d("EASY_HAND", "onSuccess: " + uri.toString());
//                    }
//                });
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.d("EASY_HAND", "onFailure: " + e.getMessage());
//            }
//        });
//    }


    public static String getCurrentDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = simpleDateFormat.format(c);
        return formattedDate;
    }

    public static String getCurrentTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss a");
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