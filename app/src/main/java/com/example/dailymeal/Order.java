package com.example.dailymeal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Order extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    ImageView imgProfile;

    private TextView tvMealTotal, tvDeliveryFee, tvHandlingFee, tvTotalPrice;
    private int mealTotal = 0;
    private final int handlingFee = 12;
    private final int deliveryFee = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_order);

        imgProfile = findViewById(R.id.imgProfile);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_orders);

        tvMealTotal = findViewById(R.id.tvMealTotal);
        tvDeliveryFee = findViewById(R.id.tvDeliveryFee);
        tvHandlingFee = findViewById(R.id.tvHandlingFee);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        tvDeliveryFee.setText("₹" + deliveryFee);
        tvHandlingFee.setText("₹" + handlingFee);

        imgProfile.setOnClickListener(view -> {
            Intent i = new Intent(Order.this, UserProfileActivity.class);
            i.putExtra("from", "Order");
            startActivity(i);
        });

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
            } else if (itemId == R.id.nav_meals) {
                startActivity(new Intent(getApplicationContext(), Meal.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_orders) {
                return true;
            }
            return false;
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"})
        LinearLayout container = findViewById(R.id.linearLayoutMeals);
        LayoutInflater inflater = LayoutInflater.from(this);
        List<MealModal> meals = SelectedMealManager.getInstance().getSelectedMeals();

        for (MealModal meal : meals) {
            View card = inflater.inflate(R.layout.meal_item, container, false);

            ((TextView) card.findViewById(R.id.tvMealName)).setText(meal.getName());
            ((TextView) card.findViewById(R.id.tvMealDescription)).setText(meal.getDescription());
            ((TextView) card.findViewById(R.id.tvMealTypeDay)).setText(meal.getMealType());
            ((TextView) card.findViewById(R.id.tvMealPrice)).setText("₹" + meal.getPrice());

            ImageView mealImage = card.findViewById(R.id.imgMeal);
            Glide.with(this).load(meal.getImgurl()).into(mealImage);

            Button btnDecrease = card.findViewById(R.id.btnDecrease);
            Button btnIncrease = card.findViewById(R.id.btnIncrease);
            TextView tvQuantity = card.findViewById(R.id.tvQuantity);

            int[] quantity = {1};
            int[] previousQuantity = {1};

            meal.setQuantity(quantity[0]); // ✅ initialize quantity inside MealModal
            updateBillDetails(meal.getPrice(), 1);

            btnDecrease.setOnClickListener(v -> {
                if (quantity[0] > 1) {
                    previousQuantity[0] = quantity[0];
                    quantity[0]--;
                    tvQuantity.setText(String.valueOf(quantity[0]));

                    meal.setQuantity(quantity[0]); // ✅ update quantity in MealModal
                    updateBillDetails(meal.getPrice(), quantity[0] - previousQuantity[0]);
                } else {
                    container.removeView(card);
                    SelectedMealManager.getInstance().removeMeal(meal);
                    updateBillDetails(-meal.getPrice(), 1);

                    if (container.getChildCount() == 0) {
                        mealTotal = 0;
                        tvMealTotal.setText("₹0");
                        tvTotalPrice.setText(" ₹ 0 ");
                    }
                }
            });

            btnIncrease.setOnClickListener(v -> {
                previousQuantity[0] = quantity[0];
                quantity[0]++;
                tvQuantity.setText(String.valueOf(quantity[0]));

                meal.setQuantity(quantity[0]); // ✅ update quantity in MealModal
                updateBillDetails(meal.getPrice(), quantity[0] - previousQuantity[0]);
            });

            Button btnOrder = card.findViewById(R.id.btnOrder);
            btnOrder.setVisibility(View.GONE);

            container.addView(card);
        }

        Button btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnConfirmOrder.setOnClickListener(view -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String uid = auth.getCurrentUser().getUid();

            int totalBill = mealTotal + handlingFee;

            db.collection("Users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String address = documentSnapshot.getString("address");

                        SelectedMealManager.getInstance().setCurrentMealTotal(mealTotal);
                        SelectedMealManager.getInstance().setHandlingFee(handlingFee);
                        SelectedMealManager.getInstance().setDeliveryFee(deliveryFee);

                        Intent i = new Intent(Order.this, ConfirmOrder.class);
                        i.putExtra("totalBill", totalBill);
                        i.putExtra("userAddress", address);
                        startActivity(i);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Order.this, "Failed to fetch user address", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void updateBillDetails(int mealPrice, int quantityDelta) {
        mealTotal += mealPrice * quantityDelta;

        tvMealTotal.setText("₹" + mealTotal);

        int total;
        if (mealTotal > 0) {
            tvHandlingFee.setText("₹" + handlingFee);
            total = mealTotal + handlingFee;
        } else {
            tvHandlingFee.setText("₹0");
            total = 0;
        }

        tvTotalPrice.setText(" ₹ " + total);
    }
}
