package com.example.dailymeal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Subscription extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imgProfile;
    FirebaseFirestore db;

    // Weekly Plan UI
    TextView tvWeeklyDesc, tvWeeklyDuration, tvWeeklyPrice;
    Button btnBuyNowWeekly;

    // Monthly Plan UI
    TextView tvMonthlyDesc, tvMonthlyDuration, tvMonthlyPrice;
    Button btnBuyNowMonthly;

    String weeklyPlanName = "", monthlyPlanName = "";
    Double weeklyPlanPrice = 0.0, monthlyPlanPrice = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_subscription);

        db = FirebaseFirestore.getInstance();
        imgProfile = findViewById(R.id.imgProfile);

        // Greeting and Location
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("username", "Hi 👋");
        String location = sharedPreferences.getString("userlocation", "Getting location...");

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvLocation = findViewById(R.id.tvLocation);
        tvGreeting.setText("Hi, " + name + " 👋");
        tvLocation.setText(location);

        // Profile click
        imgProfile.setOnClickListener(view -> {
            Intent i = new Intent(Subscription.this, UserProfileActivity.class);
            i.putExtra("from", "Subscription");
            startActivity(i);
        });

        // Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_subscription);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), Home.class));
            } else if (itemId == R.id.nav_contactus) {
                startActivity(new Intent(getApplicationContext(), ContactUs.class));
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(getApplicationContext(), Order.class));
            } else if (itemId == R.id.nav_meals) {
                startActivity(new Intent(getApplicationContext(), Meal.class));
            }
            overridePendingTransition(0, 0);
            return itemId == R.id.nav_subscription;
        });

        // Weekly Plan bindings
        tvWeeklyDesc = findViewById(R.id.tvWeeklyDesc);
        tvWeeklyDuration = findViewById(R.id.tvWeeklyDuration);
        tvWeeklyPrice = findViewById(R.id.tvWeeklyPrice);
        btnBuyNowWeekly = findViewById(R.id.btnBuyNowWeekly);

        // Monthly Plan bindings
        tvMonthlyDesc = findViewById(R.id.tvMonthlyDesc);
        tvMonthlyDuration = findViewById(R.id.tvMonthlyDuration);
        tvMonthlyPrice = findViewById(R.id.tvMonthlyPrice);
        btnBuyNowMonthly = findViewById(R.id.btnBuyNowMonthly);

        fetchSubscriptions();
    }

    private void fetchSubscriptions() {
        CollectionReference subsRef = db.collection("Subscriptions");

        subsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                String name = doc.getString("name");
                String description = doc.getString("description");
                Long duration = doc.getLong("durationInDays");
                Long mealsPerDay = doc.getLong("availableMealsPerDay");
                Double price = doc.getDouble("price");

                if (name != null) {
                    if (name.toLowerCase().contains("week")) {
                        // Weekly Plan
                        weeklyPlanName = name;
                        weeklyPlanPrice = price;

                        tvWeeklyDesc.setText(description);
                        tvWeeklyDuration.setText("Duration: " + duration + " days | Meals/day: " + mealsPerDay);
                        tvWeeklyPrice.setText("₹" + String.format("%.2f", price));

                        btnBuyNowWeekly.setOnClickListener(view -> {
                            // Moving to meal selection screen
                            Intent i = new Intent(Subscription.this, ChooseSubscriptionMeal.class);
                            i.putExtra("planName", weeklyPlanName);
                            i.putExtra("price", weeklyPlanPrice);
                            startActivity(i);
                        });

                    } else if (name.toLowerCase().contains("month")) {
                        // Monthly Plan
                        monthlyPlanName = name;
                        monthlyPlanPrice = price;

                        tvMonthlyDesc.setText(description);
                        tvMonthlyDuration.setText("Duration: " + duration + " days | Meals/day: " + mealsPerDay);
                        tvMonthlyPrice.setText("₹" + String.format("%.2f", price));

                        btnBuyNowMonthly.setOnClickListener(view -> {
                            // Moving to meal selection screen
                            Intent i = new Intent(Subscription.this, ChooseSubscriptionMeal.class);
                            i.putExtra("planName", monthlyPlanName);
                            i.putExtra("price", monthlyPlanPrice);
                            startActivity(i);
                        });
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // More detailed failure message
            Toast.makeText(this, "Failed to fetch plans. Please check your connection and try again. " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
