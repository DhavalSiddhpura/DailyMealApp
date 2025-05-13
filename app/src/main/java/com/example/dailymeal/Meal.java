package com.example.dailymeal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Meal extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MealAdapter mealAdapter;
    private ArrayList<MealModal> mealList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private CollectionReference mealRef;

    BottomNavigationView bottomNavigationView;
    ImageView imgProfile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meal);

        imgProfile = findViewById(R.id.imgProfile);

        // Set up bottom nav
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_meals);

        imgProfile.setOnClickListener(view -> {
            Intent i = new Intent(Meal.this, UserProfileActivity.class);
            i.putExtra("from", "Meal");
            startActivity(i);
        });

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mealRef = db.collection("Meals");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.mealRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mealList = new ArrayList<>();

        // Show quantity controls = false for this screen
        boolean showQuantityControls = false;
        mealAdapter = new MealAdapter(this, mealList, showQuantityControls);

        recyclerView.setAdapter(mealAdapter);

        // Fetch Meals
        fetchMealData();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("username", "Hi 👋");
        String location = sharedPreferences.getString("userlocation", "Getting location...");

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvLocation = findViewById(R.id.tvLocation);

        tvGreeting.setText("Hi, " + name + " 👋");
        tvLocation.setText(location);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_contactus) {
                startActivity(new Intent(getApplicationContext(), ContactUs.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_subscription) {
                startActivity(new Intent(getApplicationContext(), Subscription.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(getApplicationContext(), Order.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_meals) {
                return true; // already on this
            }
            return false;
        });
    }

    private void fetchMealData() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        mealRef.whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mealList.clear(); // Clear the list before adding new data
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        MealModal meal = doc.toObject(MealModal.class);
                        mealList.add(meal);
                    }
                    mealAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Meal.this, "Error fetching meals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
