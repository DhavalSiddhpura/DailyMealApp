package com.example.dailymeal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmOrder extends AppCompatActivity {

    private TextView totalBillAmount, userAddressView;
    private RadioButton radioUpi, radioCard, radioCod;
    ImageView backto_orders;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_order);

        totalBillAmount = findViewById(R.id.totalBillAmount);
        userAddressView = findViewById(R.id.userAddress);

        backto_orders = findViewById(R.id.backto_orders);
        backto_orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ConfirmOrder.this, Order.class);
                startActivity(i);
                finish();
            }
        });

        radioUpi = findViewById(R.id.radioUpi);
        radioCard = findViewById(R.id.radioCard);
        radioCod = findViewById(R.id.radioCod);

        // Ensure only one radio button is selected
        radioUpi.setOnClickListener(v -> {
            radioUpi.setChecked(true);
            radioCard.setChecked(false);
            radioCod.setChecked(false);
        });

        radioCard.setOnClickListener(v -> {
            radioUpi.setChecked(false);
            radioCard.setChecked(true);
            radioCod.setChecked(false);
        });

        radioCod.setOnClickListener(v -> {
            radioUpi.setChecked(false);
            radioCard.setChecked(false);
            radioCod.setChecked(true);
        });

        // Get data from Intent
        int totalBill = getIntent().getIntExtra("totalBill", 0);
        String userAddress = getIntent().getStringExtra("userAddress");

        totalBillAmount.setText("₹" + totalBill);
        userAddressView.setText(userAddress);

        Button btnPlaceOrder = findViewById(R.id.btnplaceorder);
        btnPlaceOrder.setOnClickListener(view -> {
            if (radioCod.isChecked()) {
                placeOrder();
            } else {
                Toast.makeText(this, "Please select Cash on Delivery option to place order", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void placeOrder() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();

        List<Map<String, Object>> mealList = new ArrayList<>();
        for (MealModal meal : SelectedMealManager.getInstance().getSelectedMeals()) {
            Map<String, Object> mealMap = new HashMap<>();
            mealMap.put("name", meal.getName());
            mealMap.put("description", meal.getDescription());
            mealMap.put("type", meal.getMealType());
            mealMap.put("price", meal.getPrice());

            // ✅ Fetch and save the actual quantity from MealModal
            mealMap.put("quantity", meal.getQuantity());

            mealList.add(mealMap);
        }

        int mealTotal = SelectedMealManager.getInstance().getCurrentMealTotal();
        int deliveryFee = SelectedMealManager.getInstance().getDeliveryFee();
        int handlingFee = SelectedMealManager.getInstance().getHandlingFee();
        int totalBill = mealTotal + handlingFee;

        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("userId", uid);
        orderMap.put("meals", mealList);
        orderMap.put("mealTotal", mealTotal);
        orderMap.put("deliveryFee", deliveryFee);
        orderMap.put("handlingFee", handlingFee);
        orderMap.put("totalBill", totalBill);
        orderMap.put("timestamp", FieldValue.serverTimestamp());
        orderMap.put("status", "Pending");

        db.collection("Orders").add(orderMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                    SelectedMealManager.getInstance().clearMeals();
                    Intent i = new Intent(ConfirmOrder.this, DeliveredOrder.class);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
