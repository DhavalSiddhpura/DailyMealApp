package com.example.dailymeal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PaymentGateway extends AppCompatActivity {

    private TextView tvPlanName, tvPlanPrice, tvSelectedMeals;
    private ImageView backtosubscription;
    private RadioGroup radioGroupPayment;
    private RadioButton radioUpi, radioCard;
    private Button btnPayNow;
    private CardView cardUpi, cardCard;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_gateway);

        // Initialize Views
        tvPlanName = findViewById(R.id.tvPlanName);
        tvPlanPrice = findViewById(R.id.tvPlanPrice);
        tvSelectedMeals = findViewById(R.id.tvSelectedMeals);
        backtosubscription = findViewById(R.id.backtosubscription);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        radioUpi = findViewById(R.id.radioUpi);
        radioCard = findViewById(R.id.radioCard);
        btnPayNow = findViewById(R.id.btnpaynow);
        cardUpi = findViewById(R.id.cardUpi);
        cardCard = findViewById(R.id.cardCard);

        // Back Button Click
    backtosubscription.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(PaymentGateway.this,Subscription.class);
            startActivity(i);
            finish();
        }
    });

        // Receive and set data from previous activity
        String planName = getIntent().getStringExtra("planName");
        double price = getIntent().getDoubleExtra("price", 0.0);
        String selectedMeals = getIntent().getStringExtra("selectedMeals");

        tvPlanName.setText("Plan: " + planName);
        tvPlanPrice.setText("Amount: ₹" + String.format("%.2f", price));
        tvSelectedMeals.setText("Selected Meals: " + selectedMeals);

        // Handle CardView clicks
        cardUpi.setOnClickListener(v -> {
            radioGroupPayment.check(radioUpi.getId()); // Properly select UPI radio button
        });

        cardCard.setOnClickListener(v -> {
            radioGroupPayment.check(radioCard.getId()); // Properly select Card radio button
        });

        // Pay Now Button
        btnPayNow.setOnClickListener(view -> {
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(PaymentGateway.this, "Please select a payment method!", Toast.LENGTH_SHORT).show();
                return;
            }

            String paymentMethod = (selectedId == R.id.radioUpi) ? "UPI" : "Card";

            // Simulate successful payment
            Toast.makeText(PaymentGateway.this, "Real-time Payment function is not implemented yet... (" + paymentMethod + ")", Toast.LENGTH_LONG).show();

            // Navigate to Home
            Intent intent = new Intent(PaymentGateway.this, Home.class);
            startActivity(intent);
            finish();
        });
    }
}
