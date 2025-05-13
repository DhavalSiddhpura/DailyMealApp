package com.example.dailymeal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Home extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;
    TextView tvLocation, tvgreeting;
    Button gujthaliorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        tvgreeting = findViewById(R.id.tvGreeting);
        tvLocation = findViewById(R.id.tvLocation);
        ImageView imgProfile = findViewById(R.id.imgProfile);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fetchAndSetUserName();
        checkLocationPermissionAndFetch();

        imgProfile.setOnClickListener(view -> {
            Intent intent = new Intent(Home.this, UserProfileActivity.class);
            startActivity(intent);
        });

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_dashboard);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;
            } else if (id == R.id.nav_meals) {
                startActivity(new Intent(Home.this, Meal.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_subscription) {
                startActivity(new Intent(Home.this, Subscription.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(Home.this, Order.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_contactus) {
                startActivity(new Intent(Home.this, ContactUs.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        //set delivery card status code
        CardView cardDeliveryStatus = findViewById(R.id.cardDeliveryStatus);
        TextView tvOrderStatus = findViewById(R.id.tvOrderStatus);
        TextView tvMealDetails = findViewById(R.id.tvMealDetails);
        Button btnOrderReceived = findViewById(R.id.btnOrderReceived);
        Button btnOrderNotReceived = findViewById(R.id.btnOrderNotReceived);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        cardDeliveryStatus.setVisibility(View.VISIBLE);

// Default fallback UI for no order
        tvOrderStatus.setText("No current orders.");
        tvMealDetails.setText("Order meals now!");
        btnOrderReceived.setVisibility(View.GONE);
        btnOrderNotReceived.setVisibility(View.GONE);

        db.collection("Orders")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot orderDoc = queryDocumentSnapshots.getDocuments().get(0);

                        Long bill = orderDoc.getLong("totalBill");
                        String status = orderDoc.getString("status");
                        long timestamp = orderDoc.getTimestamp("timestamp").toDate().getTime();
                        long now = System.currentTimeMillis();
                        long minutesPassed = (now - timestamp) / (1000 * 60);

                        List<Map<String, Object>> meals = (List<Map<String, Object>>) orderDoc.get("meals");
                        StringBuilder mealDetailsBuilder = new StringBuilder();

                        if (meals != null) {
                            for (Map<String, Object> meal : meals) {
                                String name = (String) meal.get("name");
                                String quantity = String.valueOf(meal.get("quantity"));
                                mealDetailsBuilder.append(name)
                                        .append(" (x")
                                        .append(quantity)
                                        .append(")\n");
                            }
                        }

                        if ("Pending".equals(status) && minutesPassed <= 45) {
                            tvOrderStatus.setText("Your order is on the way!");
                            tvMealDetails.setText("Meals:\n" + mealDetailsBuilder.toString().trim() + "\nTotal: ₹" + bill);
                            btnOrderReceived.setVisibility(View.VISIBLE);
                            btnOrderNotReceived.setVisibility(View.VISIBLE);

                            btnOrderReceived.setOnClickListener(v -> {
                                db.collection("Orders").document(orderDoc.getId())
                                        .update("status", "Received");
                                tvOrderStatus.setText("No current orders.");
                                tvMealDetails.setText("Order meals now!");
                                btnOrderReceived.setVisibility(View.GONE);
                                btnOrderNotReceived.setVisibility(View.GONE);
                            });

                            btnOrderNotReceived.setOnClickListener(v -> {
                                db.collection("Orders").document(orderDoc.getId())
                                        .update("status", "Not Received");
                                Toast.makeText(this, "If order is not received within time please contact us!!!", Toast.LENGTH_SHORT).show();
                                Intent i= new Intent(Home.this,ContactUs.class);
                                startActivity(i);
                                tvOrderStatus.setText("No current orders.");
                                tvMealDetails.setText("Order meals now!");


                                btnOrderReceived.setVisibility(View.GONE);
                                btnOrderNotReceived.setVisibility(View.GONE);
                            });
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Home.this, "Error fetching order data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("HomeActivity", "Error fetching order data", e);
                });
        Button btnViewOrderHistory = findViewById(R.id.btnViewOrderHistory);
        btnViewOrderHistory.setVisibility(View.VISIBLE);

        btnViewOrderHistory.setOnClickListener(v -> {
            Intent i = new Intent(Home.this,HistoryOrder.class);
            startActivity(i);
        });
        gujthaliorder = findViewById(R.id.gujthaliorder);
        gujthaliorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Home.this, Meal.class);
                startActivity(i);
                finish();
            }
        });

    }


        private void fetchAndSetUserName() {
        tvgreeting.setText("Hi, 👋");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore.getInstance().collection("Users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("name");
                            if (name != null && !name.isEmpty()) {
                                tvgreeting.setText("Hi, " + name + " 👋");

                                // Save name to SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", name);
                                editor.apply();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvgreeting.setText("Hi 👋");
                    });
        }
    }

    private void checkLocationPermissionAndFetch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
        } else {
            checkLocationSettings();
        }
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                .addOnCompleteListener(task -> {
                    try {
                        task.getResult(ApiException.class);
                        getUserLocation();
                    } catch (ApiException e) {
                        if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                            try {
                                Status status = e.getStatus();
                                status.startResolutionForResult(Home.this, 1000);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            tvLocation.setText("Location settings unavailable");
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void getUserLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                setLocationText(location);
            } else {
                LocationRequest locationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(1000)
                        .setFastestInterval(500);

                fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                            fusedLocationClient.removeLocationUpdates(this);
                            Location loc = locationResult.getLastLocation();
                            setLocationText(loc);
                        }
                    }
                }, getMainLooper());
            }
        });
    }

    private void setLocationText(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder locationBuilder = new StringBuilder();
                if (address.getThoroughfare() != null) locationBuilder.append(address.getThoroughfare()).append(", ");
                if (address.getSubLocality() != null) locationBuilder.append(address.getSubLocality()).append(", ");
                if (address.getLocality() != null) locationBuilder.append(address.getLocality()).append(", ");
                if (address.getPostalCode() != null) locationBuilder.append(address.getPostalCode());

                String locationText = locationBuilder.toString().trim();
                if (locationText.endsWith(",")) locationText = locationText.substring(0, locationText.length() - 1);

                tvLocation.setText(!locationText.isEmpty() ? locationText : "Location unavailable");

                // Save location to SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userlocation", locationText);
                editor.apply();

                // Also save to Firestore
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                            .update("gpsloc", locationText);
                }
            } else {
                tvLocation.setText("Location unavailable");
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvLocation.setText("Location error");
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 101 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationSettings();
        } else {
            tvLocation.setText("Location permission denied");
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            getUserLocation();
        } else {
            tvLocation.setText("Location services required");
        }
    }
}
