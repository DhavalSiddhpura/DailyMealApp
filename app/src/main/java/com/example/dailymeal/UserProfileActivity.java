package com.example.dailymeal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileActivity extends AppCompatActivity {

    TextView tvUserName, tvUserPhone, tvUserEmail,tvUserAddress;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    ImageView backToHome;
    String uid;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        backToHome = findViewById(R.id.backtohome);

        String fromActivity = getIntent().getStringExtra("from");

        backToHome.setOnClickListener(view -> {
            Intent i;
            if ("Meal".equals(fromActivity)) {
                i = new Intent(UserProfileActivity.this, Meal.class);
            } else if("Order".equals(fromActivity))  {
                i = new Intent(UserProfileActivity.this, Order.class);
            } else if("Subscription".equals(fromActivity)) {
                i = new Intent(UserProfileActivity.this, Subscription.class);
            }else if("ContactUs".equals(fromActivity)) {
                i = new Intent(UserProfileActivity.this, ContactUs.class);
            }else{
                i = new Intent(UserProfileActivity.this, Home.class);
            }
            startActivity(i);
            finish();
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(view -> {
            new android.app.AlertDialog.Builder(UserProfileActivity.this)
                    .setTitle("Logout Confirmation")
                    .setMessage("Do you want to logout from Daily Meal?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User chose to logout
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User chose not to logout, dismiss dialog
                        dialog.dismiss();
                    })
                    .show();
        });
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserAddress = findViewById(R.id.tvUserAddress);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        uid = auth.getCurrentUser().getUid();

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String phone = documentSnapshot.getString("phone");
                        String email = documentSnapshot.getString("email");
                        String address = documentSnapshot.getString("address");

                        tvUserName.setText("Name: " + name);
                        tvUserPhone.setText("Phone: " + phone);
                        tvUserEmail.setText("Email: " + email);
                        tvUserAddress.setText("Address : "+address);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching data: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        Button btnAccDelete = findViewById(R.id.btnaccdelete);

        btnAccDelete.setOnClickListener(view -> {
            new android.app.AlertDialog.Builder(UserProfileActivity.this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account permanently?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        uid = auth.getCurrentUser().getUid();

                        // 1. Delete from Firestore
                        db.collection("Users").document(uid).delete()
                                .addOnSuccessListener(aVoid -> {
                                    // 2. Delete from Authentication
                                    auth.getCurrentUser().delete()
                                            .addOnCompleteListener(task -> {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                                                    // 3. Redirect to login
                                                    Intent i = new Intent(UserProfileActivity.this, MainActivity.class);
                                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                } else {
                                                    Toast.makeText(this, "Failed to delete user from authentication", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to delete Firestore data", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });


        Button btnEditProfile = findViewById(R.id.btnEditProfile);

        btnEditProfile.setOnClickListener(view -> {
            Intent intent = new Intent(UserProfileActivity.this, EditProfile.class);

            // Pass current user data to EditProfileActivity
            intent.putExtra("name", tvUserName.getText().toString().replace("Name: ", ""));
            intent.putExtra("phone", tvUserPhone.getText().toString().replace("Phone: ", ""));
            intent.putExtra("email", tvUserEmail.getText().toString().replace("Email: ", ""));
            intent.putExtra("address", tvUserAddress.getText().toString().replace("Address : ", ""));

            startActivity(intent);
        });

    }
}