package com.example.dailymeal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class ChooseSubscriptionMeal extends AppCompatActivity {

    private LinearLayout lunchGroup, dinnerGroup;
    private Button btnGoToPayment;
    private ImageView backtosubscription;

    private FirebaseFirestore db;
    private String selectedLunch = "", selectedDinner = "";

    private String planName;
    private double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_subscription_meal);

        lunchGroup = findViewById(R.id.lunchGroup);
        dinnerGroup = findViewById(R.id.dinnerGroup);
        btnGoToPayment = findViewById(R.id.btnGoToPayment);

        db = FirebaseFirestore.getInstance();

        planName = getIntent().getStringExtra("planName");
        price = getIntent().getDoubleExtra("price", 0.0);

        backtosubscription = findViewById(R.id.backtosubscription);
        backtosubscription.setOnClickListener(view -> {
            Intent i = new Intent(ChooseSubscriptionMeal.this, Subscription.class);
            startActivity(i);
            finish();
        });

        fetchMeals();

        btnGoToPayment.setOnClickListener(view -> {
            if (selectedLunch.isEmpty() || selectedDinner.isEmpty()) {
                Toast.makeText(this, "Please select both lunch and dinner meals", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedMeals = "Lunch: " + selectedLunch + ", Dinner: " + selectedDinner;

            Intent intent = new Intent(ChooseSubscriptionMeal.this, PaymentGateway.class);
            intent.putExtra("planName", planName);
            intent.putExtra("price", price);
            intent.putExtra("selectedMeals", selectedMeals);
            startActivity(intent);
        });
    }

    private void fetchMeals() {
        db.collection("Meals").get().addOnSuccessListener(querySnapshot -> {
            for (QueryDocumentSnapshot doc : querySnapshot) {
                if (doc.contains("available") && !Objects.requireNonNull(doc.getBoolean("available"))) {
                    continue;
                }

                String name = doc.getString("name");
                String description = doc.getString("description");
                String imgUrl = doc.getString("imgurl");
                String mealType = doc.getString("mealType");
                int price = doc.getDouble("price").intValue();

                addMealCard(name, description, imgUrl, mealType, price);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch meals: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addMealCard(String name, String description, String imgUrl, String mealType, int price) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View mealCard = inflater.inflate(R.layout.item_choose_meal, null);

        ImageView imgMeal = mealCard.findViewById(R.id.imgMeal);
        TextView tvName = mealCard.findViewById(R.id.tvMealName);
        TextView tvDesc = mealCard.findViewById(R.id.tvMealDescription);
        TextView tvMealType = mealCard.findViewById(R.id.tvMealTypeDay);
        TextView tvPrice = mealCard.findViewById(R.id.tvMealPrice);
        RadioButton radioSelectMeal = mealCard.findViewById(R.id.radioSelectMeal);

        tvName.setText(name);
        tvDesc.setText(description);
        tvMealType.setText(mealType.substring(0, 1).toUpperCase() + mealType.substring(1));
        tvPrice.setText(" " + price);
        radioSelectMeal.setText("Select " + name);
        Glide.with(this).load(imgUrl).into(imgMeal);

        LinearLayout targetGroup = mealType.equalsIgnoreCase("lunch") ? lunchGroup : dinnerGroup;

        mealCard.setOnClickListener(v -> {
            clearOtherSelections(targetGroup);
            radioSelectMeal.setChecked(true);
            if (mealType.equalsIgnoreCase("lunch")) {
                selectedLunch = name;
            } else {
                selectedDinner = name;
            }
        });

        radioSelectMeal.setOnClickListener(v -> {
            clearOtherSelections(targetGroup);
            radioSelectMeal.setChecked(true);
            if (mealType.equalsIgnoreCase("lunch")) {
                selectedLunch = name;
            } else {
                selectedDinner = name;
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 15, 0, 30);
        mealCard.setLayoutParams(params);

        targetGroup.addView(mealCard);
    }

    private void clearOtherSelections(LinearLayout group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            RadioButton rb = child.findViewById(R.id.radioSelectMeal);
            if (rb != null) {
                rb.setChecked(false);
            }
        }
    }
}
