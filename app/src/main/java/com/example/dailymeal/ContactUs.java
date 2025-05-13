package com.example.dailymeal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class ContactUs extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imgProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contact_us); // we'll make this next

        imgProfile = findViewById(R.id.imgProfile);

        // Set up bottom nav
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_contactus); // highlight this icon

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_meals) {
                startActivity(new Intent(getApplicationContext(), Meal.class));
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
            } else if (itemId == R.id.nav_contactus) {
                return true; // already on this
            }
            return false;
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String name = sharedPreferences.getString("username", "Hi 👋");
        String location = sharedPreferences.getString("userlocation", "Getting location...");

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvLocation = findViewById(R.id.tvLocation);

        tvGreeting.setText("Hi, " + name + " 👋");
        tvLocation.setText(location);

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ContactUs.this, UserProfileActivity.class);
                i.putExtra("from", "ContactUs");
                startActivity(i);
            }
        });


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        TextView addressTextView = findViewById(R.id.tvconaddress);
        TextView phoneTextView = findViewById(R.id.tvconphone);
        TextView emailTextView = findViewById(R.id.tvconemail);

        db.collection("ContactUs").document("Info")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String address = documentSnapshot.getString("address");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");

                        addressTextView.setText(address);
                        phoneTextView.setText(phone);
                        emailTextView.setText(email);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ContactUs.this, "Failed to load contact info : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }
}
