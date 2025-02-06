package com.example.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.SupportMapFragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
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

import java.util.HashMap;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {

    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;

    private FloatingActionButton resetLocationButton;
    private TextView nameTextView;
    private TextView emailTextView;
    private GoogleSignInClient gsc;
    GoogleSignInOptions gso;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    ViewPager viewPager;

    TextView name, email;
    private DatabaseReference markersRef;
    private GoogleMap googleMap;
    private Map<String, Marker> markersMap = new HashMap<>(); // To store markers with unique IDs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personEmail = acct.getEmail();
            name.setText(personName);
            email.setText(personEmail);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    Toast.makeText(SecondActivity.this, getString(R.string.o), Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.profile) {
                    startActivity(new Intent(SecondActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.news) {
                    startActivity(new Intent(SecondActivity.this, NewsActivity.class));
                } else if (itemId == R.id.report) {
                    startActivity(new Intent(SecondActivity.this, ReportActivity.class));
                } else if (itemId == R.id.help) {
                    startActivity(new Intent(SecondActivity.this, HelpActivity.class));
                } else if (itemId == R.id.signout) {
                    signOut();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        Button newsBtn = findViewById(R.id.news2);
        Button reportBtn = findViewById(R.id.submitreport2);
        Button helpBtn = findViewById(R.id.help2);

        newsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newsPage();
            }
        });

        reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportPage();
            }
        });

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpPage();
            }
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        fusedLocationProviderClient = (FusedLocationProviderClient) LocationServices.getFusedLocationProviderClient(this);

        Dexter.withContext(getApplicationContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        getCurrentLocation();
                    }
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        // Handle permission denied
                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

        // Create a new FloatingActionButton for reset
        FloatingActionButton resetButton = new FloatingActionButton(this);
        resetButton.setImageResource(R.drawable.resetlocation); // Set your icon here
        resetButton.setSize(FloatingActionButton.SIZE_NORMAL);
        resetButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.main))); // Set background tint from colors.xml
        resetButton.setRippleColor(getResources().getColor(android.R.color.white));
        resetButton.setElevation(12);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMapToCurrentLocation();
            }
        });

        // Set layout parameters for the new button
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.BOTTOM | Gravity.END;
        layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.fab_margin) + 150;
        layoutParams.rightMargin = (int) getResources().getDimension(R.dimen.fab_margin);
        ((FrameLayout) findViewById(R.id.map)).addView(resetButton, layoutParams);

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                // Fetch and display markers on the map
                onMapReadyCallback(googleMap);
            }
        });
    }

    private void onMapReadyCallback(GoogleMap googleMap) {
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("users");

        reportsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                googleMap.clear(); // Clear existing markers

                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                    double latitude = reportSnapshot.child("latitude").getValue(Double.class);
                    double longitude = reportSnapshot.child("longitude").getValue(Double.class);
                    String title = reportSnapshot.child("title").getValue(String.class);

                    LatLng reportLatLng = new LatLng(latitude, longitude);

                    // Show a marker at the report's location with custom icon
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(reportLatLng)
                            .title(title)
                            .icon(getMarkerIcon(title)); // Replace with your custom icon

                    Marker marker = googleMap.addMarker(markerOptions);
                    markersMap.put(reportSnapshot.getKey(), marker); // Store marker with unique ID
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private BitmapDescriptor getMarkerIcon(String title) {
        // Define your mapping of titles to icon resources here
        switch (title) {
            case "Road Damage":
                return BitmapDescriptorFactory.fromResource(R.drawable.roaddamage);
            case "Flood":
                return BitmapDescriptorFactory.fromResource(R.drawable.flood);
            case "Accident":
                return BitmapDescriptorFactory.fromResource(R.drawable.accident);
            case "Landslide":
                return BitmapDescriptorFactory.fromResource(R.drawable.landslide);
            // Add cases for other titles as needed
            default:
                return BitmapDescriptorFactory.fromResource(R.drawable.others);
        }
    }

    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationProviderClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        // Make sure the map is ready before adding the marker
                        supportMapFragment.getMapAsync(googleMap -> {
                            googleMap.clear(); // Clear any existing markers
                            googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Location"));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); // Adjust zoom level
                        });
                    } else {
                        Toast.makeText(SecondActivity.this, "Unable to get location, please try again.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SecondActivity.this, "Location service failed.", Toast.LENGTH_SHORT).show();
                });
    }

    public void resetMapToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(@NonNull GoogleMap googleMap) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Current Location !");
                            //googleMap.clear(); // Clear existing markers
                            //googleMap.addMarker(markerOptions);
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        } else {
                            Toast.makeText(SecondActivity.this, "Please turn on your location permissions", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void newsPage() {
        Intent intent = new Intent(SecondActivity.this, NewsActivity.class);
        startActivity(intent);
    }

    private void reportPage() {
        Intent intent = new Intent(SecondActivity.this, ReportActivity.class);
        startActivity(intent);
    }

    private void helpPage() {
        Intent intent = new Intent(SecondActivity.this, HelpActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}