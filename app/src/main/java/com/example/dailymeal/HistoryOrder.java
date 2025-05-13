package com.example.dailymeal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class HistoryOrder extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private LinearLayout orderHistoryContainer;
    ImageView backtoHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_order);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        orderHistoryContainer = findViewById(R.id.orderHistoryContainer);

        fetchOrderHistory();
    }

    private void fetchOrderHistory() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("Orders")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            String status = document.getString("status");
                            Long bill = document.getLong("totalBill");
                            List<Map<String, Object>> meals = (List<Map<String, Object>>) document.get("meals");

                            // Inflate card layout
                            LayoutInflater inflater = LayoutInflater.from(this);
                            CardView cardLayout = (CardView) inflater.inflate(R.layout.card_order_history, null);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(0, 15, 0, 30);
                            cardLayout.setLayoutParams(params);

                            TextView tvStatus = cardLayout.findViewById(R.id.tvOrderStatus);
                            TextView tvBill = cardLayout.findViewById(R.id.tvOrderTotal);
                            TextView tvMeals = cardLayout.findViewById(R.id.tvOrderMeals);

                            tvStatus.setText("Delivery Status: " + status);
                            tvBill.setText("Total Bill: ₹" + bill);

                            StringBuilder builder = new StringBuilder();
                            builder.append("Meals:\n");
                            for (Map<String, Object> meal : meals) {
                                builder.append("• ")
                                        .append(meal.get("name"))
                                        .append(" (x")
                                        .append(meal.get("quantity"))
                                        .append(")\n");
                            }
                            tvMeals.setText(builder.toString().trim());

                            orderHistoryContainer.addView(cardLayout);
                        }
                    } else {
                        Toast.makeText(this, "No past orders found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show());
        backtoHome = findViewById(R.id.backtohome);
        backtoHome.setOnClickListener(view -> {
            Intent intent = new Intent(HistoryOrder.this, Home.class);
            startActivity(intent);
            finish();
        });
    }
}
