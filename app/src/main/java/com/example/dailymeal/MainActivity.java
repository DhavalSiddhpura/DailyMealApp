package com.example.dailymeal;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Button register, login;
    FirebaseAuth mAuth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        register = findViewById(R.id.register_b);
        login = findViewById(R.id.login_b);
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying user session...");
        progressDialog.setCancelable(false);

        // ✅ Check Internet
        if (!isInternetAvailable()) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();
            // Stop further execution
        }

        // ✅ Show progress dialog and check session
        progressDialog.show();

        if (mAuth.getCurrentUser() != null) {
            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                startActivity(new Intent(MainActivity.this, Home.class));
                finish();
            }, 1000);
        } else {
            register.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            progressDialog.dismiss();

            register.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, Register.class));
            });

            login.setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, Login.class));
            });
        }
    }

    // 🔌 Internet connectivity check
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
