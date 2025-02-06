package com.example.project;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.SupportMapFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleMap mMap;

    Button submitBtn;
    EditText location, description;
    TextView name;
    DatabaseReference databaseNews;
    Spinner title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        TextView linkEditText = findViewById(R.id.linkEditText);
        TextView googleName = findViewById(R.id.nameEditText);

        linkEditText.setVisibility(View.GONE);
        googleName.setVisibility(View.GONE);

        name = findViewById(R.id.nameEditText);
        submitBtn = findViewById(R.id.submitButton);
        location = findViewById(R.id.linkEditText);
        title = findViewById(R.id.titleEditText);
        description = findViewById(R.id.reportEditText);

        databaseNews = FirebaseDatabase.getInstance().getReference("news");

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            name.setText(personName);
        }

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitData();
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize map and location
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    getCurrentLocation();
                    loadHazardsFromFirebase(); // Fetch and display hazard markers
                }
            });
        }
    }

    private void getCurrentLocation() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if (ActivityCompat.checkSelfPermission(ReportActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        fusedLocationProviderClient.getLastLocation()
                                .addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location locationResult) {
                                        if (locationResult != null) {
                                            LatLng latLng = new LatLng(locationResult.getLatitude(), locationResult.getLongitude());
                                            mMap.clear();
                                            mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
                                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                                            // Store location in EditText
                                            location.setText(locationResult.getLatitude() + "," + locationResult.getLongitude());
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ReportActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void loadHazardsFromFirebase() {
        DatabaseReference hazardsRef = FirebaseDatabase.getInstance().getReference("news");

        hazardsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear(); // Clear previous markers

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String locationString = snapshot.child("userLocation").getValue(String.class);
                    String title = snapshot.child("userTitle").getValue(String.class);
                    String description = snapshot.child("userDescription").getValue(String.class);

                    if (locationString != null && !locationString.isEmpty()) {
                        String[] latLng = locationString.split(",");
                        double latitude = Double.parseDouble(latLng[0]);
                        double longitude = Double.parseDouble(latLng[1]);

                        LatLng hazardLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(hazardLocation).title(title).snippet(description));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ReportActivity.this, "Failed to load hazard markers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SubmitData() {
        String userLocation = location.getText().toString();
        String username = name.getText().toString();
        String userTitle = title.getSelectedItem().toString();
        String userDescription = description.getText().toString();

        if (userLocation.isEmpty()) {
            Toast.makeText(this, "Please wait for location or enter manually", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDescription.isEmpty()) {
            Toast.makeText(this, "Please fill in the description", Toast.LENGTH_SHORT).show();
            return;
        }

        int wordCount = userDescription.split("\\s+").length;
        if (wordCount > 30) {
            Toast.makeText(this, "Description is too long (max 30 words)", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = databaseNews.push().getKey();
        String time = getCurrentTime();
        String date = getCurrentDate();

        User user = new User(userLocation, username, userTitle, userDescription, time, date);

        databaseNews.child(id).setValue(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ReportActivity.this, "Report Submitted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
