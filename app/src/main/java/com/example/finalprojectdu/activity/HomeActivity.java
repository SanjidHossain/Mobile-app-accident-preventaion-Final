package com.example.finalprojectdu.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.finalprojectdu.R;
import com.example.finalprojectdu.adapter.ReportAdapter;
import com.example.finalprojectdu.model.Report;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    // Initialize Google Map
    SupportMapFragment smf;
    double lat, lan;
    FusedLocationProviderClient client;

    //Initialize Database
    private FirebaseAuth mAuth;
    private DatabaseReference bokRef, userRef;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference reportRef;

    private List<Report> reportList;

    public static int UPLOAD_IMAGES = -1;

    Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Initialize Database
        databaseInitialization();

        // Google Map
        showingMap();
        lat = 0.0;
        lan = 0.0;

        //Button Press
        onClick();

    }

    private void showingMap() {
        smf = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        client = LocationServices.getFusedLocationProviderClient(this);

        Dexter.withContext(getApplicationContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        getPermission();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
    }

    private void databaseInitialization(){
        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance("https://finalproject-d876c-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("Users");
    }

    private void onClick() {

        // Profile Button
        findViewById(R.id.btProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        //Report Button
        findViewById(R.id.btReport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ReportActivity.class).putExtra("lat", lat).putExtra("lan", lan));
            }
        });

        // Accident Button
        findViewById(R.id.btEmergency).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), AccidentActivity.class).putExtra("lat", lat).putExtra("lan", lan));
            }
        });

        // daily Upload Button
        findViewById(R.id.btDailyUpload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), DailyUploadActivity.class));
            }
        });

    }

    // Location Permission
    private void getPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                smf.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        lat = location.getLatitude();
                        lan = location.getLongitude();
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("You are Here!!");


//                        for (int i = 0; i < reportList.size(); i++) {
//                            LatLng latLngTemp = new LatLng(reportList.get(i).getLat(), reportList.get(i).getLan());
//                            MarkerOptions markerOptionsTemp = new MarkerOptions().position(latLngTemp).title(reportList.get(i).getRoadCondition());
//                            googleMap.addMarker(markerOptionsTemp);
//                        }


                        //LatLng latLng2 = new LatLng(23.777176, 90.399452);
                        //MarkerOptions markerOptions2 = new MarkerOptions().position(latLng2).title("You are There!!");

                        googleMap.addMarker(markerOptions);
                        //googleMap.addMarker(markerOptions2);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                });
            }
        });
    }

    private void getPermission(double lat, double lan, String roadCondition) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> task = client.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                smf.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        LatLng latLng = new LatLng(lat, lan);

                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(roadCondition);

//                        for (int i = 0; i < reportList.size(); i++) {
//                            LatLng latLngTemp = new LatLng(reportList.get(i).getLat(), reportList.get(i).getLan());
//                            MarkerOptions markerOptionsTemp = new MarkerOptions().position(latLngTemp).title(reportList.get(i).getRoadCondition());
//                            googleMap.addMarker(markerOptionsTemp);
//                        }

                        googleMap.addMarker(markerOptions);
                        //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                });
            }
        });
    }

    // Checking User is sign in or not
    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            checkUserExistence();
        }
    }

    // Checking user data is available or not
    private void checkUserExistence() {
        final String current_user_id = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(current_user_id)){
                    Intent intent = new Intent(getApplicationContext(), ProfileSetupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                else {
                    getReports();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //   Get Reports
    ////////////////////////////////////////////////////////////////////////////////////////////////


    public void getReports(){
        firebaseDatabase = FirebaseDatabase.getInstance("https://finalproject-d876c-default-rtdb.asia-southeast1.firebasedatabase.app/");
        reportRef = firebaseDatabase.getReference("Reports");

        reportList = new ArrayList<>();
        reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot npsnapshot : snapshot.getChildren()){
                        Report l = npsnapshot.getValue(Report.class);
                        //IF Else Time
                        reportList.add(l);
                        getPermission(l.getLat(), l.getLan(), l.getRoadCondition());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////
    //   Menu (Sign Out)
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.signOut){
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.reload){
            showingMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}































